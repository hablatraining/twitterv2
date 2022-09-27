package dev.habla.twitter
package v2_requests

import spray.json._
import scala.util.Try

trait HttpBody{

    def parseBody(response: requests.Response): Either[String, JsValue] = {
        parseJson(response.text())
    }

    def parseJson(body: String): Either[String, JsValue] = 
        Try(body.parseJson).toEither.left.map(_ => body)
}
