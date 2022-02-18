package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.RedditClient
import net.dean.jraw.databind.RedditModel
import net.dean.jraw.databind.UnixTime
import net.dean.jraw.references.LiveThreadReference
import net.dean.jraw.references.Referenceable
import java.io.Serializable
import java.util.Date

@RedditModel
@JsonClass(generateAdapter = true)
data class LiveThread(
    override val id: String,

    @UnixTime
    @Json(name = "created_utc")
    override val created: Date,

    val description: String,

    @Json(name = "name")
    override val fullName: String,

    /** True if the content in this thread is NSFW (not safe for work)  */
    @Json(name = "nsfw")
    val isNsfw: Boolean,

    /** One of 'live' or 'complete'  */
    val state: String,

    val title: String,

    /** The amount of people viewing the thread, or null if it's already completed  */
    @Json(name = "viewer_count")
    val viewerCount: Int?,

    /** If the viewer count is randomly skewed, or null if it's already completed  */
    @Json(name = "viewer_count_fuzzed")
    val viewerCountFuzzed: Boolean?,

    /** The `ws://` URL for new live updates, or null if it's already completed  */
    @Json(name = "websocket_url")
    val websocketUrl: String?,

    /** Any additional resources provided by the moderators of the thread  */
    val resources: String,
) : Created, Identifiable, Referenceable<LiveThreadReference>, Serializable {

    override val uniqueId: String
        get() = fullName

    override fun toReference(reddit: RedditClient): LiveThreadReference {
        return reddit.liveThread(id)
    }
}
