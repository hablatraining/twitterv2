package dev.habla.twitter.v2
package lookupt
package akka

import _root_.akka.http.scaladsl.model.headers.Authorization
import _root_.akka.http.scaladsl.model.HttpRequest
import _root_.akka.http.scaladsl.model.headers.OAuth2BearerToken
import _root_.akka.http.scaladsl.model.Uri

trait To extends api.akka.QueryParams{
          
    def to(request: Request): HttpRequest = 
        HttpRequest(
            uri = Uri(s"https://api.twitter.com/2/tweets/${request.id}")
                    .withQuery(Uri.Query.apply(
                            Map[String, String]()
                                .add("expansions", request.expansions)
                                .add("tweet.fields", request.tweetFields)
                                .add("place.fields", request.placeFields))),
            headers = Seq(Authorization(OAuth2BearerToken(request.bearerToken)))
        )
}

