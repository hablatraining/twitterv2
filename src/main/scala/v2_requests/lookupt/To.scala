package v2_requests.lookupt

import v2.lookupt.Request
import v2_requests.QueryParams

trait To extends QueryParams{
          
    def to(request: Request): requests.Request = {
        requests.Request(
            url = s"https://api.twitter.com/2/tweets/${request.id}",
            params = Map[String, String]()
              .add("expansions", request.expansions)
              .add("tweet.fields", request.tweetFields)
              .add("place.fields", request.placeFields),
            headers = Map("Authorization" -> request.bearerToken)
        )
    }
}