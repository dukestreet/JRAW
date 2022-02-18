package net.dean.jraw.models.internal

import com.squareup.moshi.JsonClass
import net.dean.jraw.models.CurrentFlair

@JsonClass(generateAdapter = true)
data class FlairSelector(
    val current: CurrentFlair
)
