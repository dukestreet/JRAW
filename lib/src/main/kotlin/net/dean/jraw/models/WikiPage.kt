package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.Enveloped
import net.dean.jraw.databind.RedditModel
import net.dean.jraw.databind.UnixTime
import java.io.Serializable
import java.util.Date
import java.util.concurrent.TimeUnit

@RedditModel
@JsonClass(generateAdapter = true)
data class WikiPage(
    @Json(name = "may_revise") val mayRevise: Boolean,

    /** The last time this page was edited, or null if never.  */
    @Json(name = "revision_date")
    @UnixTime(precision = TimeUnit.SECONDS)
    val revisionDate: Date?,

    /** The person that last revised this page, or null if never revised.  */
    @Enveloped
    @Json(name = "revision_by")
    val revisionBy: Account?,

    /** The Markdown-formatted body of the page  */
    @Json(name = "content_md") val content: String?,
) : Serializable {

    @Deprecated("Use revisionBy instead", ReplaceWith("revisionBy"))
    val revionBy: Account? get() = revisionBy

    /** True if there is an authenticated user and that user has the privileges to edit this wiki page  */
    fun mayRevise(): Boolean {
        return mayRevise
    }
}
