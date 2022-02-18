package net.dean.jraw.models.internal

import com.squareup.moshi.JsonClass
import net.dean.jraw.ApiException
import net.dean.jraw.RateLimitException
import net.dean.jraw.RedditException
import net.dean.jraw.http.NetworkException

/**
 * Used to model a JSON response like this:
 *
 * ```
 * {
 *   "json": {
 *     "errors": [],
 *     "data": {
 *       "foo": "bar",
 *       "baz": "qux"
 *     }
 *   }
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class GenericJsonResponse(
    val json: Inner?
) : RedditExceptionStub<RedditException> {

    override fun create(cause: NetworkException): RedditException? {
        if (json == null) {
            return null
        }
        if (json.ratelimit != null) {
            return RateLimitException(json.ratelimit, cause)
        }
        if (json.errors != null && json.errors.isNotEmpty()) {
            // We only really care about the first error and since there's rarely a time where there are more than one
            // errors being returned, it doesn't matter anyway
            val error = json.errors[0]
            val relevantParameter = if (error.size > 2) error[2] else null
            return ApiException(
                code = error[0],
                explanation = error[1],
                relevantParameters = listOfNotNull(relevantParameter),
                cause = cause
            )
        }
        return null
    }

    override fun containsError(): Boolean {
        return json != null && (json.ratelimit != null || json.errors!!.isNotEmpty())
    }

    @JsonClass(generateAdapter = true)
    data class Inner(
        /**
         * A two-dimensional list of Strings. Each child of this list is its own error. Each error (usually) has two
         * properties: an error code and a human-readable message.
         */
        val errors: List<List<String>>? = null,
        val data: Map<String, Any>? = null,
        val ratelimit: Double? = null,
    )
}
