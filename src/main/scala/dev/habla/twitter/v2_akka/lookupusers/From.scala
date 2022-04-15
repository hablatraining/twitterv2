package dev.habla.twitter
package v2_akka
package lookupusers

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.Materializer
import dev.habla.twitter.v2.lookupusers._
import dev.habla.twitter.v2_akka.{HttpBody, RateLimitHeaders}
import spray.json.JsValue

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait From extends HttpBody with RateLimitHeaders {

  def from(response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext): Future[Response] =
    parseBody(response).map { bodyE =>
      parseUserInfo(response, bodyE)
        .orElse(parseRateLimitExceeded(response))
        .orElse(parseErroneousTextResponse(bodyE))
        .orElse(parseErroneousJsonResponse(bodyE))
        .getOrElse(ErroneousTextResponse("Not a lookupUser response"))
    }

  def parseUserInfo(response: HttpResponse, bodyE: Either[String, JsValue]): Option[UserInfo] =
    for {
      body <- bodyE.toOption if response.status == StatusCodes.OK
      users <- Try(body.convertTo[UserInfo.Body]).toOption
      (rateRemaining, rateReset) <- parseRateLimitHeaders(response)
    } yield UserInfo(users, rateRemaining, rateReset)

  def parseRateLimitExceeded(response: HttpResponse): Option[RateLimitExceeded] =
    if (response.status != StatusCodes.TooManyRequests) None
    else parseRateLimitHeaders(response).map { case (_, l) => RateLimitExceeded(l) }

  def parseErroneousTextResponse(body: Either[String, JsValue]): Option[ErroneousJsonResponse] =
    body.toOption.map(ErroneousJsonResponse)

  def parseErroneousJsonResponse(body: Either[String, JsValue]): Option[ErroneousTextResponse] =
    body.swap.toOption.map(ErroneousTextResponse)
}