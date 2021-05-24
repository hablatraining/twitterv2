package dev.habla.twitter
package v2
package recents

import scala.concurrent.duration._

import spray.json.JsValue

sealed trait Response

sealed trait PaginatedResponse extends Response

object PaginatedResponse{
  case class Ok(meta: Option[Meta]) extends PaginatedResponse
  case class Error(error: ErroneousSingleResponse, meta: Option[Meta]) extends PaginatedResponse
}

sealed trait SingleResponse

case class Tweets(body: Tweets.Body, rateRemaining: Int, rateReset: Long) extends SingleResponse

object Tweets extends JsonSupport{
  case class Body(data: Option[List[Tweet]], includes: Option[Includes], meta: Meta)
  case class Includes(places: List[JsValue])
}

case class RateLimitExceeded(rateResetTime: Long) extends SingleResponse

sealed trait ErroneousSingleResponse extends Throwable with SingleResponse

case class ErroneousJsonSingleResponse(data: JsValue) extends ErroneousSingleResponse{
  override def toString: String = if (data != null) data.toString else "NULL"
}

case class ErroneousTextSingleResponse(data: String) extends ErroneousSingleResponse{
  override def toString: String = data
}

trait JsonSupport{
    import _root_.akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import spray.json._
    import DefaultJsonProtocol._
    
    implicit val includesFormat = jsonFormat1(Tweets.Includes)
    implicit val searchSingleResponseFormat = jsonFormat3(Tweets.Body)
}