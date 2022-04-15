package dev.habla.twitter
package v2
package lookupusers

case class Request(
                    ids: String,
                    bearerToken: String,
                    expansions: Option[String] = None,
                    tweetFields: Option[String] = None,
                    userFields: Option[String] = None)
