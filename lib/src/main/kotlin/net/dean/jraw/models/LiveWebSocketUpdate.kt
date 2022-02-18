package net.dean.jraw.models

import com.google.auto.value.AutoValue
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.models.LiveUpdate.Embed
import net.dean.jraw.models.LiveWebSocketUpdate.Activity
import net.dean.jraw.models.LiveWebSocketUpdate.EmbedsReady
import net.dean.jraw.models.LiveWebSocketUpdate.Settings
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class LiveWebSocketUpdate(
    /**
     *  -  `update` — a new update has been posted in the thread. the payload is a [LiveUpdate].
     *  -  `activity` — periodic update on the viewer count, the payload is an [Activity].
     *  -  `settings` — change in the thread's settings, the payload is a [Settings].
     *  -  `delete` — an update has been deleted (removed from the thread), the payload is the ID of the deleted update
     *  -  `strike` — an update has been stricken, the payload is the ID of the stricken update
     *  -  `embeds_ready` — a previously posted update has been parsed and embedded media has been found in it. The payload is an [EmbedsReady]
     *  -  `complete` — the thread has been marked as complete, no further updates will be sent. No payload.
     */
    val type: String,
    val payload: Any,
) : Serializable {

    @JsonClass(generateAdapter = true)
    data class Settings(
        val description: String?,
        val title: String?,
        @Json(name = "nsfw") val isNsfw: Boolean,
        val resources: String?,
    )

    @JsonClass(generateAdapter = true)
    data class EmbedsReady(
        /** Basic information about each of the embedded links present in the update  */
        @Json(name = "media_embeds")
        val embeds: List<Embed>,

        /**
         * Metadata provided from an external website (e.g. YouTube) about a link. For example, if the URL
         * [https://www.youtube.com/watch?v=xuCn8ux2gbs](https://www.youtube.com/watch?v=xuCn8ux2gbs) was
         * posted to a live thread, one of the entries in this list could look like this:
         *
         * <pre>
         * {
         * "provider_url":"https://www.youtube.com/",
         * "description":"http://billwurtz.com patreon: http://patreon.com/billwurtz (...)",
         * "title":"history of the entire world, i guess",
         * "url":"http://www.youtube.com/watch?v=xuCn8ux2gbs",
         * "type":"video",
         * "original_url":"https://www.youtube.com/watch?v=xuCn8ux2gbs",
         * "author_name":"bill wurtz",
         * "height":281,
         * "width":500,
         * "html":"<iframe class=\"embedly-embed\" src=\"//cdn.embedly.com/widgets/me(...)",></iframe>"thumbnail_width":480,
         * "version":"1.0",
         * "provider_name":"YouTube",
         * "thumbnail_url":"https://i.ytimg.com/vi/xuCn8ux2gbs/hqdefault.jpg",
         * "thumbnail_height":360,
         * "author_url":"https://www.youtube.com/user/billwurtz"
         * }
        </pre> *
         */
        @Json(name = "mobile_embeds")
        val externalMetadata: List<Map<String, Any>>,

        @Json(name = "liveupdate_id")
        val updateId: String,
    )

    @JsonClass(generateAdapter = true)
    data class Activity(
        val usersActive: Int,

        @Json(name = "fuzzed")
        val isFuzzed: Boolean,
    ) : Serializable
}
