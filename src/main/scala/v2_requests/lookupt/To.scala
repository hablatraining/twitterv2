package v2_requests.lookupt

import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.http.scaladsl.model.headers.{Authorization, OAuth2BearerToken}

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

