package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class Ruleset(
    /** Rules that apply to a specific subreddit  */
    @Json(name = "rules") val subredditRules: List<SubredditRule>,

    /** Rules for the entire website  */
    @Json(name = "site_rules") val siteRules: List<String>,
) : Serializable
