package v2_requests


import requests.{BaseSession, Cert, Compress, RequestBlob, Requester}
import dev.habla.twitter.v2


import javax.net.ssl.SSLContext

trait HttpEndpoint[Request]{
    /* abstract interface */

    type Response

    def from(response: requests.Response): Response

    def to(request: Request): requests.Request

    /* concrete interface */

    def apply(request: Request): Response = {
        //plantearse si poner el basesession como variable implícit
        from{
            requests.get
              .apply(to(request), RequestBlob.EmptyRequestBlob, requests.chunkedUpload)
        }
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
    implicit val lookuptEndpoint: HttpEndpoint.Aux[v2.lookupt.Request, v2.lookupt.Response] = 
        v2_requests.lookupt.Run
}

