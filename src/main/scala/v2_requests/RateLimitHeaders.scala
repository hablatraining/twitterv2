package v2_requests

import scala.util.Try

trait RateLimitHeaders {
    //x-rate-limit-limit no se tiene en cuenta????
    //Me tendría que crear un tipo headers?
    //IDEA: implicit class con un método 'headers' para wrapear requests.Response
    def parseRateLimitHeaders(response: requests.Response): Option[(Int, Long)] = {

        for {
            rateResetH <- response.headers("x-rate-limit-reset").headOption
            rateReset <- Try(rateResetH.toLong).toOption
            rateRemainingH <- response.headers("x-rate-limit-remaining").headOption
            rateRemaining <- Try(rateRemainingH.toInt).toOption
        } yield (rateRemaining, rateReset)

        /*
        val valor = response.headers("x-rate-limit-reset").flatMap(rateResetH =>
            Try(rateResetH.toLong).toOption.flatMap(rateReset =>
                response.headers("x-rate-limit-remaining").flatMap(rateRemainingH =>
                    Try(rateRemainingH.toInt).toOption.map(rateRemaining =>
            (rateRemaining, rateReset))))).headOption
         */
    }


}
