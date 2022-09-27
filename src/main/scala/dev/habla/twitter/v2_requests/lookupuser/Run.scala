package dev.habla.twitter
package v2_requests
package lookupuser

object Run extends HttpEndpoint[v2.lookupuser.Request]
    with From
    with To{
        type Response = v2.lookupuser.Response
    }
