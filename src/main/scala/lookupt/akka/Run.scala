package dev.habla.twitter.v2
package lookupt
package akka

object Run extends api.akka.HttpEndpoint[Request, Response]
    with From 
    with To