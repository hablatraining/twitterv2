package dev.habla.twitter.v2
package akka
package lookupt

import api.lookupt._

object Run extends HttpEndpoint[Request, Response]
    with From 
    with To