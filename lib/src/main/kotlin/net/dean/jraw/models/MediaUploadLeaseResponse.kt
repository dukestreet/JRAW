package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
internal data class MediaUploadLeaseResponse(
    val args: Args,
    val asset: Asset,
) {
    @JsonClass(generateAdapter = true)
    data class Args(
        /** Scheme-less URL for Reddit's Amazon S3 bucket. */
        @Json(name = "action") val uploadUrlWithoutScheme: String,

        /** Key-value pairs that must be converted into form data for uploading to Reddit's Amazon S3 bucket. */
        @Json(name = "fields") val formDataParts: List<FormFieldPart>
    ) {
        val uploadUrl: String get() = "https:$uploadUrlWithoutScheme"
    }

    @JsonClass(generateAdapter = true)
    data class FormFieldPart(
        val name: String,
        val value: String,
    )

    @JsonClass(generateAdapter = true)
    data class Asset(
        @Json(name = "asset_id") val id: String,
    )
}
