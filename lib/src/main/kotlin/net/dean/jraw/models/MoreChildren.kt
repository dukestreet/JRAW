package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.RedditModel
import java.io.Serializable

/**
 * A MoreChildren is a model used by reddit to represent comments that exist, but could not be presented in the response
 * due to the large amounts of other, higher priority comments already being shown in the thread. On the website, a
 * MoreChildren is represented by the text "load more comments (*x* replies)". The average user shouldn't have to
 * deal with this class directly as [net.dean.jraw.tree.CommentNode] will handle loading more comments for you.
 *
 * MoreChildren instances can appear anywhere in a comment tree except at the very root.
 */
@RedditModel
@JsonClass(generateAdapter = true)
data class MoreChildren(
    @Json(name = "name")
    override val fullName: String,

    override val id: String,

    @Json(name = "parent_id")
    override val parentFullName: String,

    @Json(name = "children")
    val childrenIds: List<String>

) : NestedIdentifiable, Serializable {
    override val uniqueId: String
        get() = fullName

    /**
     * Returns true if this MoreChildren object represents a thread continuation. On the website, thread continuations
     * are illustrated with "continue this thread →". Thread continuations are only seen when the depth of a CommentNode
     * exceeds the depth that reddit is willing to render (defaults to 10).
     *
     * A MoreChildren that is a thread continuation will have an [id] of "_" and an empty [childrenIds] list.
     *
     * @see net.dean.jraw.references.CommentsRequest.depth
     */
    val isThreadContinuation: Boolean
        get() = childrenIds.isEmpty() && id == "_"
}
