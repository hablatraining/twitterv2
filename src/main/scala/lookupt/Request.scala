package dev.habla.twitter
package v2
package lookupt

case class Request(
  id: String,
  bearerToken: String, 
  expansions: Option[String], 
  mediaFields: Option[String],
  placeFields: Option[String],
  pollFields: Option[String],
  tweetFields: Option[String],
  userFields: Option[String])
