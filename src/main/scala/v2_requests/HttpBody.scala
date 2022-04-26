package v2_requests


//import ujson._
import spray.json._
import scala.util.Try

trait HttpBody{

    def parseBody(response: requests.Response): Either[String, JsValue] = {
        //Unmarshal(response).to[String].map(parseJson) así estaba hecho con akka (usando Futuros)

        parseJson(response.text()) //Así estará hecho también cuando no se use spray

    }

    def parseJson(body: String): Either[String, JsValue] = {

        Try(body.parseJson).toEither.left.map(_ => body)

        // Try(read(body)).toEither.left.map(_ => body) así estará hecho cuando no se use spray
    }

}
