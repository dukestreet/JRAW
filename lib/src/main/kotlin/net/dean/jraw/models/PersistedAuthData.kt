package net.dean.jraw.models

import com.squareup.moshi.JsonClass
import java.io.Serializable

/**
 * This class is used by [net.dean.jraw.oauth.DeferredPersistentTokenStore] implementations to persist and load
 * an OAuthData/refresh token pair for a set of users.
 */
@JsonClass(generateAdapter = true)
data class PersistedAuthData(
    val latest: OAuthData?,
    val refreshToken: String?,
) : Serializable {

    /**
     * Attempts to simplify the data contained in this reference.
     *
     * Returns null if this object is not significant ([.isSignificant] returns false). If the OAuthData is
     * expired, this method returns a new object with a null OAuthData.
     *
     * If nothing can be simplified, this object is returned.
     */
    fun simplify(): PersistedAuthData? {
        return when {
            !isSignificant -> null
            latest != null && latest.isExpired -> PersistedAuthData(latest = null, refreshToken)
            else -> this
        }
    }

    /**
     * This object is said to be significant if there is either some non-null, unexpired OAuthData or a non-null refresh
     * token.
     */
    val isSignificant: Boolean
        get() = (latest != null && !latest.isExpired) || refreshToken != null
}
