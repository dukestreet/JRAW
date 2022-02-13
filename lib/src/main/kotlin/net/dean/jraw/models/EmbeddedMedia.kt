package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * Some media that is viewable inline when browsing reddit through the website. There are generally two shapes this
 * class can take. When the media is from an external source, [.getOEmbed] and [.getType] will both be
 * non-null. When the media is hosted by reddit, [.getRedditVideo] will be non-null.
 */
@JsonClass(generateAdapter = true)
data class EmbeddedMedia(
    /**
     * This is generally the OEmbed provider. Can also be the string `liveupdate` for a live reddit thread. Null
     * when [redditVideo] is not.
     */
    val type: String?,

    @Json(name = "oembed") val oEmbed: OEmbed? = null,

    /** The ID of the live thread referenced by the submission. Only present when [.getType] is "liveupdate".  */
    @Json(name = "event_id") val liveThreadId: String? = null,

    @Json(name = "reddit_video") val redditVideo: RedditVideo? = null,
) : Serializable {

    /**
     * An object that models the JSON response for the oEmbed standard. Properties are made nullable if the spec deems
     * them optional. See [here](https://oembed.com/) for more.
     */
    @JsonClass(generateAdapter = true)
    data class OEmbed(
        /**
         * One of "photo", "video", "link", or "rich". See
         * [section 2.3.4.1 to 2.3.4.4 of the OEmbed standard](https://oembed.com) for more.
         */
        val type: String?,

        /** The string "1.0"  */
        val version: String?,
        val title: String?,
        @Json(name = "author_name") val authorName: String?,
        @Json(name = "author_url") val authorUrl: String?,
        @Json(name = "provider_name") val providerName: String?,
        @Json(name = "provider_url") val providerUrl: String?,

        /** The suggested length in seconds to hold this resource in cache  */
        @Json(name = "cache_age") val cacheAge: Long?,
        @Json(name = "thumbnail_url") val thumbnailUrl: String?,
        @Json(name = "thumbnail_width") val thumbnailWidth: Int?,
        @Json(name = "thumbnail_height") val thumbnailHeight: Int?,

        /** Direct link to the image in question. Present only when type is "photo"  */
        val url: String?,

        /** Width in pixels of the media. Present only when type is "photo", "video", or "rich"  */
        val width: Int?,

        /** Height in pixels of the media. Present only when type is "photo", "video", or "rich"  */
        val height: Int?,

        /** HTML to insert directly into a page to display the resource. Present only when type is "video" or "rich".  */
        val embedHtml: String?,
    ) : Serializable

    @JsonClass(generateAdapter = true)
    data class RedditVideo(
        @Json(name = "fallback_url") val fallbackUrl: String?,
        val height: Int,
        val width: Int,
        @Json(name = "scrubber_media_url") val scrubberMediaUrl: String?,
        @Json(name = "dash_url") val dashUrl: String?,

        /** Length of the video in seconds  */
        val duration: Int,
        @Json(name = "hls_url") val hlsUrl: String?,
    ) : Serializable

    companion object {
        fun create(liveThreadId: String?): EmbeddedMedia {
            return EmbeddedMedia(
                type = "liveupdate",
                oEmbed = null,
                liveThreadId = liveThreadId,
                redditVideo = null
            )
        }
    }
}
