package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.RedditClient
import net.dean.jraw.databind.RedditModel
import net.dean.jraw.databind.UnixTime
import net.dean.jraw.models.internal.SubredditElement
import net.dean.jraw.references.MultiredditReference
import net.dean.jraw.references.Referenceable
import java.io.Serializable
import java.util.Date

@RedditModel
@JsonClass(generateAdapter = true)
data class Multireddit(
    /** If the currently logged-in user can edit this multireddit  */
    @Json(name = "can_edit")
    val isEditable: Boolean,

    /** The path this multireddit was copied from, or null if not copied  */
    @Json(name = "copied_from")
    val copiedFrom: String?,

    @UnixTime
    @Json(name = "created_utc") override
    val created: Date,

    /** Name used in the API. Usually the same thing as [.getDisplayName] unless specifically altered.  */
    @Json(name = "name")
    val codeName: String?,

    /** Markdown-formatted description  */
    @Json(name = "description_md")
    val description: String,

    /** Name displayed to the user  */
    @Json(name = "display_name")
    val displayName: String,

    /** See [MultiredditPatch.iconName]  */
    @Json(name = "icon_name")
    val iconName: String?,

    /**
     * A hex-formatted hex string, like `#CEE3F8`. This color is primarily used when viewing the multireddit on the
     * mobile site.
     */
    @Json(name = "key_color")
    val keyColor: String?,

    /** An absolute URL to an icon based on [.getIconName], if any  */
    @Json(name = "icon_url")
    val iconUrl: String?,

    /** The full multireddit path in the format of `/user/{username}/m/{multiname}`  */
    @Json(name = "path")
    val path: String,

    @Json(name = "subreddits")
    val subredditElements: List<SubredditElement>,

    /** One of `public`, `private`, or `hidden`  */
    val visibility: String,

    /** Either 'classic' or 'fresh'  */
    @Json(name = "weighting_scheme")
    val weightingScheme: String?,

    @Json(name = "user_has_favorited")
    val hasUserFavorited: Boolean?

) : Created, Referenceable<MultiredditReference>, Serializable {

    /** A list of subreddit names that this multireddit draws from  */
    val subreddits: List<String> by lazy {
        subredditElements.map { it.name }
    }

    override fun toReference(reddit: RedditClient): MultiredditReference {
        val parts = path.split("/").toTypedArray()
        // "/user/{username}/m/{name}".split("/") => ["", "user", "{username}", "m", "{name}", ""]
        return reddit.user(parts[2]).multi(parts[4])
    }
}
