package dev.habla.twitter
package v2_requests


import requests.{BaseSession, Cert, Compress, RequestBlob, Requester}

import javax.net.ssl.SSLContext

trait HttpEndpoint[Request]{
    /* abstract interface */

    type Response

    def from(response: requests.Response): Response

    def to(request: Request): requests.Request

    /* concrete interface */

    def apply(request: Request): Response = {
        //plantearse si poner el basesession como variable implícita
        //esto solo vale para peticiones GET, a lo mejor se puede hacer más general
        from{
            requests.Requester("GET", HttpEndpoint.MySession)
              .apply(to(request), RequestBlob.EmptyRequestBlob, HttpEndpoint.MySession.chunkedUpload)
        }
    }
}

object HttpEndpoint{
    type Aux[Req, Res] = HttpEndpoint[Req]{type Response = Res }

    object MySession extends BaseSession {
        def cookies = requests.cookies

        val headers = requests.headers

        def auth = requests.auth

        def proxy = requests.proxy

        def cert: Cert = requests.cert

        def sslContext: SSLContext = requests.sslContext

        def maxRedirects: Int = requests.maxRedirects

        def persistCookies = requests.persistCookies

        def readTimeout: Int = requests.readTimeout

        def connectTimeout: Int = requests.connectTimeout

        def verifySslCerts: Boolean = requests.verifySslCerts

        def autoDecompress: Boolean = requests.autoDecompress

        def compress: Compress = requests.compress

        def chunkedUpload: Boolean = requests.chunkedUpload

        def check: Boolean = requests.check
    }
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

