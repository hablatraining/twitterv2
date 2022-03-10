package v2_requests

//Esto de usar spray habría que cambiarlo
import spray.json._
import scala.util.Try

trait HttpBody{

    def parseBody(response: requests.Response): Either[String, JsValue] = {
        //Unmarshal(response).to[String].map(parseJson)
        parseJson(response.text())
    }

    def parseJson(body: String): Either[String, JsValue] = 
        Try(body.parseJson).toEither.left.map(_ => body)
}
