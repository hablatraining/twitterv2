package dev.habla.twitter
package v2
package akka

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

import _root_.akka.NotUsed
import _root_.akka.actor.typed.ActorSystem
import _root_.akka.actor.typed.scaladsl.Behaviors
import _root_.akka.event.Logging
import _root_.akka.http.scaladsl.Http
import _root_.akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import _root_.akka.http.scaladsl.model._
import _root_.akka.http.scaladsl.model.headers.Authorization
import _root_.akka.http.scaladsl.model.headers.BasicHttpCredentials
import _root_.akka.http.scaladsl.model.headers.OAuth2BearerToken
import _root_.akka.http.scaladsl.unmarshalling.Unmarshal
import _root_.akka.stream.Attributes
import _root_.akka.stream.scaladsl.FileIO
import _root_.akka.stream.scaladsl.Flow
import _root_.akka.stream.scaladsl.Keep
import _root_.akka.stream.scaladsl.Sink
import _root_.akka.stream.scaladsl.Source
import _root_.akka.util.ByteString
import spray.json.DefaultJsonProtocol._
import spray.json._
import _root_.akka.stream.scaladsl.FlowOpsMat

import api._

trait PaginationEndpoint[Request] extends HttpEndpoint[Request]{

    def foldResponse[A](response: Response)(ok: (Int, Long, Meta) => A, limit: Long => A, other: => A): A

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
}