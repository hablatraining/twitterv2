package dev.habla.twitter
package v2_akka
package lookupusersby

import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpRequest, Uri}
import dev.habla.twitter.v2.lookupusersby.Request
import dev.habla.twitter.v2_akka.QueryParams


trait To extends QueryParams {

  def to(request: Request): HttpRequest =
    HttpRequest(
      uri = Uri("https://api.twitter.com/2/users/by")
        .withQuery(Uri.Query.apply(
          Map[String, String]()
            .add("usernames", request.usernames)
            .add("expansions", request.expansions)
            .add("tweet.fields", request.tweetFields)
            .add("user.fields", request.userFields))),
      headers = Seq(Authorization(OAuth2BearerToken(request.bearerToken)))
    )
}