package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class SubredditSearchResult(
    /** The approximate number of active users right now  */
    @Json(name = "active_user_count")
    val activeUserCount: Int,

    /** A full URL that points to the subreddit's icon  */
    @Json(name = "icon_img")
    val iconUrl: String?,

    /** A hex color with the "#" included, or an empty string  */
    @Json(name = "key_color")
    val keyColor: String?,

    /** The subreddit's display name, e.g. "RocketLeague"  */
    @Json(name = "name")
    val name: String?,

    @Json(name = "subscriber_count")
    val subscriberCount: Int,

    @Json(name = "allow_images")
    val isAllowImages: Boolean,
) : Serializable
