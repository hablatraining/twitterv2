package dev.habla.twitter.v2
package api

import spray.json.JsValue

// https://developer.twitter.com/en/docs/twitter-api/data-dictionary/object-model/tweet

case class Tweet(
    id: String, 
    text: String, 
    attachments: Option[JsValue], 
    author_id: Option[JsValue], 
    context_annotations: Option[JsValue], 
    conversation_id: Option[JsValue], 
    created_at: Option[JsValue], 
    entities: Option[JsValue], 
    geo: Option[JsValue], 
    in_reply_to_user_id: Option[JsValue], 
    lang: Option[JsValue], 
    non_public_metrics: Option[JsValue], 
    organic_metrics: Option[JsValue], 
    possiby_sensitive: Option[JsValue], 
    promoted_metrics: Option[JsValue], 
    public_metrics: Option[JsValue], 
    referenced_tweets: Option[JsValue], 
    reply_settings: Option[JsValue], 
    source: Option[JsValue], 
    withheld: Option[JsValue])

object Tweet{
    import _root_.akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import spray.json._
    import DefaultJsonProtocol._
    
    implicit val tweetFormat = jsonFormat20(Tweet.apply)

}