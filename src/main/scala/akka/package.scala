package dev.habla.twitter.v2

package object akka extends HttpEndpoint.Syntax{

    implicit val lookuptEndpoint: HttpEndpoint.Aux[api.lookupt.Request, api.lookupt.Response] = akka.lookupt.Run
    implicit val recentsEndpoint: PaginationEndpoint.Aux[api.recents.SingleRequest, api.recents.SingleResponse] = akka.recents.Run

}