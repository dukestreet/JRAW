package net.dean.jraw.models

import java.util.Locale

/**
 * All of the values in this enum can be returned by the reddit API, except for [.URL] and [.NONE].
 * If `URL` is returned, then Reddit has created a thumbnail for specifically for that post. If
 * `NONE` is returned, then there is no thumbnail available.
 */
enum class ThumbnailType {
    /** For when a post is marked as NSFW  */
    NSFW,

    /** For when reddit couldn't create one  */
    DEFAULT,

    /** For self posts  */
    SELF,

    /** No thumbnail  */
    NONE,

    /** A custom thumbnail that can be accessed by calling [Submission.getThumbnail]  */
    URL;

    companion object {
        fun parse(value: String?): ThumbnailType {
            return if (value == null || value.isEmpty()) {
                NONE
            } else try {
                valueOf(value.uppercase(Locale.US))
            } catch (e: IllegalArgumentException) {
                // "thumbnail"'s value is a URL
                URL
            }
        }
    }
}
