package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.RedditModel
import net.dean.jraw.databind.UnixTime
import java.io.Serializable
import java.util.Date

@RedditModel
@JsonClass(generateAdapter = true)
data class LiveUpdate(
    override val id: String,

    val author: String,

    val body: String,

    @UnixTime
    @Json(name = "created_utc")
    override val created: Date,

    @Json(name = "name")
    override val fullName: String,

    val embeds: List<Embed>,

    /**
     * If the update has been stricken. A stricken update appears on the website with the <strike>strikethrough</strike>
     * effect applied to its body.
     */
    @Json(name = "stricken")
    val isStricken: Boolean,
) : Created, Identifiable, Serializable {

    override val uniqueId: String get() = fullName

    /** Embedded media inside of a live update.  */
    @JsonClass(generateAdapter = true)
    data class Embed(
        val url: String,
        val width: Int?,
        val height: Int?,
    ) : Serializable
}
