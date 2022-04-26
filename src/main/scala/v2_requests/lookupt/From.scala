package v2_requests.lookupt

import v2_requests._
import v2.lookupt._
import ujson._
import spray.json._

import scala.util.Try


trait From extends HttpBody with RateLimitHeaders{

    def from(response: requests.Response): Response = {
        parseTweetInfo(response, parseBody(response))
          .orElse(parseRateLimitExceeded(response))
          .orElse(parseErroneousTextResponse(parseBody(response)))
          .orElse(parseErroneousJsonResponse(parseBody(response)))
          .getOrElse(ErroneousTextResponse("Not a lookup response"))
        }

    def parseTweetInfo(response: requests.Response, bodyE: Either[String, JsValue]): Option[TweetInfo] =
        for {
            body <- bodyE.toOption if response.statusCode == 200
            tweets <- Try(body.convertTo[TweetInfo.Body]).toOption
            (rateRemaining, rateReset) <- parseRateLimitHeaders(response)
        } yield TweetInfo(tweets, rateRemaining, rateReset)

    def parseRateLimitExceeded(response: requests.Response): Option[RateLimitExceeded] =
        if (response.statusCode != 429) None
        else parseRateLimitHeaders(response).map{ case (_, l) => RateLimitExceeded(l) }

    def parseErroneousTextResponse(body: Either[String, JsValue]): Option[ErroneousJsonResponse] = 
        body.toOption.map(ErroneousJsonResponse)

    def parseErroneousJsonResponse(body: Either[String, JsValue]): Option[ErroneousTextResponse] = 
        body.swap.toOption.map(ErroneousTextResponse)
}
