package dev.habla.twitter.v2_akka.lookupusers

import dev.habla.twitter.v2
import dev.habla.twitter.v2.lookupusers.Request
import dev.habla.twitter.v2_akka.HttpEndpoint

object Run extends HttpEndpoint[Request]
  with From
  with To {
  type Response = v2.lookupusers.Response
}

