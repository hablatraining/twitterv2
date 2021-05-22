package dev.habla.twitter
package v2
package akka
package recents
package pagination

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

import api.recents._

object Stream{

  def apply(search: SingleRequest)(implicit system: ActorSystem[_], ec: ExecutionContext): Source[SingleResponse, NotUsed] = 
    Source.unfoldAsync(Option(search)){
      case state@Some(request) => 
        single.Run(request).map(response => Some((nextState(state, response), response)))
      case None => 
        Future.successful(None)
    }

  def nextState: (Option[SingleRequest], SingleResponse) => Option[SingleRequest] = {
    case (Some(request), response@Tweets(Tweets.Body(_, _, meta),_,_)) => 
      meta.next_token.map(next => request.copy(next_token = Some(next)))
    case (state, RateLimitExceeded(_)) => 
      state
    case (_, _) => 
      None
  }
}