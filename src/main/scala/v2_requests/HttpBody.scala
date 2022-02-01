package v2_requests

import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import spray.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

trait HttpBody{

    def parseBody(response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext): Future[Either[String, JsValue]] = 
        Unmarshal(response).to[String].map(parseJson)

    def parseJson(body: String): Either[String, JsValue] = 
        Try(body.parseJson).toEither.left.map(_ => body)
}

