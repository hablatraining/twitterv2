package dev.habla.twitter
package v2_requests


import requests.RequestBlob
import v2._

trait HttpEndpoint[Request]{
    /* abstract interface */

    type Response

    def from(response: requests.Response): Response

    def to(request: Request): requests.Request

    /* concrete interface */

    def apply(request: Request): Response =
        from{
            requests.get
              .apply(to(request), RequestBlob.EmptyRequestBlob, requests.chunkedUpload)
        }
}

object HttpEndpoint{
    type Aux[Req, Res] = HttpEndpoint[Req]{type Response = Res }
}

trait HttpEndpointSyntax{

    implicit class HttpEndpointRequestOps[Req, Res](request: Req)(implicit ep: HttpEndpoint.Aux[Req, Res]){
        def single: Res =
            ep.apply(request)
    }
}

trait HttpEndpointInstances{
    implicit val lookuptEndpoint: HttpEndpoint.Aux[lookupt.Request, lookupt.Response] =
        v2_requests.lookupt.Run

    implicit val lookupuserEndpoint: HttpEndpoint.Aux[lookupuser.Request, lookupuser.Response] =
        v2_requests.lookupuser.Run
}

