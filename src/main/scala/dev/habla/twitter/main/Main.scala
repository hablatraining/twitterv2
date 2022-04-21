package dev.habla.twitter
package v2
package main

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import _root_.akka.actor.typed.ActorSystem
import _root_.akka.actor.typed.scaladsl.Behaviors
import caseapp._

import scala.util.Success
import scala.util.Failure

object Main extends CommandApp[Command]{




   def run(command: Command, rargs: RemainingArgs): Unit =
      command match {
         case cmd: SearchRecent => runSearchRecent(cmd)
         case cmd: LookupTweet => runLookupTweet(cmd)
         case cmd: LookupUser => runLookupUser(cmd)
         case cmd: LookupUsers => runLookupUsers2(cmd)
      }

   def runLookupTweet(cmd: LookupTweet): Unit = withExecutionContext{
      implicit system => implicit ec => 
         v2_akka.lookupt.Run(cmd.toLookupTweetRequest)
   }(println, _.printStackTrace)

   def runSearchRecent(search: SearchRecent): Unit = withExecutionContext{ 
      implicit system => implicit ec => search.toSearchRecentCommand match {
         case Right(singleRequest: recents.SingleRequest) => 
            v2_akka.recents.Run(singleRequest)
         case Right(pagination: recents.Pagination) => 
            v2_akka.recents.RunPagination(pagination)
         case Left(error) => 
            Future.failed(new Exception(error))
      }
   }(println, _.printStackTrace)


   def runLookupUser(cmd: LookupUser): Unit = withExecutionContext {
      implicit system =>
         implicit ec =>
            v2_akka.lookupuser.Run(cmd.toLookupUserRequest)
   }(println, _.printStackTrace)


   def runLookupUsers2(cmd: LookupUsers): Unit = withExecutionContext {
      implicit system =>
         implicit ec =>
            v2_akka.lookupusers.Run(cmd.toLookupUsersRequest)
   }(println, _.printStackTrace)


   def withExecutionContext[A](
      run: ActorSystem[_] => ExecutionContext => Future[A])(
      onSuccess: A => Unit, 
      onFailure: Throwable => Unit
   ): Unit = {
      val system = ActorSystem(Behaviors.empty, "TwitterV2")
      implicit val ec: ExecutionContextExecutor = system.executionContext
      run(system)(ec).onComplete{
         case Success(value) => onSuccess(value); system.terminate
         case Failure(exception) => onFailure(exception); system.terminate
      }
   }
}

