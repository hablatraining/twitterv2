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
                       id: Option[String] = None,
                       username: Option[String] = None,
                       bearer_token: String,
                       expansions: Option[String] = None,
                       userFields: Option[String] = None,
                       tweetFields: Option[String] = None)

  extends Command {

  def toLookupUserRequest: Either[String,lookupuser.Request] =

    if (id.isDefined && username.isDefined)
      Left("Solo se puede definir o id o username")
    else if (id.isEmpty && username.isEmpty)
      Left("Se tiene que especificar o id o username")
    else if (id.isDefined)
      Right(lookupuser.Request(Left(id.get), bearer_token, expansions, userFields, tweetFields))
    else
      Right(lookupuser.Request(Right(username.get), bearer_token, expansions, userFields, tweetFields))



}

/**
case class LookupUser(
                       idOrName: String,
                       bearer_token: String,
                       expansions: Option[String] = None,
                       userFields: Option[String] = None,
                       tweetFields: Option[String] = None)

  extends Command {

  def toLookupUserRequest: lookupuser.Request =

    if (idOrName.exists(_.isLetter))
      lookupuser.Request(Right(idOrName), bearer_token, expansions, userFields, tweetFields)
    else
      lookupuser.Request(Left(idOrName), bearer_token, expansions, userFields, tweetFields)



}*/


case class LookupUsers(
                          ids: Option[List[String]] = None,
                          usernames: Option[List[String]] = None,
                          bearer_token: String,
                          expansions: Option[String] = None,
                          userFields: Option[String] = None,
                          tweetFields: Option[String] = None)

  extends Command {

  def toLookupUsersRequest: Either[String,lookupusers.Request] = {

    if (ids.isDefined && usernames.isDefined)
      Left("Solo se puede definir o ids o usernames")
    else if (ids.isEmpty && usernames.isEmpty)
      Left("Se tiene que especificar o id o username")
    else if (ids.isDefined)
      Right(lookupusers.Request(Left(ids.get), bearer_token, expansions, userFields, tweetFields))
    else
      Right(lookupusers.Request(Right(usernames.get), bearer_token, expansions, userFields, tweetFields))

  }
}
