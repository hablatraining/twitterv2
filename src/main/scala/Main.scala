package dev.habla.twitter.v2

import scala.concurrent.ExecutionContext
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import dev.habla.twitter.v2.recents._
import scala.util.Success
import scala.util.Failure
import scala.concurrent.Future


object Main extends App{

   // TODO: replace caseapp
   runSearchRecent(Command.SearchRecent(query="scala3", bearer_token="A"))
   
   def runSearchRecent(search: Command.SearchRecent): Unit = withExecutionContext{ 
      implicit system => implicit ec => search.toSearchRecentCommand match {
         case Right(singleRequest: Request.SingleRequest) => 
            recents.single.Run(singleRequest)
         case Right(pagination: Request.Pagination) => 
            recents.pagination.Run(pagination)
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

