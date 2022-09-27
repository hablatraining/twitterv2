package dev.habla.twitter
package v2_requests
package lookupuser

import v2.lookupuser.Request

trait To extends QueryParams{
          
    def to(request: Request): requests.Request = {
        requests.Request(
            url = s"https://api.twitter.com/2/users/${request
              .idOrName
              .fold(
                  identity,
                  username => s"by/username/$username"
              )
            }",
            params = Map[String, String]()
              .add("expansions", request.expansions)
              .add("tweet.fields", request.tweetFields)
              .add("user.fields", request.userFields),
            headers = Map("Authorization" -> request.bearerToken)
        )
    }
}
