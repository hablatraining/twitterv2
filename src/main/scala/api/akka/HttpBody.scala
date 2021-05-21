package dev.habla.twitter.v2
package api
package akka

import _root_.akka.http.scaladsl.model.HttpResponse
import _root_.akka.stream.Materializer
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import _root_.akka.http.scaladsl.unmarshalling.Unmarshal
import scala.util.Try
import spray.json._, DefaultJsonProtocol._

trait HttpBody{

    def parseBody(response: HttpResponse)(implicit mat: Materializer, ec: ExecutionContext): Future[Either[String, JsValue]] = 
        Unmarshal(response).to[String].map(parseJson)

    def parseJson(body: String): Either[String, JsValue] = 
        Try(body.parseJson).toEither.left.map(_ => body)
}

