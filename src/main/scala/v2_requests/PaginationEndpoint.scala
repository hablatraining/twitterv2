package v2_requests

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.stream.scaladsl.Source

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

trait PaginationEndpoint[Request] extends HttpEndpoint[Request]{

    /* abstract interface */
    
    def foldResponse[A](response: Response)(
        ok: (Int, Long, Meta) => A, 
        limit: Long => A, 
        other: => A): A

    def updateNextToken(request: Request, next: String): Request

    /* concrete interface */

    def stream(search: Request)(implicit system: ActorSystem[_], ec: ExecutionContext): Source[Response, NotUsed] = 
        Source.unfoldAsync(Option(search)){
            case state@Some(request) => 
                apply(request).map(response => Some((nextState(state, response), response)))
            case None => 
                Future.successful(None)
        }.throttlePipeline

    def nextState(state: Option[Request], response: Response): Option[Request] =
        state.fold(Option.empty[Request]){ request => 
            foldResponse(response)(
                (_, _, meta) => meta.next_token.map(next => updateNextToken(request, next)),
                _ => Some(request),
                None)
        }
    
    implicit class PipelineOps[A](source: Source[Response, A])(implicit system: ActorSystem[_], ec: ExecutionContext){

        object RateLimitReached{
            def waitingTime(rateReset: Long): FiniteDuration = 
                (rateReset*1000L - System.currentTimeMillis() + 3000L).milliseconds

            def unapply(response: Response): Option[FiniteDuration] = foldResponse(response)(
                (remaining, resetTime, meta)  => 
                if (remaining > 0) None
                else Some(waitingTime(resetTime)),
                resetTime => Some(waitingTime(resetTime)),
                None
            )
        }

        def throttlePipeline: Source[Response, A] = 
            source.flatMapConcat{
                case response@ RateLimitReached(d) => 
                Source.single(response).delay(d)
                case response => 
                Source.single(response)
            }
    }

}

object PaginationEndpoint{
    type Aux[Req, Res] = PaginationEndpoint[Req]{ type Response = Res }
}

trait PaginationEndpointSyntax{
    implicit class PaginationEndpointRequestOps[Req, Res](request: Req)(implicit ep: PaginationEndpoint.Aux[Req, Res]){
        def stream(implicit system: ActorSystem[_], ec: ExecutionContext): Source[Res, NotUsed] = 
            ep.stream(request)
    }
}

trait PaginationEndpointInstances{
    implicit val recentsEndpoint: PaginationEndpoint.Aux[v2.recents.SingleRequest, v2.recents.SingleResponse] = v2_akka.recents.Run
}

