package dev.habla.twitter
package v2
package api
package lookupt

case class Request(
  id: String,
  bearerToken: String, 
  expansions: Option[String] = None, 
  mediaFields: Option[String] = None,
  placeFields: Option[String] = None,
  pollFields: Option[String] = None,
  tweetFields: Option[String] = None,
  userFields: Option[String] = None)
