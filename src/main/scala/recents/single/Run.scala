package dev.habla.twitter
package v2
package recents
package single

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http

object Run{

  def apply(search: SingleRequest)(implicit system: ActorSystem[_]): Future[SingleResponse] = {
    implicit val ec: ExecutionContext = system.executionContext
    Http().singleRequest(http.To(search))
      .flatMap(http.From.apply)
  }
}