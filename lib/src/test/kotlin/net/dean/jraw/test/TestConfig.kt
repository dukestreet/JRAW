package net.dean.jraw.test

import net.dean.jraw.RedditClient
import net.dean.jraw.Version
import net.dean.jraw.http.UserAgent
import net.dean.jraw.oauth.OAuthHelper

object TestConfig {
    /** UserAgent used by all HttpAdapters used for testing */
    val userAgent = UserAgent("lib", "net.dean.jraw.test", Version.get(), "thatJavaNerd")

    val reddit: RedditClient by lazy {
        OAuthHelper.automatic(newOkHttpAdapter(), CredentialsUtil.script)
    }
    val redditUserless: RedditClient by lazy {
        OAuthHelper.automatic(newOkHttpAdapter(), CredentialsUtil.applicationOnly)
    }
}
