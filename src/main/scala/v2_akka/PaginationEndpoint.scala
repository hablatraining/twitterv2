package dev.habla.twitter
package v2_akka

import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.time.temporal.TemporalUnit

import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.language.postfixOps
import scala.util.Failure
import scala.util.Success

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.event.Logging
import akka.http.scaladsl.Http
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.headers.BasicHttpCredentials
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Attributes
import akka.stream.scaladsl.FileIO
import akka.stream.scaladsl.Flow
import akka.stream.scaladsl.Keep
import akka.stream.scaladsl.Sink
import akka.stream.scaladsl.Source
import akka.util.ByteString
import spray.json.DefaultJsonProtocol._
import spray.json._
import akka.stream.scaladsl.FlowOpsMat

import v2._

trait PaginationEndpoint[Request] extends HttpEndpoint[Request]{

    def foldResponse[A](response: Response)(
        ok: (Int, Long, Meta) => A, 
        limit: Long => A, 
        other: => A): A

    def updateNextToken(request: Request, next: String): Request

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

    trait Syntax{

        implicit class PaginationEndpointRequestOps[Req, Res](request: Req)(implicit ep: PaginationEndpoint.Aux[Req, Res]){
            def stream(implicit system: ActorSystem[_], ec: ExecutionContext): Source[Res, NotUsed] = 
                ep.stream(request)
        }
    }
}