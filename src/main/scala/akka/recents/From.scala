package dev.habla.twitter
package v2
package akka
package recents

import scala.jdk.OptionConverters._
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import _root_.akka.http.scaladsl.unmarshalling.Unmarshal
import _root_.akka.http.scaladsl.model.StatusCodes
import _root_.akka.http.scaladsl.model.HttpResponse
import _root_.akka.http.scaladsl.model.headers.ModeledCustomHeader
import _root_.akka.http.scaladsl.model.headers.ModeledCustomHeaderCompanion
import _root_.akka.stream.Materializer
import spray.json._, DefaultJsonProtocol._
import api.recents._

trait From extends HttpBody with RateLimitHeaders{

    def from(response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext): Future[SingleResponse] =
        parseBody(response).map{ bodyE => 
            parseTweets(response, bodyE)
                .orElse(parseRateLimitExceeded(response))
                .orElse(parseErroneousTextResponse(bodyE))
                .orElse(parseErroneousJsonResponse(bodyE))
                .getOrElse(ErroneousTextSingleResponse("Not a search recent response"))
        }

    def parseTweets(response: HttpResponse, bodyE: Either[String,JsValue]): Option[Tweets] = 
        if (response.status != StatusCodes.OK) None
        else for {
            body <- bodyE.toOption
            tweets <- Try(body.convertTo[Tweets.Body]).toOption
            (rateRemaining, rateReset) <- parseRateLimitHeaders(response)
        } yield Tweets(tweets, rateRemaining, rateReset)

    def parseRateLimitExceeded(response: HttpResponse): Option[RateLimitExceeded] = 
        if (response.status != StatusCodes.TooManyRequests) None
        else parseRateLimitHeaders(response).map{ case (_, l) => RateLimitExceeded(l) }

    def parseErroneousTextResponse(body: Either[String, JsValue]): Option[ErroneousJsonSingleResponse] = 
        body.toOption.map(ErroneousJsonSingleResponse)

    def parseErroneousJsonResponse(body: Either[String, JsValue]): Option[ErroneousTextSingleResponse] = 
        body.swap.toOption.map(ErroneousTextSingleResponse)
}
