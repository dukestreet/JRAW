package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.RedditModel
import java.io.Serializable

@RedditModel
@JsonClass(generateAdapter = true)
data class KarmaBySubreddit(
    @Json(name = "sr")
    val subreddit: String?,

    @Json(name = "comment_karma")
    val commentKarma: Int,

    @Json(name = "link_karma")
    val linkKarma: Int,
) : Serializable
