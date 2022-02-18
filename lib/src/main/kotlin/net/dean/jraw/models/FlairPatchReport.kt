package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class FlairPatchReport(
    /** A summary of the action the API took. For example, "skipped," or "added flair for user _vargas_"  */
    val status: String,

    /** True if the operation completed successfully  */
    @Json(name = "ok")
    val isOk: Boolean,

    /** Any errors that occurred during processing  */
    val errors: Map<String, String>
) : Serializable
