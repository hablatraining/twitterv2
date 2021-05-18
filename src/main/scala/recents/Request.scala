package dev.habla.twitter
package v2
package recents

enum Request:

  case SingleRequest(
    query: String, 
    bearer_token: String,
    next_token: Option[String]=None,
    max_results: Option[Int]=None,
    start_time: Option[String]=None, 
    end_time: Option[String]=None,
    since_id: Option[String]=None,
    until_id: Option[String]=None,
    tweet_fields: Option[String]=None)

  case Pagination(
    request: SingleRequest, 
    file_name: String, 
    max: Option[Long] = None)



