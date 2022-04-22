package dev.habla.twitter
package v2
package lookupusers

case class Request(
                    idsOrNames: Either[List[String],List[String]],
                    bearerToken: String,
                    expansions: Option[String] = None,
                    tweetFields: Option[String] = None,
                    userFields: Option[String] = None)
