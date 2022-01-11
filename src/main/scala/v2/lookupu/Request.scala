package v2.lookupu

case class Request(
  id: String,
  bearerToken: String,
  expansions: Option[String] = None,
  tweetFields: Option[String] = None,
  userFields: Option[String] = None
)