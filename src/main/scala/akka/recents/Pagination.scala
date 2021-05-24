package dev.habla.twitter
package v2
package akka
package recents

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

import api.Meta, api.recents._

object RunPagination{
  
  def stream(request: SingleRequest)(implicit system: ActorSystem[_], ec: ExecutionContext): Source[Tweets, Future[PaginatedResponse]] = 
    Run.stream(request)
      .updateResult
      .throttlePipeline
      .collect{ case tweets: Tweets => tweets }
  
  def apply(cmd: Pagination)(implicit system: ActorSystem[_], ec: ExecutionContext): Future[PaginatedResponse] = 
    Run.stream(cmd.request)
      .take(cmd.max, cmd.request.max_results)
      .updateResult
      .logPipeline
      .throttlePipeline
      .collect{ case tweets: Tweets => tweets }
      .alsoTo(storeTweetIncludes(cmd.file_name))
      .toMat(storeTweetData(cmd.file_name))(Keep.left)
      .run
  
  implicit class PipelineOps[A](source: Source[SingleResponse, A])(implicit system: ActorSystem[_], ec: ExecutionContext){

    def take(max_total: Option[Long], max_results: Option[Int]): Source[SingleResponse, A] = {
      val batches: Long = math.ceil(max_total.getOrElse(10000000L)/max_results.map(i => i: Double).getOrElse(10.0)).toLong
      source.take(batches)
    }

    object RateLimitReached{
      def waitingTime(rateReset: Long): FiniteDuration = 
        (rateReset*1000L - System.currentTimeMillis() + 3000L).milliseconds

      def unapply(response: SingleResponse): Option[FiniteDuration] = response match {
        case tweets: Tweets => 
          if (tweets.rateRemaining > 0) None
          else Some(waitingTime(tweets.rateReset))
        case RateLimitExceeded(rateReset) => 
          Some(waitingTime(rateReset))
        case _ => 
          None
      }
    }

    def throttlePipeline: Source[SingleResponse, A] = 
      source.flatMapConcat{
        case response@ RateLimitReached(d) => 
          Source.single(response).delay(d)
        case response => 
          Source.single(response)
      }

    def updateResult: Source[SingleResponse, Future[PaginatedResponse]] = 
      source.alsoToMat(Sink.fold((None: Option[Meta], None: Option[ErroneousSingleResponse])){
        case ((None, _), tweets: Tweets) => 
          (Some(tweets.body.meta), None)
        case (state, RateLimitExceeded(delay)) => 
          state
        case ((Some(Meta(ini, _, count, _)),error), tweets@Tweets(Tweets.Body(_, _, meta@Meta(_, last, count2, nextToken)), remaining, reset)) => 
          (Some(Meta(ini, last, count+count2, nextToken)), error)
        case ((meta, _), error: ErroneousSingleResponse) => 
          (meta, Some(error))
      })( (l, r) => r.map {
        case (meta, Some(error)) => PaginatedResponse.Error(error, meta)
        case (meta, _) => PaginatedResponse.Ok(meta)
      })

    def logPipeline: Source[SingleResponse, A] = 
      source.alsoToMat(Sink.fold(true){
        case (_, RateLimitExceeded(delay)) => 
          system.log.info(s"Rate limit exceeded: waiting ${RateLimitReached.waitingTime(delay)} for next batch")
          true

        case (isFirst, tweets@Tweets(Tweets.Body(_,_,meta), remaining, reset)) => 
          if (isFirst) system.log.info(s"Received first response in batch:\n$meta; $remaining; $reset")
          else system.log.debug(s"Received response:\n$meta; $remaining; $reset")
          val rateLimit = RateLimitReached.unapply(tweets)
          rateLimit.foreach{ delay => 
            system.log.info(s"Waiting $delay for next batch")
          }
          rateLimit.isDefined

        case (state, response: ErroneousSingleResponse) => 
          val error = response match { 
            case ErroneousJsonSingleResponse(jsValue) => jsValue.prettyPrint
            case ErroneousTextSingleResponse(text) => text 
          }
          system.log.error("Error found:\n" + error)
          state
      })(Keep.left)
  }


  def storeTweetData(fileName: String): Sink[Tweets, NotUsed] =
    Flow[Tweets]
      .mapConcat( _.body.data.getOrElse(List())
          .map(tweet => ByteString(tweet.toString+"\n"))
      ).to(FileIO.toPath(Paths.get(fileName+".json")))

  def storeTweetIncludes(fileName: String): Sink[Tweets, NotUsed] =
    Flow[Tweets]
      .mapConcat( _.body.includes.map(_.places.map(place => 
        ByteString(place.toString+"\n"))).getOrElse(List()))
      .to(FileIO.toPath(Paths.get(fileName+".places.json")))

}