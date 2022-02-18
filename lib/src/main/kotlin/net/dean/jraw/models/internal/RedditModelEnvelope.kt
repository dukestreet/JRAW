package net.dean.jraw.models.internal

import com.squareup.moshi.JsonClass

/**
 * Class that models a typical JSON structure returned by the reddit API.
 */
@JsonClass(generateAdapter = true)
data class RedditModelEnvelope<T>(
    /**
     * Describes the type of the encapsulated data. For example, "t1" for comments, "t2" for accounts. See
     * [net.dean.jraw.models.KindConstants] for more.
     */
    val kind: String,

    val data: T,
)
