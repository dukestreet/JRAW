package net.dean.jraw.test

import com.gargoylesoftware.htmlunit.WebClient
import com.gargoylesoftware.htmlunit.html.HtmlButton
import com.gargoylesoftware.htmlunit.html.HtmlElement
import com.gargoylesoftware.htmlunit.html.HtmlInput
import com.gargoylesoftware.htmlunit.html.HtmlPage
import com.winterbe.expekt.should
import net.dean.jraw.RedditClient
import net.dean.jraw.oauth.OAuthHelper
import net.dean.jraw.oauth.StatefulAuthHelper

/*
Utility functions to emulate browser authentication
 */

fun createWebClient(): WebClient {
    val client = WebClient()

    // Reddit does some weird things with JS, but it's not necessary to be emulating them for this test
    client.options.isJavaScriptEnabled = false
    // Turn off CSS because Jacoco complains that one of the classes HtmlUnit it pulls in is too long (like ~5000 lines)
    // com.steadystate.css.parser.SACParserCSS3TokenManager
    client.options.isCssEnabled = false
    // Save some time
    client.options.isDownloadImages = false

    return client
}

fun doBrowserLogin(vararg scopes: String = arrayOf("identity")): Pair<StatefulAuthHelper, HtmlPage> {
    val helper = OAuthHelper.interactive(newOkHttpAdapter(), CredentialsUtil.app, InMemoryTokenStore())

    // Test state change once we get the authorization URL
    helper.authStatus.should.equal(StatefulAuthHelper.Status.INIT)
    val url = helper.getAuthorizationUrl(requestRefreshToken = true, useMobileSite = false, scopes = *scopes)
    helper.authStatus.should.equal(StatefulAuthHelper.Status.WAITING_FOR_CHALLENGE)

    val client = createWebClient()

    // First we're gonna log in with the testing user credentials
    val loginPage = client.getPage<HtmlPage>(url)
    val loginForm = loginPage.forms.first { it.id == "login-form" }
    loginForm.getInputByName<HtmlInput>("user").valueAttribute = CredentialsUtil.script.username
    loginForm.getInputByName<HtmlInput>("passwd").valueAttribute = CredentialsUtil.script.password

    // Submit the form so we get redirected to the page where we can authorize our app
    val authorizePage: HtmlPage = findChild<HtmlButton>(loginForm, "button", "type" to "submit").click()
    return helper to authorizePage
}

fun emulateBrowserAuth(vararg scopes: String = arrayOf("identity")): RedditClient {
    val (helper, authorizePage) = doBrowserLogin(*scopes)
    val redirectPage: HtmlPage = findChild<HtmlInput>(authorizePage.body, "input", "name" to "authorize").click()

    val url = redirectPage.url.toExternalForm()
    helper.isFinalRedirectUrl(url).should.be.`true`
    val reddit = helper.onUserChallenge(url)
    helper.authStatus.should.equal(StatefulAuthHelper.Status.AUTHORIZED)
    return reddit
}

fun <E : HtmlElement> findChild(parent: HtmlElement, elName: String, attribute: Pair<String, String>): E {
    val elements: List<E> = parent.getElementsByAttribute(elName, attribute.first, attribute.second)
    if (elements.isEmpty())
        throw NoSuchElementException("Could not find element for selector '$elName[${attribute.first}=\"${attribute.second}\"']")

    return elements[0]
}
