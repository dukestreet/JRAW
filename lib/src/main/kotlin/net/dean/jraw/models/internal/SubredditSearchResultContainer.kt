package net.dean.jraw.models.internal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.models.SubredditSearchResult

@JsonClass(generateAdapter = true)
data class SubredditSearchResultContainer(
    @Json(name = "subreddits") val subreddits: List<SubredditSearchResult>
)
