package net.dean.jraw.models.internal

import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.Enveloped
import net.dean.jraw.databind.RedditModel
import net.dean.jraw.models.Trophy

@RedditModel
@JsonClass(generateAdapter = true)
data class TrophyList(
    @Enveloped val trophies: List<Trophy>
)
