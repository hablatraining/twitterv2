package dev.habla.twitter
package v2
package user_lookup

sealed abstract class Request

case class SingleRequest(
  username: String,
  tweet_fields: Option[String]=None,
  user_fields: Option[String]=None
  bearer_token: String
  )
extends Request
