package dev.habla.twitter
package v2_akka
package lookupuser

import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}
import akka.http.scaladsl.model.{HttpRequest, Uri}
import dev.habla.twitter.v2.lookupuser.Request
import dev.habla.twitter.v2_akka.QueryParams

trait To extends QueryParams {

  def to(request: Request): HttpRequest = {
    val uriBegin = request.idOrName.fold(l => s"https://api.twitter.com/2/users/${l}", r => s"https://api.twitter.com/2/users/by/username/${r}")
    HttpRequest(
      uri = Uri(uriBegin)
        .withQuery(Uri.Query.apply(
          Map[String, String]()
            .add("expansions", request.expansions)
            .add("tweet.fields", request.tweetFields)
            .add("user.fields", request.userFields))),
      headers = Seq(Authorization(OAuth2BearerToken(request.bearerToken)))
    )
  }
}