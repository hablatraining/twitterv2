package dev.habla.twitter
package v2_akka
package lookupt

import v2.lookupt._

object Run extends HttpEndpoint[Request]
    with From 
    with To{
        type Response = v2.lookupt.Response
    }

