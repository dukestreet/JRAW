package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.UnixTime
import java.io.Serializable
import java.util.Date
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class SubredditRule(
    /** "link" if the rule only applies to submissions, "comment" for only comments, and "all" for both.  */
    val kind: String,

    /** Short Markdown-formatted description of the rule  */
    val description: String,

    /** A succinct version of the description  */
    @Json(name = "short_name") val shortName: String,

    /** The String the user uses when reporting something that violates this rule  */
    @Json(name = "violation_reason")
    val violationReason: String,

    @Json(name = "created_utc")
    @UnixTime(precision = TimeUnit.SECONDS)
    override val created: Date,
) : Created, Serializable
