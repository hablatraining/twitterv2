package dev.habla.twitter
package v2
package lookupuserid

case class Request(
                    id: String,
                    bearerToken: String,
                    expansions: Option[String] = None,
                    tweetFields: Option[String] = None,
                    userFields: Option[String] = None)
