package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Flair(
    @Json(name = "text") val text: String,
    @Json(name = "text_editable") val isTextEditable: Boolean,
    @Json(name = "id") val id: String,
    @Json(name = "css_class") val cssClass: String,
) : Serializable
