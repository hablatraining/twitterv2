package v2_requests

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.Materializer

import scala.concurrent.{ExecutionContext, Future}

trait HttpEndpoint[Request]{
    /* abstract interface */

    type Response

    def from(response: HttpResponse)(implicit mat: Materializer, as: ExecutionContext): Future[Response]

    def to(request: Request): HttpRequest

    /* concrete interface */

    def apply(request: Request)(implicit system: ActorSystem[_], ec: ExecutionContext): Future[Response] = 
        Http().singleRequest(to(request))
            .flatMap(from)
}

object HttpEndpoint{
    type Aux[Req, Res] = HttpEndpoint[Req]{type Response = Res }
}

trait HttpEndpointSyntax{

    implicit class HttpEndpointRequestOps[Req, Res](request: Req)(implicit ep: HttpEndpoint.Aux[Req, Res]){
        def single(implicit system: ActorSystem[_], ec: ExecutionContext): Future[Res] = 
            ep.apply(request)
    }
}

trait HttpEndpointInstances{
    implicit val lookuptEndpoint: HttpEndpoint.Aux[v2.lookupt.Request, v2.lookupt.Response] = 
        v2_akka.lookupt.Run
}

