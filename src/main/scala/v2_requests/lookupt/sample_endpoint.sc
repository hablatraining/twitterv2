
val token: String = os.read(os.home / "Documents" / "credentials_tw" / "bearer_token.txt").trim()
val bearer_oauth: String = s"Bearer $token"

val url: String = "https://api.twitter.com/2/tweets/search/recent"

val query_params = Map("query" -> "(from:twitterdev -is:retweet) OR #twitterdev", "tweet.fields" -> "author_id")

val r = requests.get(
  url,
  params = query_params,
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

// Hacer ahora una petición POST y cambiar dónde tengo guardado este worksheet

val post = requests.post(
  "https://api.twitter.com/1.1/statuses/update.json",
  params = Map("status" -> publicacion),
  headers = Map("Authorization" -> bearer_oauth)
)

println(post.statusCode)
println(post.headers("content-type"))