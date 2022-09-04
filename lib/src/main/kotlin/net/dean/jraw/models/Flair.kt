package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Flair(
    @Json(name = "text") val text: String,
    @Json(name = "richtext") val richText: List<RichTextSpan>,
    @Json(name = "text_editable") val isTextEditable: Boolean,
    @Json(name = "id") val id: String,
    @Json(name = "css_class") val cssClass: String,
) : Serializable {

    @JsonClass(generateAdapter = true)
    data class RichTextSpan(
        /** Example values: "emoji", "text". */
        @Json(name = "e") val type: String?,

        /** Replacement text for emojis that can be shown until [emojiUrl] is loaded. */
        @Json(name = "a") val emojiTextRepresentation: String?,

        /** Link to this span's image */
        @Json(name = "u") val emojiUrl: String?,

        /** Non-null only when [type] is "text". */
        @Json(name = "t") val text: String?,
    ) : Serializable
}
