package dev.habla.twitter.v2_akka

import akka.http.scaladsl.model.HttpResponse
import akka.actor.typed.ActorSystem
import scala.concurrent.Future
import akka.http.scaladsl.model.HttpRequest
import scala.concurrent.ExecutionContext
import akka.http.scaladsl.Http
import akka.stream.Materializer

trait HttpEndpoint[Request]{
    type Response

    def from(response: HttpResponse)(implicit mat: Materializer, as: ExecutionContext): Future[Response]

    def to(request: Request): HttpRequest

    def apply(request: Request)(implicit system: ActorSystem[_], ec: ExecutionContext): Future[Response] = 
        Http()
            .singleRequest(to(request))
            .flatMap(from)
}

object HttpEndpoint{

    type Aux[Req, Res] = HttpEndpoint[Req]{type Response = Res }

    trait Syntax{

        implicit class RequestOps[Req, Res](request: Req)(implicit ep: HttpEndpoint.Aux[Req, Res]){
            def single(implicit system: ActorSystem[_], ec: ExecutionContext): Future[Res] = 
                ep(request)
        }
    }
}

