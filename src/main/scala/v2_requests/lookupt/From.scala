package v2_requests
package lookupt

import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.stream.Materializer
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try


trait From extends HttpBody with RateLimitHeaders{

    def from(response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext): Future[Response] =
        parseBody(response).map{ bodyE => 
            parseTweetInfo(response, bodyE)
                .orElse(parseRateLimitExceeded(response))
                .orElse(parseErroneousTextResponse(bodyE))
                .orElse(parseErroneousJsonResponse(bodyE))
                .getOrElse(ErroneousTextResponse("Not a lookup response"))
        }

    def parseTweetInfo(response: HttpResponse, bodyE: Either[String,JsValue]): Option[TweetInfo] = 
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
