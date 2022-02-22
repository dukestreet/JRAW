package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.RedditClient
import net.dean.jraw.databind.RedditModel
import net.dean.jraw.databind.UnixTime
import net.dean.jraw.references.Referenceable
import net.dean.jraw.references.UserReference
import java.io.Serializable
import java.util.Date
import java.util.concurrent.TimeUnit

@RedditModel
@JsonClass(generateAdapter = true)
data class Account(
    /** The amount of Karma this user has acquired through comment  */
    @Json(name = "comment_karma")
    val commentKarma: Int,

    @Json(name = "created_utc")
    @UnixTime(precision = TimeUnit.SECONDS)
    override val created: Date,

    /** If the currently logged in user is friends with this account  */
    @Json(name = "is_friend")
    val isFriend: Boolean,

    @Json(name = "is_blocked")
    val isBlocked: Boolean,

    /** If this user is a moderator  */
    @Json(name = "is_mod")
    val isModerator: Boolean,

    /** If this property is true, the user has reddit Gold  */
    @Json(name = "is_gold")
    val isGoldMember: Boolean,

    /** True if this user has verified ownership of the email address used to create their account  */
    @Json(name = "has_subscribed")
    val hasSubscribed: Boolean,

    /** True if this user has verified ownership of the email address used to create their account. May be null.  */
    @Json(name = "has_verified_email")
    val hasVerifiedEmail: Boolean?,

    /** The amount of karma gained from submitting links  */
    @Json(name = "link_karma")
    val linkKarma: Int,

    /** The name chosen for this account by a real person  */
    @Json(name = "name")
    val name: String,

    /** URL to user's avatar  */
    @Json(name = "icon_img")
    val icon: String,

    @Json(name = "subreddit")
    val profile: Profile?,

    // TODO: a lot more properties for logged-in users (see /api/v1/me)

) : Created, Referenceable<UserReference<*>>, Serializable, UniquelyIdentifiable {

    override val uniqueId: String
        get() = name

    override fun toReference(reddit: RedditClient): UserReference<*> {
        return reddit.user(name)
    }

    @JsonClass(generateAdapter = true)
    data class Profile(
        @Json(name = "title")
        val displayName: String?,

        @Json(name = "public_description")
        val about: String?,
    )
}
