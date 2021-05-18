package dev.habla.twitter
package v2
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

object Run{

  def apply(cmd: Request.Pagination)(implicit system: ActorSystem[_], ec: ExecutionContext): Future[PaginatedResponse] = 
    single.Stream(cmd.request)
      .take(cmd.max, cmd.request.max_results)
      .updateResult
      .logPipeline
      .throttlePipeline
      .collect{ case SingleResponse.Ok(tweets) => tweets }
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
        case SingleResponse.Ok(tweets) => 
          if (tweets.rateRemaining > 0) None
          else Some(waitingTime(tweets.rateReset))
        case SingleResponse.RateLimitExceeded(rateReset) => 
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
      source.alsoToMat(Sink.fold((None: Option[Tweets.Meta], None: Option[ErroneousSingleResponse])){
        case ((None, _), SingleResponse.Ok(tweets)) => 
          (Some(tweets.body.meta), None)
        case (state, SingleResponse.RateLimitExceeded(delay)) => 
          state
        case ((Some(Tweets.Meta(ini, _, count, _)),error), SingleResponse.Ok(tweets@Tweets(Tweets.Body(_, _, meta@Tweets.Meta(_, last, count2, nextToken)), remaining, reset))) => 
          (Some(Tweets.Meta(ini, last, count+count2, nextToken)), error)
        case ((meta, _), SingleResponse.Error(error)) => 
          (meta, Some(error))
      })( (l, r) => r.map {
        case (meta, Some(error)) => PaginatedResponse.Error(error, meta)
        case (meta, _) => PaginatedResponse.Ok(meta)
      })

    def logPipeline: Source[SingleResponse, A] = 
      source.alsoToMat(Sink.fold(true){
        case (_, SingleResponse.RateLimitExceeded(delay)) => 
          system.log.info(s"Rate limit exceeded: waiting ${RateLimitReached.waitingTime(delay)} for next batch")
          true

        case (isFirst, tweets@SingleResponse.Ok(Tweets(Tweets.Body(_,_,meta), remaining, reset))) => 
          if (isFirst) system.log.info(s"Received first response in batch:\n$meta; $remaining; $reset")
          else system.log.debug(s"Received response:\n$meta; $remaining; $reset")
          val rateLimit = RateLimitReached.unapply(tweets)
          rateLimit.foreach{ delay => 
            system.log.info(s"Waiting $delay for next batch")
          }
          rateLimit.isDefined

        case (state, SingleResponse.Error(response)) => 
          val error = response match { 
            case ErroneousSingleResponse.Json(jsValue) => jsValue.prettyPrint
            case ErroneousSingleResponse.Text(text) => text 
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