package dev.habla.twitter
package v2
package recents
package http

import scala.jdk.OptionConverters._
import scala.util.Try
import scala.concurrent.Future
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.ModeledCustomHeader
import akka.http.scaladsl.model.headers.ModeledCustomHeaderCompanion
import akka.stream.Materializer
import spray.json._, DefaultJsonProtocol._

object From extends HttpBody with RateLimitHeaders{
    implicit val metaFormat: RootJsonFormat[Tweets.Meta] = jsonFormat4(Tweets.Meta.apply)
    implicit val includesFormat: RootJsonFormat[Tweets.Includes] = jsonFormat1(Tweets.Includes.apply)
    implicit val searchSingleResponseFormat: RootJsonFormat[Tweets.Body] = jsonFormat3(Tweets.Body.apply)

    def apply(response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext): Future[SingleResponse] =
        parseBody(response).map{ bodyE => 
            parseTweets(response, bodyE)
                .orElse(parseRateLimitExceeded(response))
                .orElse(parseErroneousTextResponse(bodyE))
                .orElse(parseErroneousJsonResponse(bodyE))
                .getOrElse(SingleResponse.Error(ErroneousSingleResponse.Text("Not a search recent response")))
        }

    def parseTweets(response: HttpResponse, bodyE: Either[String,JsValue]): Option[SingleResponse.Ok] = 
        if (response.status != StatusCodes.OK) None
        else for {
            body <- bodyE.toOption
            tweets <- Try(body.convertTo[Tweets.Body]).toOption
            (rateRemaining, rateReset) <- parseRateLimitHeaders(response)
        } yield SingleResponse.Ok(Tweets(tweets, rateRemaining, rateReset))

    def parseRateLimitExceeded(response: HttpResponse): Option[SingleResponse.RateLimitExceeded] = 
        if (response.status != StatusCodes.TooManyRequests) None
        else parseRateLimitHeaders(response).map{ case (_, l) => SingleResponse.RateLimitExceeded(l) }

    def parseErroneousTextResponse(body: Either[String, JsValue]): Option[SingleResponse.Error] = 
        body.toOption.map(ErroneousSingleResponse.Json.apply).map(SingleResponse.Error.apply)

    def parseErroneousJsonResponse(body: Either[String, JsValue]): Option[SingleResponse.Error] = 
        body.swap.toOption.map(ErroneousSingleResponse.Text.apply).map(SingleResponse.Error.apply)
}

trait HttpBody{

    def parseBody(response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext): Future[Either[String, JsValue]] = 
        Unmarshal(response).to[String].map(parseJson)

    def parseJson(body: String): Either[String, JsValue] = 
        Try(body.parseJson).toEither.left.map(_ => body)
}


trait RateLimitHeaders{
  
    def parseRateLimitHeaders(response: HttpResponse): Option[(Int, Long)] = 
        for {
            rateResetH <- response.getHeader(XRateLimitReset.name).toScala
            rateReset <- Try(java.lang.Long.parseLong(rateResetH.value())).toOption
            rateRemainingH <- response.getHeader(XRateLimitRemaining.name).toScala
            rateRemaining <- Try(Integer.parseInt(rateRemainingH.value())).toOption
        } yield (rateRemaining, rateReset)

    final class XRateLimitReset(token: String) extends ModeledCustomHeader[XRateLimitReset] {
        override def renderInRequests = true
        override def renderInResponses = true
        override val companion = XRateLimitReset
        override def value: String = token
    }

    object XRateLimitReset extends ModeledCustomHeaderCompanion[XRateLimitReset] {
        override val name = "x-rate-limit-reset"
        override def parse(value: String) = Try(new XRateLimitReset(value))
    }

    final class XRateLimitRemaining(token: String) extends ModeledCustomHeader[XRateLimitRemaining] {
        override def renderInRequests = true
        override def renderInResponses = true
        override val companion = XRateLimitRemaining
        override def value: String = token
    }

    object XRateLimitRemaining extends ModeledCustomHeaderCompanion[XRateLimitRemaining] {
        override val name = "x-rate-limit-remaining"
        override def parse(value: String) = Try(new XRateLimitRemaining(value))
    }
}