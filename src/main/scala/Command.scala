package dev.habla.twitter.v2

enum Command:
  case SearchRecent(
    query: String, 
    bearer_token: String,
    all: Boolean = false, 
    max: Option[Long] = None,
    file_name: Option[String] = None, 
    next_token: Option[String]=None,
    max_results: Option[Int]=None,
    start_time: Option[String]=None, 
    end_time: Option[String]=None,
    since_id: Option[String]=None,
    until_id: Option[String]=None,
    tweet_fields: Option[String]=None)

  def toSearchRecentCommand: Either[String, recents.Request] = this match {
    case SearchRecent(query, bearer_token, all, max, file_name, next_token, max_results, start_time, end_time, since_id, until_id, tweet_fields) => 
      val singleRequest: recents.Request.SingleRequest = recents.Request.SingleRequest(query, bearer_token, next_token, max_results, start_time, end_time, since_id, until_id, tweet_fields)
      (all, max, file_name) match {
        case (false, None, None) => Right(singleRequest)
        case (false, None, Some(_)) => Left("No file if no pagination required")
        case (_, Some(max), Some(fileName)) => Right(recents.Request.Pagination(singleRequest, fileName, Some(max)))
        case (true, _, Some(fileName)) => Right(recents.Request.Pagination(singleRequest, fileName, None))
        case (_, _, None) => Left("Pagination required, but no file name specified")
      }
    }
  

