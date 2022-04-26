package v2

import spray.json.JsValue

// https://developer.twitter.com/en/docs/twitter-api/data-dictionary/object-model/user

case class User(
               id: String,
               name: String,
               username: String,
               created_at: Option[JsValue],
               description: Option[String],
               entities: Option[JsValue],
               location: Option[String],
               pinned_tweet_id : Option[String],
               profile_image_url: Option[String],
               protected_val: Option[Boolean],
               public_metrics: Option[JsValue],
               url: Option[String],
               verified: Option[String],
               withheld: Option[JsValue]
)