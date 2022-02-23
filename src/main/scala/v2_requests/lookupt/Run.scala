package dev.habla.twitter
package v2_requests
package lookupt


import dev.habla.twitter.v2.lookupt.Request
import v2_requests.lookupt.{From, To}

object Run extends HttpEndpoint[Request]
    with From 
    with To{
        type Response = v2.lookupt.Response
    }