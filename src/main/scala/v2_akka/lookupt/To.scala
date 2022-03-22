package v2_akka
package lookupt

import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.Uri

import v2.lookupt._

trait To extends QueryParams{
          
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

