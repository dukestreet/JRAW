package net.dean.jraw.references

import com.squareup.moshi.Types
import net.dean.jraw.JrawUtils
import net.dean.jraw.RedditClient
import net.dean.jraw.databind.Enveloped
import net.dean.jraw.http.HttpResponse
import net.dean.jraw.models.Comment
import net.dean.jraw.models.KindConstants
import net.dean.jraw.models.Listing
import net.dean.jraw.models.internal.GenericJsonResponse

/** A reference to a reply to a submission or another comment */
class CommentReference(reddit: RedditClient, id: String) : PublicContributionReference(reddit, id, KindConstants.COMMENT) {
    fun data(): Comment {
        val response = reddit.request {
            it.path("/api/info")
            it.query(mapOf("id" to fullName))
        }

        val type = Types.newParameterizedType(Listing::class.java, Comment::class.java)
        val adapter = JrawUtils.moshi.adapter<Listing<Comment>>(type, Enveloped::class.java)
        return response.deserializeWith(adapter).single()
    }

    fun editAndGetData(text: String): Comment {
        val response = super.edit(text).deserialize<GenericJsonResponse>()
        val editedThings = (response.json?.data?.get("things") as? List<*>) ?: error("Unexpected JSON structure")
        return JrawUtils.adapter<Comment>(Enveloped::class.java).fromJsonValue(editedThings.first())!!
    }
}
