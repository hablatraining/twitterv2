package v2

case class Meta(newest_id: Option[String], oldest_id: Option[String], result_count: Int, next_token: Option[String])

object Meta{
    import _root_.akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
    import spray.json._
    import DefaultJsonProtocol._

    implicit val metaFormat = jsonFormat4(Meta.apply)
}