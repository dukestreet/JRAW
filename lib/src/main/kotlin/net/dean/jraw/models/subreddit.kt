@file:kotlin.jvm.JvmName("Subreddits")

package net.dean.jraw.models

fun Subreddit.nonEmptyIconUrl(): String? {
    return if (!communityIconUrl.isNullOrBlank()) {
        communityIconUrl
    } else if (!iconImageUrl.isNullOrBlank()) {
        iconImageUrl
    } else {
        null
    }
}
