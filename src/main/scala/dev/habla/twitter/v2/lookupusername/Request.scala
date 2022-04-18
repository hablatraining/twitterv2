package dev.habla.twitter
package v2
package lookupusername

case class Request(
                    username: String,
                    bearerToken: String,
                    expansions: Option[String] = None,
                    tweetFields: Option[String] = None,
                    userFields: Option[String] = None)
