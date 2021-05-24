package dev.habla.twitter
package v2
package recents

sealed abstract class Request

case class SingleRequest(
  query: String, 
  bearer_token: String,
  next_token: Option[String]=None,
  max_results: Option[Int]=None,
  start_time: Option[String]=None, 
  end_time: Option[String]=None,
  since_id: Option[String]=None,
  until_id: Option[String]=None,
  tweet_fields: Option[String]=None)
extends Request

case class Pagination(
  request: SingleRequest, 
  file_name: String, 
  max: Option[Long] = None)
extends Request



