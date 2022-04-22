package dev.habla.twitter
package v2
package lookupuser

sealed trait Response


import spray.json.JsValue



case class UserInfo(body: UserInfo.Body, rateRemaining: Int, rateReset: Long) extends Response

object UserInfo extends JsonSupport{
  case class Body(data: User, includes: Option[Includes])
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

  implicit val includesFormat: RootJsonFormat[UserInfo.Includes] = jsonFormat1(UserInfo.Includes)
  implicit val bodyFormat: RootJsonFormat[UserInfo.Body] = jsonFormat2(UserInfo.Body)
}
