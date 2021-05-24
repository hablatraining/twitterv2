package dev.habla.twitter
package v2_akka
package recents

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http
import v2._, v2.recents._

object Run extends PaginationEndpoint[SingleRequest] 
  with From 
  with To{
    type Response = SingleResponse

    def foldResponse[A](response: SingleResponse)(ok: (Int, Long, Meta) => A, limit: Long => A, other: => A): A = 
      response match {
        case Tweets(Tweets.Body(_,_,meta), remaining, resetTime) => ok(remaining, resetTime, meta)
        case RateLimitExceeded(resetTime) => limit(resetTime)
        case _ => other
      }

    def updateNextToken(request: SingleRequest, next: String): SingleRequest = 
      request.copy(next_token = Some(next))

  }

