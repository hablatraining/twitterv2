package dev.habla.twitter
package v2_akka
package lookupusersby

import dev.habla.twitter.v2
import dev.habla.twitter.v2.lookupusersby.Request
import dev.habla.twitter.v2_akka.HttpEndpoint

object Run extends HttpEndpoint[Request]
  with From
  with To {
  type Response = v2.lookupusersby.Response
}

