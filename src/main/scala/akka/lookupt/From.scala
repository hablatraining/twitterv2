package dev.habla.twitter
package v2
package akka
package lookupt

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
import api.lookupt._

trait From extends HttpBody with RateLimitHeaders{

    def from(response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext): Future[Response] =
        parseBody(response).map{ bodyE => 
            parseTweets(response, bodyE)
                .orElse(parseRateLimitExceeded(response))
                .orElse(parseErroneousTextResponse(bodyE))
                .orElse(parseErroneousJsonResponse(bodyE))
                .getOrElse(ErroneousTextResponse("Not a search recent response"))
        }

    def parseTweets(response: HttpResponse, bodyE: Either[String,JsValue]): Option[TweetInfo] = 
        for {
            body <- bodyE.toOption if response.status == StatusCodes.OK
            tweets <- Try(body.convertTo[TweetInfo.Body]).toOption
            (rateRemaining, rateReset) <- parseRateLimitHeaders(response)
        } yield TweetInfo(tweets, rateRemaining, rateReset)

    def parseRateLimitExceeded(response: HttpResponse): Option[RateLimitExceeded] = 
        if (response.status != StatusCodes.TooManyRequests) None
        else parseRateLimitHeaders(response).map{ case (_, l) => RateLimitExceeded(l) }

    def parseErroneousTextResponse(body: Either[String, JsValue]): Option[ErroneousJsonResponse] = 
        body.toOption.map(ErroneousJsonResponse)

    def parseErroneousJsonResponse(body: Either[String, JsValue]): Option[ErroneousTextResponse] = 
        body.swap.toOption.map(ErroneousTextResponse)
}
