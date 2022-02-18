package net.dean.jraw.models.internal

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SubredditElement(
    val name: String
)
