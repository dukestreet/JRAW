package net.dean.jraw.oauth

import net.dean.jraw.JrawUtils
import net.dean.jraw.RedditClient
import net.dean.jraw.http.HttpRequest
import net.dean.jraw.http.NetworkAdapter
import net.dean.jraw.http.NetworkException
import net.dean.jraw.models.OAuthData
import net.dean.jraw.models.internal.OAuthDataJson
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import java.math.BigInteger
import java.net.URI
import java.security.SecureRandom

/**
 * This class helps manage the interactive OAuth2 authentication process. Each instance of this class has its own
 * internal state. Multiple calls to [getAuthorizationUrl] or [onUserChallenge] are prone to throw Exceptions because of
 * this internal state. Here's an overview of how this class functions:
 *
 * 1. On creation, [authStatus] is [Status.INIT].
 * 2. An authorization URL is generated for this OAuth2 app via [getAuthorizationUrl]. A "state" (random alphanumeric
 *    string of 26 characters) is generated, specifically tied to that request and included in the query of the
 *    generated URL. Auth status is now [Status.WAITING_FOR_CHALLENGE]
 * 3. The user is shown the genreated URL and must log in if not already authenticated, then allow the app access to
 *    their account. reddit will then redirect the user's browser to the OAuth2 app's redirect URL with data in the
 *    query.
 * 4. When the final redirect URL is detected ([isFinalRedirectUrl] can helpful here), that URL is passed to
 *    [onUserChallenge]. If the query contains an error, an [OAuthException] is thrown. reddit also hands back the state
 *    generated by [getAuthorizationUrl]. If this doesn't match, an IllegalArgumentException is thrown. If the query
 *    contains the "code" key, its value is then used to request an access code and a RedditClient is created.
 */
class StatefulAuthHelper internal constructor(
    /** Used to send HTTP requests and construct the RedditClient */
    private val http: NetworkAdapter,
    /** Used to craft HTTP requests to authenticate users for this app */
    private val creds: Credentials,
    /** Used to create the RedditClient */
    private val tokenStore: TokenStore,
    /** Optional callback for when the client is authenticated */
    private val onAuthenticated: (reddit: RedditClient) -> Unit
) {
    private var state: String? = null
    private var _authStatus: Status = Status.INIT

    /** The helper's current state */
    val authStatus: Status
        get() = _authStatus

    /**
     * Generates an authorization URL for the OAuth2 app.
     *
     * @param requestRefreshToken If true, a refresh token will be returned as well as an access token. This refresh
     * token can be used to request more access tokens until revoked.
     * @param useMobileSite If true, will use the site optimized for mobile devices
     * @param scopes A list of OAuth2 scopes. See [here](https://www.reddit.com/dev/api/oauth) for a full list.
     */
    fun getAuthorizationUrl(requestRefreshToken: Boolean = true, useMobileSite: Boolean = true, vararg scopes: String): String {
        // Generate a random alpha-numeric string
        // http://stackoverflow.com/a/41156
        state = BigInteger(128, rand).toString(32)

        this._authStatus = Status.WAITING_FOR_CHALLENGE

        // Use HttpRequest.Builder as an interface to create a URL
        return HttpRequest.Builder()
            .secure(true)
            .host("www.reddit.com")
            .path("/api/v1/authorize${if (useMobileSite) ".compact" else ""}")
            .query(mapOf(
                "client_id" to creds.clientId,
                "response_type" to "code",
                "state" to state!!,
                "redirect_uri" to creds.redirectUrl!!,
                "duration" to if (requestRefreshToken) "permanent" else "temporary",
                "scope" to scopes.joinToString(separator = " ")
            )).build().url
    }

    /**
     * Checks if the given URL is the URL that reddit redirects to when the user allows or denies access to the OAuth2
     * app.
     */
    fun isFinalRedirectUrl(url: String): Boolean {
        val httpUrl = url.toHttpUrlOrNull() ?: throw IllegalArgumentException("Malformed URL: $url")
        creds.redirectUrl ?: throw IllegalStateException("Given credentials have no redirect URL")

        return httpUrl.toString().startsWith(creds.redirectUrl) && httpUrl.queryParameter("state") != null
    }

    /**
     * Parses the query of the given URL and attempts to request an access token. This sends an HTTP request and is
     * therefore a blocking call.
     *
     * @return An authenticated RedditClient instance
     *
     * @throws IllegalStateException If [authStatus] is not [Status.WAITING_FOR_CHALLENGE] or if the value of the
     * `state` key in the query does not equal the internal state generated when [getAuthorizationUrl] was called.
     * @throws OAuthException If the key `error` is present in the query
     * @throws IllegalAccessError If no `state` or `code` key is present in the query
     */
    @Throws(NetworkException::class, OAuthException::class, IllegalStateException::class)
    fun onUserChallenge(finalUrl: String): RedditClient {
        if (authStatus != Status.WAITING_FOR_CHALLENGE)
            throw IllegalStateException("Expecting auth status ${Status.WAITING_FOR_CHALLENGE}, got $authStatus")

        val query = JrawUtils.parseUrlEncoded(URI(finalUrl).query)
        if ("error" in query)
            throw OAuthException("Reddit responded with error: ${query["error"]}")
        if ("state" !in query)
            throw IllegalArgumentException("Final redirect URL did not contain the 'state' query parameter")
        if (query["state"] != state)
            throw IllegalStateException("State did not match")
        if ("code" !in query)
            throw IllegalArgumentException("Final redirect URL did not contain the 'code' query parameter")

        val code = query["code"]!!

        try {
            val response: OAuthData = http.execute(HttpRequest.Builder()
                .secure(true)
                .host("www.reddit.com")
                .path("/api/v1/access_token")
                .post(mapOf(
                    "grant_type" to "authorization_code",
                    "code" to code,
                    "redirect_uri" to creds.redirectUrl!!
                ))
                .basicAuth(creds.clientId to creds.clientSecret)
                .build()).deserialize<OAuthDataJson>().toOAuthData()

            this._authStatus = Status.AUTHORIZED
            val r = RedditClient(
                http = http,
                initialOAuthData = response,
                creds = creds,
                tokenStore = tokenStore
            )
            onAuthenticated(r)
            return r
        } catch (ex: NetworkException) {
            if (ex.res.code == 401)
                throw OAuthException("Invalid client ID/secret", ex)
            throw ex
        }
    }

    /** */
    companion object {
        private val rand: SecureRandom = SecureRandom()
    }

    /** A list of possible authentication statuses */
    enum class Status {
        /** An instance has been created by no action has been performed */
        INIT,

        /** An authorization URL has been created, but the user has not accepted/declined yet */
        WAITING_FOR_CHALLENGE,

        /** Authorized and ready to send requests */
        AUTHORIZED
    }
}
