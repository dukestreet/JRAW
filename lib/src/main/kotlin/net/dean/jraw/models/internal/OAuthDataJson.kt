package net.dean.jraw.models.internal

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.models.OAuthData
import java.util.Date
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OAuthDataJson(
    /** A token that can be sent with an Authorization header to access oauth.reddit.com  */
    @Json(name = "access_token") val accessToken: String,

    /** The time in seconds that the access token will be valid for  */
    @Json(name = "expires_in") val expiresIn: Long,

    /** A comma-separated list of OAuth2 scopes the application has permission to use  */
    @Json(name = "scope") val scopeList: String,

    /** A refresh token, if one was requested  */
    @Json(name = "refresh_token") val refreshToken: String?,
) {

    fun toOAuthData(): OAuthData {
        return OAuthData(
            accessToken = accessToken,
            scopes = listOf(*scopeList.split(" ").toTypedArray()),
            refreshToken = refreshToken,
            expiration = Date(Date().time + TimeUnit.MILLISECONDS.convert(expiresIn, TimeUnit.SECONDS))
        )
    }
}
