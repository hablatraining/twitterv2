


val token: String = os.read(os.home / "Documents" / "credentials_tw" / "bearer_token.txt").trim()
val bearer_oauth: String = s"Bearer $token"

// CASO DE USO DEL ENDPOINT RECENTS

val url: String = "https://api.twitter.com/2/tweets/search/recent"

val queryParams = Map("query" -> "(from:EvilAFM -is:retweet) OR #EvilAFM", "tweet.fields" -> "author_id")

val r = requests.get(
  url,
  params = queryParams,
  headers = Map("Authorization" -> bearer_oauth)
)

println(r.statusCode)
println(r.headers("content-type"))

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
