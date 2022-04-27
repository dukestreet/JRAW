package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubredditModerator(
    @Json(name = "name")
    val username: String
)
