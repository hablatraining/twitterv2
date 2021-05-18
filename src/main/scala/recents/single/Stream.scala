package dev.habla.twitter
package v2
package recents
package single

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

object Stream{

  def apply(search: Request.SingleRequest)(implicit system: ActorSystem[_], ec: ExecutionContext): Source[SingleResponse, akka.NotUsed] = 
    Source.unfoldAsync(Option(search)){
      case state@Some(request) => 
        Run(request).map(response => Some((nextState(state, response), response)))
      case None => 
        Future.successful(None)
    }

  def nextState: (Option[Request.SingleRequest], SingleResponse) => Option[Request.SingleRequest] = {
    case (Some(request), SingleResponse.Ok(Tweets(Tweets.Body(_, _, meta),_,_))) => 
      meta.next_token.map(next => request.copy(next_token = Some(next)))
    case (state, SingleResponse.RateLimitExceeded(_)) => 
      state
    case (_, _) => 
      None
  }
}