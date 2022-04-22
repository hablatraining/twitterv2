package dev.habla.twitter
package v2
package recents

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
    import spray.json._
    import DefaultJsonProtocol._
    
    implicit val includesFormat: RootJsonFormat[Tweets.Includes] = jsonFormat1(Tweets.Includes)
    implicit val searchSingleResponseFormat: RootJsonFormat[Tweets.Body] = jsonFormat3(Tweets.Body)
}