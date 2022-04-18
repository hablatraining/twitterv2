package dev.habla.twitter
package v2
package lookupusersby

case class Request(
                    usernames: String,
                    bearerToken: String,
                    expansions: Option[String] = None,
                    tweetFields: Option[String] = None,
                    userFields: Option[String] = None)
