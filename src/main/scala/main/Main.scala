package dev.habla.twitter.v2
package main

import scala.concurrent.ExecutionContext

import _root_.akka.actor.typed.ActorSystem
import _root_.akka.actor.typed.scaladsl.Behaviors
import caseapp._
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future

import api.{recents, lookupt}

object Main extends CommandApp[Command]{

   def run(command: Command, rargs: RemainingArgs): Unit =
      command match {
         case cmd: SearchRecent => runSearchRecent(cmd)
         case cmd: LookupTweet => runLookupTweet(cmd)
      }

   def runLookupTweet(cmd: LookupTweet): Unit = withExecutionContext{
      implicit system => implicit ec => 
         akka.lookupt.Run(cmd.toLookupTweetRequest)
   }(println, _.printStackTrace)

   def runSearchRecent(search: SearchRecent): Unit = withExecutionContext{ 
      implicit system => implicit ec => search.toSearchRecentCommand match {
         case Right(singleRequest: recents.SingleRequest) => 
            akka.recents.single.Run(singleRequest)
         case Right(pagination: recents.Pagination) => 
            akka.recents.pagination.Run(pagination)
         case Left(error) => 
            Future.failed(new Exception(error))
      }
   }(println, _.printStackTrace)

   def withExecutionContext[A](
      run: ActorSystem[_] => ExecutionContext => Future[A])(
      onSuccess: A => Unit, 
      onFailure: Throwable => Unit
   ): Unit = {
      val system = ActorSystem(Behaviors.empty, "TwitterV2")
      implicit val ec = system.executionContext
      run(system)(ec).onComplete{
         case Success(value) => onSuccess(value); system.terminate
         case Failure(exception) => onFailure(exception); system.terminate
      }
   }
}

