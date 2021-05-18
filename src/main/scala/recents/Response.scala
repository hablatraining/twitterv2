package dev.habla.twitter
package v2
package recents

import scala.concurrent.duration._

import spray.json.JsValue

enum Response:
  case Paginated(response: PaginatedResponse)
  case Single(response: SingleResponse)

enum PaginatedResponse:
  case Ok(meta: Option[Tweets.Meta])
  case Error(error: ErroneousSingleResponse, meta: Option[Tweets.Meta])

enum SingleResponse:
  case Ok(tweets: Tweets)
  case RateLimitExceeded(rateResetTime: Long)
  case Error(error: ErroneousSingleResponse)

case class Tweets(body: Tweets.Body, rateRemaining: Int, rateReset: Long)

object Tweets extends JsonSupport{
  case class Body(data: Option[List[JsValue]], includes: Option[Includes], meta: Meta)
  case class Meta(newest_id: Option[String], oldest_id: Option[String], result_count: Int, next_token: Option[String])
  case class Includes(places: List[JsValue])
}

enum ErroneousSingleResponse:
  case Json(data: JsValue)
  case Text(data: String)

trait JsonSupport{ this: Tweets.type => 
    import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import spray.json._
    import DefaultJsonProtocol._
    
    implicit val metaFormat: RootJsonFormat[Meta] = jsonFormat4(Meta.apply)
    implicit val includesFormat: RootJsonFormat[Includes] = jsonFormat1(Includes.apply)
    implicit val searchSingleResponseFormat: RootJsonFormat[Body] = jsonFormat3(Body.apply)
}