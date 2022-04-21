package dev.habla.twitter
package v2
package main

sealed abstract class Command

case class LookupTweet(
  id: String,
  bearer_token: String, 
  expansions: Option[String] = None, 
  mediaFields: Option[String] = None,
  placeFields: Option[String] = None,
  pollFields: Option[String] = None,
  tweetFields: Option[String] = None,
  userFields: Option[String] = None)
extends Command {
  def toLookupTweetRequest: lookupt.Request = 
    lookupt.Request(id, bearer_token, expansions, mediaFields, placeFields, pollFields, tweetFields, userFields)
}

case class SearchRecent(
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
extends Command {

  def toSearchRecentCommand: Either[String, recents.Request] = {
    val singleRequest = recents.SingleRequest(query, bearer_token, next_token, max_results, start_time, end_time, since_id, until_id, tweet_fields)
    (all, max, file_name) match {
      case (false, None, None) => Right(singleRequest)
      case (false, None, Some(_)) => Left("No file if no pagination required")
      case (_, Some(max), Some(fileName)) => Right(recents.Pagination(singleRequest, fileName, Some(max)))
      case (true, _, Some(fileName)) => Right(recents.Pagination(singleRequest, fileName, None))
      case (_, _, None) => Left("Pagination required, but no file name specified")
    }
  }
}


case class LookupUser(
                       idOrName: String,
                       bearer_token: String,
                       expansions: Option[String] = None,
                       userFields: Option[String] = None,
                       tweetFields: Option[String] = None)

  extends Command {

  def toLookupUserRequest: lookupuser.Request = {
    if (idOrName.exists(_.isLetter))
      lookupuser.Request(Right(idOrName), bearer_token, expansions, userFields, tweetFields)
    else
      lookupuser.Request(Left(idOrName), bearer_token, expansions, userFields, tweetFields)

  }

}

case class LookupUsers(
                       ids: String,
                       bearer_token: String,
                       expansions: Option[String] = None,
                       userFields: Option[String] = None,
                       tweetFields: Option[String] = None)

  extends Command {

  def toLookupUsersRequest: lookupusers.Request =
    lookupusers.Request(ids, bearer_token, expansions, userFields, tweetFields)

}

case class LookupUsersBy(
                        usernames: String,
                        bearer_token: String,
                        expansions: Option[String] = None,
                        userFields: Option[String] = None,
                        tweetFields: Option[String] = None)

  extends Command {

  def toLookupUsersByRequest: lookupusersby.Request =
    lookupusersby.Request(usernames, bearer_token, expansions, userFields, tweetFields)

}
