package net.dean.jraw.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class SubmitPostRequest(
    val api_type: String,
    val extension: String,
    val resubmit: Boolean,
    val sendreplies: Boolean,
    val spoiler: Boolean,
    val nsfw: Boolean,
    val sr: String,
    val title: String,
    val kind: String? = null,
    val url: String? = null,
    val text: String? = null,
    val items: List<GalleryItem>? = null,
    val crosspost_fullname: String? = null,
) {

    @JsonClass(generateAdapter = true)
    data class GalleryItem(
        val caption: String? = null,
        val outbound_url: String? = null,
        val media_id: String,
    )
}
