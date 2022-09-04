package net.dean.jraw.references

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Types
import net.dean.jraw.*
import net.dean.jraw.JrawUtils.urlEncode
import net.dean.jraw.databind.Enveloped
import net.dean.jraw.http.NetworkException
import net.dean.jraw.models.*
import net.dean.jraw.models.internal.GenericJsonResponse
import net.dean.jraw.models.internal.ObjectBasedApiExceptionStub
import net.dean.jraw.models.internal.RedditModelEnvelope
import net.dean.jraw.models.internal.TrophyList
import net.dean.jraw.pagination.BarebonesPaginator
import net.dean.jraw.pagination.DefaultPaginator
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody

/**
 * A user reference is exactly what you think it is, a reference to a user.
 *
 * @property username The name of the users account (note: not the ID or the full name)
 */
sealed class UserReference<out T : UserFlairReference>(reddit: RedditClient, val username: String) : AbstractReference(reddit) {
    /** True if and only if this UserReference is a [SelfUserReference] */
    abstract val isSelf: Boolean

    /**
     * Fetches basic information about this user.
     */
    @Throws(SuspendedAccountException::class)
    @Deprecated("Prefer query() for better handling of non-existent/suspended accounts", ReplaceWith("query()"))
    fun about(): Account {
        val body = reddit.request {
            it.path(if (isSelf) "/api/v1/me" else "/user/$username/about")
        }.body

        // /api/v1/me returns an Account that isn't wrapped with the data/kind nodes
        if (isSelf)
            return JrawUtils.adapter<Account>().fromJson(body)!!
        try {
            return JrawUtils.adapter<Account>(Enveloped::class.java).fromJson(body)!!
        } catch (npe: NullPointerException) {
            throw SuspendedAccountException(username)
        }
    }

    /**
     * Gets information about this account.
     */
    @EndpointImplementation(Endpoint.GET_ME, Endpoint.GET_USER_USERNAME_ABOUT)
    fun query(): AccountQuery {
        return try {
            AccountQuery(name = username, status = AccountStatus.EXISTS, account = about())
        } catch (e: ApiException) {
            val cause = e.cause
            if (cause is NetworkException && cause.res.code != 404)
                throw e
            else
                AccountQuery(name = username, status = AccountStatus.NON_EXISTENT)
        } catch (e: SuspendedAccountException) {
            AccountQuery(name = username, status = AccountStatus.SUSPENDED)
        }
    }

    /** Fetches any trophies the user has achieved */
    @EndpointImplementation(Endpoint.GET_ME_TROPHIES, Endpoint.GET_USER_USERNAME_TROPHIES)
    fun trophies(): List<Trophy> {
        return reddit.request {
            if (isSelf)
                it.endpoint(Endpoint.GET_ME_TROPHIES)
            else
                it.endpoint(Endpoint.GET_USER_USERNAME_TROPHIES, username)
        }.deserializeEnveloped<TrophyList>().trophies
    }

    /**
     * Creates a new [net.dean.jraw.pagination.Paginator.Builder] which can iterate over a user's public history.
     *
     * Possible `where` values:
     *
     * - `overview` — submissions and comments
     * - `submitted` — only submissions
     * - `comments` — only comments
     * - `gilded` — submissions and comments which have received reddit Gold
     *
     * If this user reference is for the currently logged-in user, these `where` values can be used:
     *
     * - `upvoted`
     * - `downvoted`
     * - `hidden`
     * - `saved`
     *
     * Only `overview`, `submitted`, and `comments` are sortable.
     */
    @EndpointImplementation(Endpoint.GET_USER_USERNAME_WHERE, type = MethodType.NON_BLOCKING_CALL)
    fun history(where: String): DefaultPaginator.Builder<PublicContribution<*>, UserHistorySort> {
        // Encode URLs to prevent accidental malformed URLs
        return DefaultPaginator.Builder.create(reddit, "/user/${urlEncode(username)}/${urlEncode(where)}",
            sortingAlsoInPath = false)
    }

    /**
     * Creates a [MultiredditReference] for a multireddit that belongs to this user.
     */
    fun multi(name: String) = MultiredditReference(reddit, username, name)

    /**
     * Lists the multireddits this client is able to view.
     *
     * If this UserReference is for the logged-in user, all multireddits will be returned. Otherwise, only public
     * multireddits will be returned.
     */
    @EndpointImplementation(Endpoint.GET_MULTI_MINE, Endpoint.GET_MULTI_USER_USERNAME)
    fun listMultis(): List<Multireddit> {
        val res = reddit.request {
            if (isSelf) {
                it.endpoint(Endpoint.GET_MULTI_MINE)
            } else {
                it.endpoint(Endpoint.GET_MULTI_USER_USERNAME, username)
            }
        }

        val type = Types.newParameterizedType(List::class.java, Multireddit::class.java)
        val adapter = JrawUtils.moshi.adapter<List<Multireddit>>(type, Enveloped::class.java)

        return res.deserializeWith(adapter)
    }

    /**
     * Returns a [UserFlairReference] for this user for the given subreddit. If this user is not the authenticated user,
     * the authenticated must be a moderator of the given subreddit to access anything specific to this user.
     */
    abstract fun flairOn(subreddit: String): T
}

/** A reference to the currently authenticated user */
class SelfUserReference(reddit: RedditClient) : UserReference<SelfUserFlairReference>(reddit, reddit.requireAuthenticatedUser()) {
    override val isSelf = true

    private val prefsAdapter: JsonAdapter<Map<String, Any>> by lazy {
        val type = Types.newParameterizedType(Map::class.java, String::class.java, Object::class.java)
        JrawUtils.moshi.adapter<Map<String, Any>>(type)
    }

    /** Creates an InboxReference */
    fun inbox() = InboxReference(reddit)

    /**
     * Creates a Multireddit (or updates it if it already exists).
     *
     * This method is equivalent to
     *
     * ```kotlin
     * userReference.multi(name).createOrUpdate(patch)
     * ```
     *
     * and provided for semantics.
     */
    fun createMulti(name: String, patch: MultiredditPatch): Multireddit = multi(name).createOrUpdate(patch)

    /**
     * Creates a live thread. The only property that's required to be non-null in the LiveThreadPatch is
     * [title][LiveThreadPatch.title].
     *
     * @see LiveThreadReference.edit
     */
    @EndpointImplementation(Endpoint.POST_LIVE_CREATE)
    fun createLiveThread(data: LiveThreadPatch): LiveThreadReference {
        val res = reddit.request {
            it.endpoint(Endpoint.POST_LIVE_CREATE)
                .post(data.toRequestMap())
        }.deserialize<GenericJsonResponse>()

        val id = res.json?.data?.get("id") as? String ?:
            throw IllegalArgumentException("Could not find ID")

        return LiveThreadReference(reddit, id)
    }

    /**
     * Gets a Map of preferences set at [https://www.reddit.com/prefs].
     *
     * Likely to throw an [ApiException] if authenticated via application-only credentials
     */
    @EndpointImplementation(Endpoint.GET_ME_PREFS)
    @Throws(ApiException::class)
    fun prefs(): Map<String, Any> {
        return reddit.request { it.endpoint(Endpoint.GET_ME_PREFS) }.deserializeWith(prefsAdapter)
    }

    /**
     * Patches over certain user preferences and returns all preferences.
     *
     * Although technically you can send any value as a preference value, generally only strings and booleans are used.
     * See [here](https://www.reddit.com/dev/api/oauth#GET_api_v1_me_prefs) for a list of all available preferences.
     *
     * Likely to throw an [ApiException] if authenticated via application-only credentials
     */
    @EndpointImplementation(Endpoint.PATCH_ME_PREFS)
    @Throws(ApiException::class)
    fun patchPrefs(newPrefs: Map<String, Any>): Map<String, Any> {
        val body = RequestBody.create("application/json".toMediaType(), prefsAdapter.toJson(newPrefs))
        return reddit.request { it.endpoint(Endpoint.PATCH_ME_PREFS).patch(body) }.deserialize()
    }

    /**
     * Returns a Paginator builder for subreddits the user is associated with
     *
     * Possible `where` values:
     *
     * - `contributor`
     * - `moderator`
     * - `subscriber`
     */
    @EndpointImplementation(Endpoint.GET_SUBREDDITS_MINE_WHERE, type = MethodType.NON_BLOCKING_CALL)
    fun subreddits(where: String): BarebonesPaginator.Builder<Subreddit> {
        return BarebonesPaginator.Builder.create(reddit, "/subreddits/mine/${JrawUtils.urlEncode(where)}")
    }

    /**
     * Fetches a breakdown of comment and link karma by subreddit for the user
     */
    @EndpointImplementation(Endpoint.GET_ME_KARMA)
    fun karma(): List<KarmaBySubreddit> {
        val json = reddit.request {
            it.endpoint(Endpoint.GET_ME_KARMA)
        }

        // Our data is represented by RedditModelEnvelope<List<KarmaBySubreddit>> so we need to create a Type instance
        // that reflects that
        val listType = Types.newParameterizedType(List::class.java, KarmaBySubreddit::class.java)
        val type = Types.newParameterizedType(RedditModelEnvelope::class.java, listType)

        // Parse the envelope and return its data
        val adapter = JrawUtils.moshi.adapter<RedditModelEnvelope<List<KarmaBySubreddit>>>(type)
        val parsed = adapter.fromJson(json.body)!!
        return parsed.data
    }

    override fun flairOn(subreddit: String): SelfUserFlairReference = SelfUserFlairReference(reddit, subreddit)
}

/**
 * A reference to user that is not the currently authenticated user. Note that it's still technically possible to create
 * an OtherUserReference for the currently authenticated user, but it won't be nearly as useful as creating a
 * [SelfUserReference] instead.
 */
class OtherUserReference(reddit: RedditClient, username: String) : UserReference<OtherUserFlairReference>(reddit, username) {
    override val isSelf = false

    override fun flairOn(subreddit: String): OtherUserFlairReference = OtherUserFlairReference(reddit, subreddit, username)

    fun setBlocked(blocked: Boolean): SetUserBlockedResult {
        return try {
            reddit.request {
                if (blocked) {
                    it.endpoint(Endpoint.POST_BLOCK_USER).post(mapOf("name" to username))
                } else {
                    val selfAccount = reddit.me().query().account!!
                    it.endpoint(Endpoint.POST_UNFRIEND, /* pathParams = */ "all")
                        .post(
                            mapOf(
                                "container" to selfAccount.fullName,
                                "type" to "enemy",
                                "name" to username,
                            )
                        )
                }
            }
            SetUserBlockedResult.Success

        } catch (e: Throwable) {
            return if (e is NetworkException) {
                val (explanation) = runCatching { e.res.deserialize<ObjectBasedApiExceptionStub>().explanation }
                SetUserBlockedResult.Failed(e, explanation = explanation)
            } else {
                SetUserBlockedResult.Failed(e, explanation = null)
            }
        }
    }
}

private operator fun <T> Result<T>.component1(): T? = getOrNull()

sealed interface SetUserBlockedResult {
    object Success : SetUserBlockedResult

    data class Failed(
        val error: Throwable,
        val explanation: String?
    ): SetUserBlockedResult
}
