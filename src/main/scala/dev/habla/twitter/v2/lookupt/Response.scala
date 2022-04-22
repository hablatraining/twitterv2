package dev.habla.twitter
package v2
package lookupt

import spray.json.JsValue

sealed trait Response

case class TweetInfo(body: TweetInfo.Body, rateRemaining: Int, rateReset: Long) extends Response

object TweetInfo extends JsonSupport{
  case class Body(data: Tweet, includes: Option[Includes])
  case class Includes(places: List[JsValue])
}

case class RateLimitExceeded(rateResetTime: Long) extends Response

sealed trait ErroneousResponse extends Throwable with Response

case class ErroneousJsonResponse(data: JsValue) extends ErroneousResponse{
  override def toString: String = if (data != null) data.toString else "NULL"
}

case class ErroneousTextResponse(data: String) extends ErroneousResponse{
  override def toString: String = data
}

trait JsonSupport{
    import spray.json._
    import DefaultJsonProtocol._
    
    implicit val includesFormat: RootJsonFormat[TweetInfo.Includes] = jsonFormat1(TweetInfo.Includes)
    implicit val bodyFormat: RootJsonFormat[TweetInfo.Body] = jsonFormat2(TweetInfo.Body)
}