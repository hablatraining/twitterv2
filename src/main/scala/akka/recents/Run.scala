package dev.habla.twitter
package v2
package akka
package recents

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import _root_.akka.actor.typed.ActorSystem
import _root_.akka.http.scaladsl.Http
import api._, api.recents._

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

