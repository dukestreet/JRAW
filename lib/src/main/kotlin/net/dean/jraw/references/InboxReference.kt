package net.dean.jraw.references

import com.squareup.moshi.Types
import net.dean.jraw.*
import net.dean.jraw.databind.Enveloped
import net.dean.jraw.models.Listing
import net.dean.jraw.models.Message
import net.dean.jraw.pagination.BarebonesPaginator

/**
 * Reference to the user's inbox. Requires an authenticated user to use.
 */
class InboxReference internal constructor(reddit: RedditClient) : ReplyableReference(reddit) {
    /**
     * Creates a [BarebonesPaginator.Builder] to view messages.
     *
     * Possible `where` values:
     *
     * - `inbox` — all non-deleted messages
     * - `unread` — all unread messages
     * - `messages` — only private messages
     * - `comments` — only replies to comments the user created
     * - `selfreply` — only top-level replies to posts submitted by the user
     * - `mentions` — only comments that mention this user
     */
    @EndpointImplementation(Endpoint.GET_MESSAGE_WHERE, type = MethodType.NON_BLOCKING_CALL)
    fun iterate(where: String): BarebonesPaginator.Builder<Message> {
        return BarebonesPaginator.Builder.create(reddit, "/message/${JrawUtils.urlEncode(where)}")
    }

    /**
     * Sends a private message (PM) to another user from the currently authenticated user.
     *
     * @param dest The receiver of this message. Should be a username (like 'spez') or a subreddit WITH the "/r/"
     * prefix, e.g. '/r/redditdev'. If sent to a subreddit, the moderators of that subreddit will receive it in their
     * modmail.
     * @param subject Subject line, similar to an email
     * @param body Markdown-formatted text
     */
    fun compose(dest: String, subject: String, body: String) = compose(null, dest, subject, body)

    /**
     * Sends a private message (PM) to another user or the moderators of a subreddit.
     *
     * @param fromSubreddit If specified, will attempt to send the message as a moderator of the subreddit. If null, the
     * message will be sent as the authenticated user. If specified, the authenticated user must be a moderator of that
     * subreddit for this to work. Do not specify the "/r/" prefix. For example, if a moderator of /r/redditdev wanted
     * to send a PM, [fromSubreddit] would be "redditdev". If they wanted to send the message as themselves, they would
     * leave this null (or use the other overload of this method)
     * @param dest The receiver of this message. Should be a username (like 'spez') or a subreddit WITH the "/r/"
     * prefix, e.g. '/r/redditdev'. If sent to a subreddit, the moderators of that subreddit will receive it in their
     * modmail.
     * @param subject Subject line, similar to an email
     * @param body Markdown-formatted text
     */
    @EndpointImplementation(Endpoint.POST_COMPOSE)
    fun compose(fromSubreddit: String?, dest: String, subject: String, body: String) {
        val args = mutableMapOf(
            "api_type" to "json",
            "subject" to subject,
            "text" to body,
            "to" to dest
        )
        if (fromSubreddit != null)
            args["from_sr"] = fromSubreddit

        reddit.request {
            it.endpoint(Endpoint.POST_COMPOSE)
                .post(args)
        }
    }

    /**
     * Replies to a private message and returns the newly-created Message.
     *
     * @param parentFullName The full name of the private message to respond to. Should be something like "t4_XXXXXX"
     */
    fun replyTo(parentFullName: String, text: String): Message {
        val message = reply(parentFullName, text).first()
        return JrawUtils.adapter<Message>(Enveloped::class.java).fromJsonValue(message)!!
    }

    /**
     * Marks or unmarks the messages with the specified fullnames as read.
     */
    @EndpointImplementation(Endpoint.POST_READ_MESSAGE, Endpoint.POST_UNREAD_MESSAGE)
    fun markRead(read: Boolean, firstFullName: String, vararg otherFullNames: String) {
        markRead(read = read, fullNames = listOf(firstFullName, *otherFullNames))
    }

    /**
     * Marks or unmarks the messages with the specified fullnames as read.
     */
    @EndpointImplementation(Endpoint.POST_READ_MESSAGE, Endpoint.POST_UNREAD_MESSAGE)
    fun markRead(read: Boolean, fullNames: List<String>) {
        reddit.request {
            it.endpoint(if (read) Endpoint.POST_READ_MESSAGE else Endpoint.POST_UNREAD_MESSAGE)
                .post(mapOf("id" to fullNames.joinToString(",")))
        }
    }

    /** Marks all unread messages as read */
    @EndpointImplementation(Endpoint.POST_READ_ALL_MESSAGES)
    fun markAllRead() {
        reddit.request {
            it.endpoint(Endpoint.POST_READ_ALL_MESSAGES)
                .post(mapOf())
        }
    }

    /**
     * Removes a message from the user's inbox. Note that the message will remain in the inboxes of the other
     * participants and on reddit servers. Deleted messages will not appear in the user's inbox.
     *
     * @param fullName The full name of a message, something like `t4_xxxxx`
     *
     * @see Message.fullName
     */
    @EndpointImplementation(Endpoint.POST_DEL_MSG)
    fun delete(fullName: String) {
        reddit.request {
            it.endpoint(Endpoint.POST_DEL_MSG)
                .post(mapOf("id" to fullName))
        }
    }

    fun message(messageId: String): Message {
        val response = reddit.request {
            it.path("message/messages/{id}", messageId)
        }

        val type = Types.newParameterizedType(Listing::class.java, Message::class.java)
        val adapter = JrawUtils.moshi.adapter<Listing<Message>>(type, Enveloped::class.java)
        return adapter.fromJson(response.body)!!.children.single()
    }
}
