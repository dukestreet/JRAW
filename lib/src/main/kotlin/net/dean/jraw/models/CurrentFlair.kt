package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class CurrentFlair(
    @Json(name = "flair_css_class") val cssClass: String? = null,
    @Json(name = "flair_template_id") val id: String? = null,
    @Json(name = "flair_text") val text: String? = null,

    /** Either 'right' or 'left'  */
    @Json(name = "flair_position") val position: String? = null,
) : Serializable {

    /**
     * Reddit doesn't represent the current flair as a null object, but rather as an object will all-null properties.
     * This method checks if there is a current flair for user/submission.
     */
    val isPresent: Boolean
        get() = cssClass != null && id != null && text != null && position != null
}
