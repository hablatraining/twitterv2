package dev.habla.twitter
package v2_requests
package lookupuser

import dev.habla.twitter.v2
import dev.habla.twitter.v2.lookupuser._
import spray.json.JsValue

import scala.util.Try


trait From extends HttpBody with RateLimitHeaders{

    def from(response: requests.Response): Response = {
        val bodyE = parseBody(response)
        parseTweetInfo(response, bodyE)
          .orElse(parseRateLimitExceeded(response))
          .orElse(parseErroneousTextResponse(bodyE))
          .orElse(parseErroneousJsonResponse(bodyE))
          .getOrElse(ErroneousTextResponse("Not a lookup response"))
        }

    def parseTweetInfo(response: requests.Response, bodyE: Either[String,JsValue]): Option[UserInfo] =
        for {
            body <- bodyE.toOption if response.statusCode == 200
            users <- Try(body.convertTo[UserInfo.Body]).toOption
            (rateRemaining, rateReset) <- parseRateLimitHeaders(response)
        } yield UserInfo(users, rateRemaining, rateReset)

    def parseRateLimitExceeded(response: requests.Response): Option[RateLimitExceeded] =
        if (response.statusCode != 429) None
        else parseRateLimitHeaders(response).map{ case (_, l) => RateLimitExceeded(l) }

    def parseErroneousTextResponse(body: Either[String, JsValue]): Option[ErroneousJsonResponse] = 
        body.toOption.map(ErroneousJsonResponse)

    def parseErroneousJsonResponse(body: Either[String, JsValue]): Option[ErroneousTextResponse] = 
        body.swap.toOption.map(ErroneousTextResponse)
}