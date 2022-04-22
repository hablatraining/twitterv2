package dev.habla.twitter
package v2_akka
package lookupusers

import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpRequest, Uri}
import dev.habla.twitter.v2.lookupusers.Request


trait To extends QueryParams {

  def to(request: Request): HttpRequest = {
    val requestData = request.idsOrNames.fold(
      listIds => ("https://api.twitter.com/2/users", "ids", listIds),
      listUsernames => ("https://api.twitter.com/2/users/by", "usernames", listUsernames)
    )
    HttpRequest(
      uri = Uri(requestData._1)
        .withQuery(Uri.Query.apply(
          Map[String, String]()
            .add(requestData._2, requestData._3.mkString(","))
            .add("expansions", request.expansions)
            .add("tweet.fields", request.tweetFields)
            .add("user.fields", request.userFields))),
      headers = Seq(Authorization(OAuth2BearerToken(request.bearerToken)))
    )
  }
}