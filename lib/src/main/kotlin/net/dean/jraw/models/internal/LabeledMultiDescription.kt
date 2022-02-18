package net.dean.jraw.models.internal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.RedditModel

@RedditModel
@JsonClass(generateAdapter = true)
data class LabeledMultiDescription(
    @Json(name = "body_md") val body: String
)
