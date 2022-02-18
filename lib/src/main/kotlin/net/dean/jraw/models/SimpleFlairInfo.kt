package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * SimpleFlairInfo contains basic information about user's flair on a subreddit: username, flair css class, flair text.
 *
 * Used by endpoints with `modflair` scope (require moderator priveleges).
 *
 * @see net.dean.jraw.references.SubredditReference.flairList
 * @see net.dean.jraw.references.SubredditReference.patchFlairList
 * @see [Reddit API - POST /api/flaircsv](https://www.reddit.com/dev/api/.POST_api_flaircsv)
 */
@JsonClass(generateAdapter = true)
data class SimpleFlairInfo(
    /** Username  */
    val user: String,

    /** CSS class of user's flair used for custom styling of the flair, if any  */
    @Json(name = "flair_css_class")
    val cssClass: String?,

    /** Text displayed on the flair, if any  */
    @Json(name = "flair_text")
    val text: String?,
) : Serializable, UniquelyIdentifiable {

    override val uniqueId: String
        get() = hashCode().toString()

    /**
     * Convert this object into a single line in a CSV line used for setting user flairs in bulk.
     *
     * @see net.dean.jraw.references.SubredditReference.patchFlairList
     */
    fun toCsvLine(): String {
        val text = text ?: ""
        val cssClass = cssClass ?: ""
        return "$user,$text,$cssClass"
    }
}
