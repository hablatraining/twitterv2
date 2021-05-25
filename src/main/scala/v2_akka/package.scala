package dev.habla.twitter

package object v2_akka extends HttpEndpoint.Syntax 
    with PaginationEndpoint.Syntax{

    implicit val lookuptEndpoint: HttpEndpoint.Aux[v2.lookupt.Request, v2.lookupt.Response] = v2_akka.lookupt.Run
    implicit val recentsEndpoint: PaginationEndpoint.Aux[v2.recents.SingleRequest, v2.recents.SingleResponse] = v2_akka.recents.Run

    println(1)
}