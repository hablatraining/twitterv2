package dev.habla.twitter
package v2_akka
package recents

import akka.http.scaladsl.model.HttpRequest
import akka.http.scaladsl.model.headers.Authorization
import akka.http.scaladsl.model.headers.OAuth2BearerToken
import akka.http.scaladsl.model.Uri
import v2.recents._

trait To extends QueryParams{
        
    def to(search: SingleRequest): HttpRequest = {
        val tweet_fields: String = "attachments,author_id,context_annotations,conversation_id,created_at,entities,geo,id,in_reply_to_user_id,lang,public_metrics,possibly_sensitive,referenced_tweets,reply_settings,source,text,withheld"
        val oauthHeader = Authorization(OAuth2BearerToken(search.bearer_token))

        // for expansions
        // val media_fields: String = "duration_ms,height,media_key,preview_image_url,public_metrics,type,url,width"
        // val place_fields: String = "contained_within,country,country_code,full_name,geo,id,name,place_type"
        // val poll_fields: String = "duration_minutes,end_datetime,id,options,voting_status"
        // val user_fields: String = "created_at,description,entities,id,location,name,pinned_tweet_id,profile_image_url,protected,public_metrics,url,username,verified,withheld"

        HttpRequest(
        uri = Uri("https://api.twitter.com/2/tweets/search/recent").withQuery(Uri.Query.apply(
                    Map[String, String]()
                        .add("query", search.query)
                        .add("next_token", search.next_token)
                        .add("since_id", search.since_id)
                        .add("max_results", search.max_results.map(_.toString))
                        .add("until_id", search.until_id)
                        .add("start_time", search.start_time)
                        .add("end_time", search.end_time)
                        .add("tweet.fields", search.tweet_fields.getOrElse(tweet_fields))
                        .add("expansions", "geo.place_id")
                        .add("place.fields", "contained_within,country,country_code,full_name,geo,id,name,place_type"))),
        headers = scala.collection.immutable.Seq(oauthHeader)
        )
    }  
}