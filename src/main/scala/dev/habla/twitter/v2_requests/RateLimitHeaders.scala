package dev.habla.twitter
package v2_requests

import scala.util.Try

trait RateLimitHeaders {
    def parseRateLimitHeaders(response: requests.Response): Option[(Int, Long)] = {
        for {
            rateResetH <- response.headers("x-rate-limit-reset").headOption
            rateReset <- Try(rateResetH.toLong).toOption
            rateRemainingH <- response.headers("x-rate-limit-remaining").headOption
            rateRemaining <- Try(rateRemainingH.toInt).toOption
        } yield (rateRemaining, rateReset)
    }
}
