package dev.habla.twitter.v2

import spray.json.JsValue

case class User(
                 id: String,
                 text: String,
                 username: String,
                 created_at: Option[JsValue],
                 description: Option[JsValue],
                 entities: Option[JsValue],
                 location: Option[JsValue],
                 pinned_tweet_id: Option[JsValue],
                 profile_image_url: Option[JsValue],
                 protection: Option[JsValue],
                 public_metrics: Option[JsValue],
                 url: Option[JsValue],
                 verified: Option[JsValue],
                 withheld: Option[JsValue])

object User {

  import spray.json._
  import DefaultJsonProtocol._

  implicit val userFormat: RootJsonFormat[User] = jsonFormat14(User.apply)
}