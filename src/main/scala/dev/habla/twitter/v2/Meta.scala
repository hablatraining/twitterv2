package dev.habla.twitter.v2

case class Meta(newest_id: Option[String], oldest_id: Option[String], result_count: Int, next_token: Option[String])

object Meta{
    import spray.json._
    import DefaultJsonProtocol._

    implicit val metaFormat: RootJsonFormat[Meta] = jsonFormat4(Meta.apply)
}