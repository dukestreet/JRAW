package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class SubmissionPreview(
    val images: List<ImageSet>,

    @Json(name = "enabled")
    val isEnabled: Boolean,
) : Serializable {

    @JsonClass(generateAdapter = true)
    data class ImageSet(
        val source: Variation?,
        val resolutions: List<Variation>,
        val id: String?,
    ) : Serializable

    @JsonClass(generateAdapter = true)
    data class Variation(
        val url: String?,
        val width: Int,
        val height: Int,
    ) : Serializable
}
