package v2_requests

import scala.util.Try

trait RateLimitHeaders {

    //Me tendría que crear un tipo headers?
    //IDEA: implicit class con un método 'headers' para wrapear requests.Response
    def parseRateLimitHeaders(response: requests.Response): Option[(Int, Long)] = {
        for {
            rateResetH <- response.getHeader("x-rate-limit-reset")
            rateReset <- Try(rateResetH.toLong).toOption
            rateRemainingH <- response.getHeader("x-rate-limit-remaining")
            rateRemaining <- Try(rateRemainingH.toInt).toOption
        } yield (rateRemaining, rateReset)

        /*
        val valor = response.headers("x-rate-limit-reset").headOption.flatMap(rateResetH =>
            Try(rateResetH.toLong).toOption.flatMap(rateReset =>
                response.headers("x-rate-limit-remaining").headOption.flatMap(rateRemainingH =>
                    Try(rateRemainingH.toInt).toOption.map(rateRemaining =>
            (rateRemaining, rateReset)))))

         */
    }

    implicit class RequestsGetHeader(response: requests.Response) {
            def getHeader(name: String): Option[String] =
                response.headers(name).headOption
        }
}
