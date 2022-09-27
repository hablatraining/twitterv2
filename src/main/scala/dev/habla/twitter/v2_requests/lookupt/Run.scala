package dev.habla.twitter
package v2_requests
package lookupt

object Run extends HttpEndpoint[v2.lookupt.Request]
    with From
    with To{
        type Response = v2.lookupt.Response
    }