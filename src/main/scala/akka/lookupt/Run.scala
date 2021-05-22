package dev.habla.twitter.v2
package akka
package lookupt

import api.lookupt._

object Run extends HttpEndpoint[Request]
    with From 
    with To{
        type Response = api.lookupt.Response
    }