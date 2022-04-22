package dev.habla.twitter
package v2_akka
package lookupuser

import dev.habla.twitter.v2.lookupuser.Request

object Run extends HttpEndpoint[Request]
  with From
  with To {
  type Response = v2.lookupuser.Response
}

