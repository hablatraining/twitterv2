package dev.habla.twitter
package main

import caseapp._

import scala.util.Try

object MainLi extends CommandApp[Command] {


  def run(command: Command, rargs: RemainingArgs): Unit =
    command match {
      case cmd: LookupTweet => runLookupTweet(cmd)
      case cmd: LookupUser => runLookupUser(cmd)
      case _ => ()
    }

  def runLookupTweet(cmd: LookupTweet): Unit =
    Try(v2_requests.lookupt.Run(cmd.toLookupTweetRequest))
      .fold(msg => println(msg.getMessage), println)

  def runLookupUser(cmd: LookupUser): Unit = {
    cmd.toLookupUserRequest.left.map(new Exception(_)).toTry
      .flatMap(req => Try(v2_requests.lookupuser.Run(req)))
      .fold(msg => println(msg.getMessage), println)
  }
}
