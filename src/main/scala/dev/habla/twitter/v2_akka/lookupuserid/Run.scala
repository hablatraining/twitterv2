package dev.habla.twitter
package v2_akka
package lookupuserid

import dev.habla.twitter.v2
import dev.habla.twitter.v2.lookupuserid.Request
import dev.habla.twitter.v2_akka.HttpEndpoint

object Run extends HttpEndpoint[Request]
  with From
  with To {
  type Response = v2.lookupuserid.Response
}

