package v2_requests
package lookupt

import dev.habla.twitter._

object Run extends HttpEndpoint[v2.lookupt.Request]
    with From
    with To{
        type Response = v2.lookupt.Response
    }