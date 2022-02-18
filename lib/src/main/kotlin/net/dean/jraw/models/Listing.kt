package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.Enveloped
import net.dean.jraw.databind.RedditModel
import java.io.Serializable

/**
 * A Listing is how reddit handles pagination.
 *
 * A Listing has two main parts: the fullnames of the item that comes next, and current page's children. As a
 * convenience, Listing delegates the methods inherited from [java.util.List] to [.getChildren]. That
 * means that `listing.indexOf(foo)` is the same as `listing.children.indexOf(foo)`.
 */
@RedditModel
@JsonClass(generateAdapter = true)
data class Listing<T>(
    /** Gets the fullname of the model at the top of the next page, if it exists  */
    @Json(name = "after") val nextName: String?,

    /** Gets the objects contained on this page  */
    @Json(name = "children") @Enveloped val children: List<T>,
) : Serializable, List<T> by children {

    companion object {
        fun <T> empty(): Listing<T> {
            return Listing(nextName = null, children = ArrayList())
        }
    }
}
