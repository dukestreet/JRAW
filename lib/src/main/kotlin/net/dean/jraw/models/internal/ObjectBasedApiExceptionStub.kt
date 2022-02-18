package net.dean.jraw.models.internal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.ApiException
import net.dean.jraw.http.NetworkException

/**
 * The model for a JSON-object based API error. Expected formats are:
 *
 * ```
 * {
 *   "fields": ["multipath"],
 *   "explanation": "you can't change that multireddit",
 *   "message": "Forbidden",
 *   "reason": "MULTI_CANNOT_EDIT"
 * }
 * ```
 *
 * ```
 * {
 *   "message": "Forbidden",
 *   "reason": 403
 * }
 * ```
 */
@JsonClass(generateAdapter = true)
data class ObjectBasedApiExceptionStub(
    @Json(name = "fields")
    val relevantFields: List<String>?,

    val explanation: String?,

    val message: String?,

    @Json(name = "reason")
    val code: String?,

    @Json(name = "error")
    val httpStatusCode: Int?,
) : RedditExceptionStub<ApiException> {

    override fun create(cause: NetworkException): ApiException? {
        return if (relevantFields != null && explanation != null && message != null && code != null) {
            ApiException(
                code = code,
                explanation = explanation,
                relevantParameters = relevantFields,
                cause = cause
            )
        } else if (message != null && httpStatusCode != null) {
            ApiException(
                code = httpStatusCode.toString(),
                explanation = message,
                relevantParameters = ArrayList(),
                cause = cause
            )
        } else null
    }

    override fun containsError(): Boolean {
        // Not exactly always true but close enough
        return message != null
    }
}
