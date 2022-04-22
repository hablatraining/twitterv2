package dev.habla.twitter
package v2
package lookupuser

case class Request(
                    idOrName: Either[String, String],
                    bearerToken: String,
                    expansions: Option[String] = None,
                    tweetFields: Option[String] = None,
                    userFields: Option[String] = None)
