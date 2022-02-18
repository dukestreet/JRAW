package net.dean.jraw.models

import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.Enveloped
import net.dean.jraw.databind.RedditModel
import net.dean.jraw.databind.UnixTime
import java.io.Serializable
import java.util.Date
import java.util.concurrent.TimeUnit

@RedditModel(enveloped = false)
@JsonClass(generateAdapter = true)
data class WikiRevision(
    @UnixTime(precision = TimeUnit.SECONDS)
    val timestamp: Date,

    val reason: String?,

    @Enveloped
    val author: Account?,

    val page: String,

    val id: String,
) : Serializable, UniquelyIdentifiable {
    override val uniqueId: String get() = hashCode().toString()
}
