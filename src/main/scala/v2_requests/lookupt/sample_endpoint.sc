import requests.{Request, RequestBlob, Response}
import requests._

import java.net.HttpCookie
import javax.net.ssl.SSLContext
import scala.collection.mutable

val token: String = os.read(os.home / "Documents" / "credentials_tw" / "bearer_token.txt").trim()
val bearer_oauth: String = s"Bearer $token"

// CASO DE USO DEL ENDPOINT RECENTS

val url: String = "https://api.twitter.com/2/tweets/search/recent"

val queryParams = Map("query" -> "from:Escapatrisia", "tweet.fields" -> "author_id")

val r: Response = requests.get(
  url,
  params = queryParams,
  headers = Map("Authorization" -> bearer_oauth)
)

println(r.statusCode)
println(r.headers("content-type"))

r.headers("x-rate-limit-reset")
println("Hola me he descargao tos tus tuits xDDD")
r.headers("x-rate-limit-remaining")

r.headers.foreach{
  kv => println(kv._1)
}
r.

val data = ujson.read(r.text())

val data_tweets : ujson.Value = ujson.read(ujson.write(data("data")))

println(data_tweets(0).render(indent = 4))

//data_tweets(0).asInstanceOf[Map[String, String]].keys
data_tweets(0).obj.keys
data_tweets.arr.size
//Escribir el json de ejemplo

//os.write(os.home / "Documents" / "out.json", data_tweets)

println(data.render(indent = 4))

//Función que hace mezcla de los textos de los tuits recopilados

def mix_text(v: ujson.Value) : Iterable[String] = {
  v match {
    case s : ujson.Str => Seq(s.str.split(" ")(0))
    case a : ujson.Arr => a.arr.map(mix_text).flatten
    case o : ujson.Obj => o.obj.values.map(mix_text).flatten
    case _ => Nil
  }
}

val publicacion : String = mix_text(data_tweets).mkString("_")


// CASO DE USO DEL ENDPOINT LOOKUPU
// Primero buscando múltiples por id: GET /2/users

val url: String = "https://api.twitter.com/2/users"
val query_params_users = Map("ids" -> "61879392,2244994945")

val r = requests.get(
  url,
  params = query_params_users,
  headers = Map("Authorization" -> bearer_oauth)
)
val data = ujson.read(r.text())

val data_tweets : ujson.Value = ujson.read(ujson.write(data("data")))

def loop(current: Int): Unit = {
  if (current >= 0) {
    println(data_tweets(current).render(indent = 4))
    loop(current - 1)
  }
}

//Ahora buscando varios por username

val url: String = "https://api.twitter.com/2/users"
val queryParamsMultipleUsers : Map[String, String] = Map("usernames" -> "EvilAFM,Twitterdev,mangelrogel")

val r = requests.get(
  url,
  params = query_params_users,
  headers = Map("Authorization" -> bearer_oauth)
)
val data = ujson.read(r.text())

val dataTweets : ujson.Value = ujson.read(ujson.write(data("data")))
loop(1)

//Hacer caso de uso utilizando el Requester y no el método get

val mi_request: Request = requests.Request(url,
  headers = Map("Authorization" -> bearer_oauth),
  params = query_params_users)

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

val mi_requester = requests.Requester("GET", MySession)

val mi_response : Response = mi_requester.apply(mi_request, RequestBlob.EmptyRequestBlob, requests.chunkedUpload)

val data = ujson.read(mi_response.text())

val dataTweets : ujson.Value = ujson.read(ujson.write(data("data")))
loop(1)


//val mi_response: Response = Requester()

//val my_string: Set[String] = Requester.officialHttpMethods
//val valor = BaseSession
//Requester("GET", BaseSession)
//val data = ujson.read(r.text())

//val dataTweets : ujson.Value = ujson.read(ujson.write(data("data")))
//loop(1)