package dev.habla.twitter
package v2
package recents
package single

import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import akka.actor.typed.ActorSystem
import akka.http.scaladsl.Http

object Run extends api.akka.HttpEndpoint[SingleRequest, SingleResponse] 
  with From 
  with To

