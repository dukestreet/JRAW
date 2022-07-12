package net.dean.jraw.pagination

import net.dean.jraw.RedditClient
import net.dean.jraw.http.HttpRequest
import net.dean.jraw.models.UniquelyIdentifiable

/**
 * This class, like its name suggests, supports fewer query modifiers compared to other Paginators. Only the limit can
 * be set.
 */
open class BarebonesPaginator<T : UniquelyIdentifiable> private constructor(
    reddit: RedditClient,
    baseUrl: String,
    limit: Int,
    private val afterFullName: String?,
    clazz: Class<T>
) : Paginator<T>(reddit, baseUrl, limit, clazz) {

    override fun createNextRequest(): HttpRequest.Builder {
        val args = mutableMapOf("limit" to limit.toString())

        val anchor = afterFullName ?: current?.nextName
        if (anchor != null)
            args["after"] = anchor

        return reddit.requestStub()
            .path(baseUrl)
            .query(args)
    }

    /** Builder pattern for this class */
    class Builder<T : UniquelyIdentifiable>(reddit: RedditClient, baseUrl: String, clazz: Class<T>) :
        Paginator.Builder<T>(reddit, baseUrl, clazz) {

        /** How many items to request at once */
        private var limit: Int = Paginator.DEFAULT_LIMIT
        private var afterFullName: String? = null

        /** How many items to request at once */
        fun limit(limit: Int): Builder<T> { this.limit = limit; return this }

        /** Sets a custom pagination anchor */
        @Deprecated("Use after() instead", ReplaceWith("after(anchorFullName)"))
        fun customAnchor(anchorFullName: String): Builder<T> = after(anchorFullName)

        /** Sets a custom pagination anchor */
        fun after(fullName: String?): Builder<T> { this.afterFullName = fullName; return this }

        override fun build(): BarebonesPaginator<T> =
            BarebonesPaginator(reddit, baseUrl, limit, afterFullName, clazz)

        /** */
        companion object {
            /** Convenience factory function using reified generics */
            inline fun <reified T : UniquelyIdentifiable> create(reddit: RedditClient, baseUrl: String): Builder<T> {
                return Builder(reddit, baseUrl, T::class.java)
            }
        }
    }
}
