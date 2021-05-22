package dev.habla.twitter.v2
package akka

import _root_.akka.http.scaladsl.model.HttpResponse
import _root_.akka.actor.typed.ActorSystem
import scala.concurrent.Future
import _root_.akka.http.scaladsl.model.HttpRequest
import scala.concurrent.ExecutionContext
import _root_.akka.http.scaladsl.Http
import _root_.akka.stream.Materializer

trait HttpEndpoint[Request, Response]{

    def from(response: HttpResponse)(implicit mat: Materializer, as: ExecutionContext): Future[Response]

    def to(request: Request): HttpRequest

    def apply(request: Request)(implicit system: ActorSystem[_], ec: ExecutionContext): Future[Response] = 
        Http()
            .singleRequest(to(request))
            .flatMap(from)
}

