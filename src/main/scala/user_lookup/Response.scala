package dev.habla.twitter
package v2
package user_lookup

import scala.concurrent.duration._

import spray.json.JsValue

sealed trait SingleResponse

case class User(body: User.Body, rateRemaining: Int, rateReset: Long) extends SingleResponse

object User extends JsonSupport{
  case class Body(data: Option[List[JsValue]], includes: Option[Includes])
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
    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import spray.json._
    import DefaultJsonProtocol._
    
    implicit val includesFormat = jsonFormat1(Tweets.Includes)
    implicit val searchSingleResponseFormat = jsonFormat2(Tweets.Body)
}