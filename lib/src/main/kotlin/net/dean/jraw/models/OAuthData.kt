package net.dean.jraw.models

import com.squareup.moshi.JsonClass
import net.dean.jraw.databind.UnixTime
import java.io.Serializable
import java.util.Date
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class OAuthData(
    /** A token that can be sent with an Authorization header to access oauth.reddit.com  */
    val accessToken: String,

    /** A list in scopes the access token has permission for  */
    val scopes: List<String>,

    /** A token that can be used to request a new access token after the current one has expired, if one was requested  */
    val refreshToken: String?,

    /** The date at which the access token will expire  */
    @UnixTime(precision = TimeUnit.MILLISECONDS) val expiration: Date,
) : Serializable {

    val isExpired: Boolean get() = expiration.before(Date())
}
