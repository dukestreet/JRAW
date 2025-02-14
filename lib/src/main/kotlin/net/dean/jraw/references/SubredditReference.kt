package net.dean.jraw.references

import com.squareup.moshi.JsonDataException
import com.squareup.moshi.Types
import net.dean.jraw.*
import net.dean.jraw.models.*
import kotlinx.coroutines.runBlocking
import net.dean.jraw.models.internal.GenericJsonResponse
import net.dean.jraw.models.internal.SubmissionData
import net.dean.jraw.models.internal.SubredditModeratorList
import net.dean.jraw.pagination.BarebonesPaginator
import net.dean.jraw.pagination.DefaultPaginator
import net.dean.jraw.pagination.SearchPaginator
import net.dean.jraw.tree.RootCommentNode
import net.dean.jraw.websocket.readSubmissionIdFromWebSocket
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

/**
 * Allows the user to perform API actions against a subreddit
 *
 * @constructor Creates a new SubredditReference for the given subreddit. Do not include the "/r/" prefix (e.g. "pics")
 *
 * @property subreddit The name of the subreddit without the "/r/" prefix.
 */
class SubredditReference internal constructor(reddit: RedditClient, val subreddit: String) : AbstractReference(reddit) {

    /**
     * Returns a [Subreddit] instance for this reference.
     *
     * @throws ApiException if private.
     * @throws NoSuchSubredditException if it doesn't exist in the first place
     */
    @EndpointImplementation(Endpoint.GET_SUBREDDIT_ABOUT)
    fun about(): Subreddit = try {
        reddit.request { it.endpoint(Endpoint.GET_SUBREDDIT_ABOUT, subreddit) }.deserializeEnveloped()
    } catch (e: JsonDataException) {
        // Querying subreddits that don't exist doesn't 404 like a rational API, but 200's with an empty Listing as its
        // body. Don't ask me why. Trying to deserialize this as a Subreddit causes a JsonDataException.
        throw NoSuchSubredditException(subreddit, e)
    }

    /**
     * Creates a new [DefaultPaginator.Builder] to iterate over this subreddit's posts. Not a blocking call.
     */
    @EndpointImplementation(
        Endpoint.GET_HOT, Endpoint.GET_NEW, Endpoint.GET_RISING, Endpoint.GET_SORT, Endpoint.GET_BEST,
        type = MethodType.NON_BLOCKING_CALL
    )
    fun posts() = DefaultPaginator.Builder.create<Submission, SubredditSort>(reddit, "/r/$subreddit", sortingAlsoInPath = true)

    /**
     * Creates a BarebonesPaginator.Builder that will iterate over the latest comments from this subreddit when built.
     *
     * @see RedditClient.latestComments
     */
    fun comments(): BarebonesPaginator.Builder<Comment> = reddit.latestComments(subreddit)

    /**
     * Creates a BarebonesPaginator.Builder that will iterate over the gilded contributions in this subreddit when built.
     *
     * @see RedditClient.gildedContributions
     */
    fun gilded(): BarebonesPaginator.Builder<PublicContribution<*>> = reddit.gildedContributions(subreddit)

    /**
     * Creates a SearchPaginator.Builder to search for submissions in this subreddit.
     *
     * @see SearchPaginator.inSubreddits
     */
    fun search(): SearchPaginator.Builder = SearchPaginator.inSubreddits(reddit, subreddit)

    /**
     * Gets a random submission from this subreddit.
     *
     * @see RedditClient.randomSubreddit
     */
    @EndpointImplementation(Endpoint.GET_RANDOM)
    fun randomSubmission(): RootCommentNode {
        val data: SubmissionData = reddit.request { it.endpoint(Endpoint.GET_RANDOM, subreddit) }.deserialize()
        return RootCommentNode(data.submissions[0], data.comments, settings = null)
    }

    /**
     * Submits content to this subreddit
     *
     * @param kind Is this a self post (text) or a link post?
     * @param content If `kind` is [SubmissionKind.SELF], the Markdown-formatted body, else a URL.
     * @param sendReplies If direct replies to the submission should be sent to the user's inbox
     */
    @Suppress("DeprecatedCallableAddReplaceWith")
    @Deprecated("Use submit() with SubmissionKind2 instead")
    @EndpointImplementation(Endpoint.POST_SUBMIT)
    fun submit(kind: SubmissionKind, title: String, content: String, sendReplies: Boolean): SubmissionReference {
        return runBlocking {
            submit(
                title = title,
                kind = when (kind) {
                    SubmissionKind.LINK -> SubmissionKind2.Link(url = content)
                    SubmissionKind.SELF -> SubmissionKind2.SelfText(text = content)
                },
                sendReplies = sendReplies,
                flairId = null,
                flairText = null,
                isSpoiler = false,
                isNsfw = false
            )
        }
    }

    /**
     * Submits content to this subreddit
     *
     * @param sendReplies If direct replies to the submission should be sent to the user's inbox
     */
    @EndpointImplementation(Endpoint.POST_SUBMIT)
    suspend fun submit(
        kind: SubmissionKind2,
        title: String,
        flairId: String?,
        flairText: String?,
        sendReplies: Boolean,
        isNsfw: Boolean,
        isSpoiler: Boolean,
    ): SubmissionReference {
        val commonRequest = SubmitPostRequest(
            api_type = "json",
            extension = "json",
            resubmit = false,
            sendreplies = sendReplies,
            spoiler = isSpoiler,
            nsfw = isNsfw,
            title = title,
            sr = subreddit,
            flair_id = flairId,
            flair_text = flairText,
        )
        val request = when (kind) {
            is SubmissionKind2.Link -> commonRequest.copy(
                kind = "link",
                url = kind.url,
            )
            is SubmissionKind2.SelfText -> commonRequest.copy(
                kind = "self",
                text = kind.text,
            )
            is SubmissionKind2.CrossPost -> commonRequest.copy(
                kind = "crosspost",
                crosspost_fullname = kind.submissionFullName
            )
            is SubmissionKind2.Media -> when (val single = kind.items.singleOrNull()) {
                null -> commonRequest.copy(
                    kind = null,
                    items = kind.items.map { SubmitPostRequest.GalleryItem(media_id = it.assetId) }
                )
                else -> commonRequest.copy(
                    kind = single.mediaType.type,
                    url = kind.items.single().url
                )
            }
        }
        val requestJson = JrawUtils.adapter<SubmitPostRequest>().toJson(request)

        val res = reddit.request {
            if (request.items == null) {
                it.endpoint(Endpoint.POST_SUBMIT)
                it.post(JrawUtils.adapter<Map<String, Any>>().fromJson(requestJson)!!.mapValues { (key, value) -> value.toString() })
            } else {
                it.path("/api/submit_gallery_post.json")
                it.post(requestJson.toRequestBody("application/json".toMediaType()))
            }
        }.deserialize<GenericJsonResponse>()

        val submissionId = res.json?.data?.get("id") as? String
        if (submissionId != null) {
            // IDs are prefixed with "t3_" for gallery posts.
            return SubmissionReference(reddit, submissionId.removePrefix(KindConstants.SUBMISSION + "_"))
        }

        val websocketUrl = res.json?.data?.get("websocket_url") as? String
        if (websocketUrl != null) {
            return SubmissionReference(reddit, id = reddit.readSubmissionIdFromWebSocket(websocketUrl))
        }

        throw IllegalArgumentException("Submission ID not found")
    }

    /**
     * Gets the text meant to be displayed on the submission form.
     */
    @EndpointImplementation(Endpoint.GET_SUBMIT_TEXT)
    fun submitText(): String {
        return reddit.request {
            it.endpoint(Endpoint.GET_SUBMIT_TEXT, subreddit)
        }.deserialize<Map<String, String>>().getOrElse("submit_text") {
            throw IllegalArgumentException("Unexpected response: no `submit_text` key")
        }
    }

    /** Alias to `setSubscribed(true)` */
    fun subscribe() = setSubscribed(true)

    /** Alias to `setSubscribed(false)` */
    fun unsubscribe() = setSubscribed(false)

    /** Subscribes or unsubscribes to this subreddit. Requires an authenticated user. */
    @EndpointImplementation(Endpoint.POST_SUBSCRIBE)
    fun setSubscribed(subscribe: Boolean) {
        val body = mutableMapOf(
            "sr_name" to subreddit,
            "action" to if (subscribe) "sub" else "unsub"
        )

        if (subscribe)
            // "prevent automatically subscribing the user to the current set of defaults when they take their first
            // subscription action"
            body["skip_initial_defaults"] = "true"

        // Response is an empty JSON object
        reddit.request {
            it.endpoint(Endpoint.POST_SUBSCRIBE).post(body)
        }
    }

    fun setFavorite(favorite: Boolean) {
        val body = mutableMapOf(
            "sr_name" to subreddit,
            "make_favorite" to favorite.toString()
        )
        reddit.request {
            it.path("/api/favorite").post(body)
        }
    }

    /**
     * Lists all possible flairs for users. Requires an authenticated user. Will return nothing if flair is disabled on
     * the subreddit, the user cannot set their own flair, or they are not a moderator that can set flair.
     *
     * @see FlairReference.updateToTemplate
     */
    @EndpointImplementation(Endpoint.GET_USER_FLAIR)
    fun userFlairOptions(): List<Flair> = requestFlair("user")

    /**
     * Lists all possible flairs for submissions to this subreddit. Requires an authenticated user. Will return nothing
     * if the user cannot set their own link flair and they are not a moderator that can set flair.
     *
     * @see FlairReference.updateToTemplate
     */
    @EndpointImplementation(Endpoint.GET_LINK_FLAIR)
    fun linkFlairOptions(): List<Flair> = requestFlair("link")

    private fun requestFlair(type: String): List<Flair> {
        return reddit.request {
            it.path("/r/${JrawUtils.urlEncode(subreddit)}/api/${type}_flair")
        }.deserializeWith(JrawUtils.moshi.adapter(listOfFlairsType))
    }

    /**
     * Returns a UserFlairReference for a user.
     *
     * Equivalent to `redditClient.user(name).flairOn(subreddit)`.
     */
    fun otherUserFlair(name: String) = reddit.user(name).flairOn(subreddit)

    /**
     * Returns a UserFlairReference for the authenticated user.
     *
     * Equivalent to `redditClient.me().flairOn(subreddit)`.
     */
    fun selfUserFlair() = reddit.me().flairOn(subreddit)

    /**
     * Returns a SubmissionFlairReference for the given submission id (without the kind prefix).
     */
    fun submissionFlair(id: String) = SubmissionFlairReference(reddit, subreddit, id)

    /**
     * Returns the moderator-created rules for the subreddit. Users can report things in this subreddit using the String
     * specified in [SubredditRule.violationReason]
     */
    @EndpointImplementation(Endpoint.GET_SUBREDDIT_ABOUT_RULES)
    fun rules(): Ruleset {
        return reddit.request {
            it.endpoint(Endpoint.GET_SUBREDDIT_ABOUT_RULES, subreddit)
        }.deserialize()
    }

    fun moderators(): LoadSubredditModeratorsResult {
        return try {
            val list = reddit.request {
                it.path("r/${JrawUtils.urlEncode(subreddit)}/about/moderators.json")
            }.deserializeEnveloped<SubredditModeratorList>()
            LoadSubredditModeratorsResult.Loaded(list.moderators)

        } catch (e: Throwable) {
            if (e is ApiException && e.code == "403") {
                LoadSubredditModeratorsResult.Forbidden
            } else {
                LoadSubredditModeratorsResult.UnknownFailure(e)
            }
        }
    }

    /** Returns a reference to the subreddit's wiki */
    fun wiki() = WikiReference(reddit, subreddit)

    /** Fetches the stylesheet of a subreddit. Returned value is CSS. */
    @EndpointImplementation(Endpoint.GET_STYLESHEET)
    fun stylesheet(): String {
        return reddit.request {
            it.endpoint(Endpoint.GET_STYLESHEET, subreddit)
        }.body
    }

    /**
     * Updates the stylesheet of a subreddit. Requires mod priveleges on the subreddit.
     *
     * @param stylesheet New stylesheet of the subreddit, completely replaces the pre-existing one
     * @param reason Reason for the update to be displayed in the stylesheet change history
     * */
    @EndpointImplementation(Endpoint.POST_SUBREDDIT_STYLESHEET)
    fun updateStylesheet(stylesheet: String, reason: String) {
        reddit.request {
            it.endpoint(Endpoint.POST_SUBREDDIT_STYLESHEET, subreddit)
                .post(mapOf(
                    "api_type" to "json",
                    "op" to "save",
                    "reason" to reason,
                    "stylesheet_contents" to stylesheet
                ))
        }
    }

    /**
     * Returns a listing of currently set flairs on the subreddit (both custom and template-based ones)
     * Requires mod priveleges on the subreddit.
     */
    @EndpointImplementation(Endpoint.GET_FLAIRLIST)
    fun flairList() = BarebonesPaginator.Builder.create<SimpleFlairInfo>(reddit, "/r/$subreddit/api/flairlist")

    /**
     * Updates users flairs on the subreddit in bulk (up to 100 rows, the rest are ignored by Reddit).
     * Requires mod privileges on the subreddit. If the CSS class and text are empty/null for a particular user, that
     * user's flair is cleared.
     *
     * Note that even if this call succeeds, reddit may still have rejected one or more of the changes. Make sure to
     * check the [FlairPatchReport]s that this method returns.
     */
    @EndpointImplementation(Endpoint.POST_FLAIRCSV)
    fun patchFlairList(patch: List<SimpleFlairInfo>): List<FlairPatchReport> {
        return reddit.request {
            it.path("/r/$subreddit/api/flaircsv")
                .post(mapOf(
                    "flair_csv" to (patch.joinToString(separator = "\n") { it.toCsvLine() })
                ))
        }.deserializeWith(JrawUtils.moshi.adapter(Types.newParameterizedType(List::class.java, FlairPatchReport::class.java)))
    }

    /** */
    companion object {
        private val listOfFlairsType = Types.newParameterizedType(List::class.java, Flair::class.java)
    }
}

sealed interface LoadSubredditModeratorsResult {
    data class Loaded(
        val moderators: List<SubredditModerator>
    ) : LoadSubredditModeratorsResult

    /**
     * Returned when either there is no logged in user or the user is
     * banned from the subreddit whose moderator list was queried.
     */
    object Forbidden : LoadSubredditModeratorsResult

    /**
     * Includes 404 as well that is difficult to identify because Reddit
     * sends down search results instead of sending an error like a normal system.
     */
    data class UnknownFailure(val error: Throwable) : LoadSubredditModeratorsResult
}
