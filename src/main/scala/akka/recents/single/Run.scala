package dev.habla.twitter
package v2
package akka
package recents
package single

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import _root_.akka.actor.typed.ActorSystem
import _root_.akka.http.scaladsl.Http
import api.recents._

object Run extends HttpEndpoint[SingleRequest] 
  with From 
  with To{
    type Response = SingleResponse
  }

