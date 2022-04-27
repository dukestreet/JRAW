package net.dean.jraw.models.internal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.RedditModel
import net.dean.jraw.models.SubredditModerator

@RedditModel
@JsonClass(generateAdapter = true)
internal data class SubredditModeratorList(
    @Json(name = "children")
    val moderators: List<SubredditModerator>
)
