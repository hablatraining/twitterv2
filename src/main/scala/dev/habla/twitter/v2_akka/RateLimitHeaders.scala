package dev.habla.twitter.v2_akka

import scala.jdk.OptionConverters._
import akka.http.scaladsl.model.HttpResponse
import akka.http.scaladsl.model.headers.ModeledCustomHeader
import akka.http.scaladsl.model.headers.ModeledCustomHeaderCompanion
import scala.util.Try

trait RateLimitHeaders{
  
    def parseRateLimitHeaders1(response: HttpResponse): Option[(Int, Long)] = 
        response.getHeader(XRateLimitReset.name).toScala.flatMap{ rateResetH =>
            Try(java.lang.Long.parseLong(rateResetH.value())).toOption.flatMap{ rateReset =>
                response.getHeader(XRateLimitRemaining.name).toScala.flatMap{ rateRemainingH =>
                    Try(Integer.parseInt(rateRemainingH.value())).toOption.map{ rateRemaining =>
                        (rateRemaining, rateReset)
                    }
                }
            }
        }

    def parseRateLimitHeaders2(response: HttpResponse): Option[(Int, Long)] = 
        response.getHeader(XRateLimitReset.name).toScala match {
            case None => None 
            case Some(rateResetH) =>
                Try(java.lang.Long.parseLong(rateResetH.value())).toOption match { 
                    case None => None
                    case Some(rateReset) =>
                        response.getHeader(XRateLimitRemaining.name).toScala match {
                            case None => None
                            case Some(rateRemainingH) =>
                                Try(Integer.parseInt(rateRemainingH.value())).toOption match {
                                    case None => None 
                                    case Some(rateRemaining) => Some((rateRemaining, rateReset))
                            }
                        }
                }
        }

    def parseRateLimitHeaders(response: HttpResponse): Option[(Int, Long)] = 
        for {
            rateResetH <- response.getHeader(XRateLimitReset.name).toScala
            rateReset <- Try(java.lang.Long.parseLong(rateResetH.value())).toOption
            rateRemainingH <- response.getHeader(XRateLimitRemaining.name).toScala
            rateRemaining <- Try(Integer.parseInt(rateRemainingH.value())).toOption
        } yield (rateRemaining, rateReset)

    final class XRateLimitReset(token: String) extends ModeledCustomHeader[XRateLimitReset] {
        override def renderInRequests = true
        override def renderInResponses = true
        override val companion = XRateLimitReset
        override def value: String = token
    }

    object XRateLimitReset extends ModeledCustomHeaderCompanion[XRateLimitReset] {
        override val name = "x-rate-limit-reset"
        override def parse(value: String) = Try(new XRateLimitReset(value))
    }

    final class XRateLimitRemaining(token: String) extends ModeledCustomHeader[XRateLimitRemaining] {
        override def renderInRequests = true
        override def renderInResponses = true
        override val companion = XRateLimitRemaining
        override def value: String = token
    }

    object XRateLimitRemaining extends ModeledCustomHeaderCompanion[XRateLimitRemaining] {
        override val name = "x-rate-limit-remaining"
        override def parse(value: String) = Try(new XRateLimitRemaining(value))
    }
}