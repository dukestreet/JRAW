package net.dean.jraw.models

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import net.dean.jraw.models.internal.SubredditElement
import java.io.Serializable

@JsonClass(generateAdapter = true)
data class MultiredditPatch(
    /** Markdown-formatted text  */
    @Json(name = "description_md")
    val description: String? = null,

    /** The name this multireddit will go by  */
    @Json(name = "display_name")
    val displayName: String? = null,

    /**
     * According to the API, one of:
     *
     *  * `art and design`
     *  * `ask`
     *  * `books`
     *  * `business`
     *  * `cars`
     *  * `comics`
     *  * `cute animals`
     *  * `diy`
     *  * `entertainment`
     *  * `food and drink`
     *  * `funny`
     *  * `games`
     *  * `grooming`
     *  * `health`
     *  * `life advice`
     *  * `military`
     *  * `models pinup`
     *  * `music`
     *  * `news`
     *  * `philosophy`
     *  * `pictures and gifs`
     *  * `science`
     *  * `shopping`
     *  * `sports`
     *  * `style`
     *  * `tech`
     *  * `travel`
     *  * `unusual stories`
     *  * `video`
     *  * `(empty string)`
     *
     */
    @Json(name = "icon_name")
    val iconName: String? = null,

    /**
     * A hex-formatted hex string, like `#CEE3F8`. This color is primarily used when viewing the multireddit on the
     * mobile site.
     */
    @Json(name = "key_color")
    val keyColor: String? = null,

    /** A list of subreddits to include in this multireddit. Do not include the `/r/` prefix.  */
    @Json(name = "subreddits")
    val subreddits: List<SubredditElement>? = null,

    /** One of `public`, `private`, or `hidden`  */
    val visibility: String? = null,

    /** Either `classic` or `fresh`  */
    val weightingScheme: String? = null,
) : Serializable {

    @Deprecated("Use MultiredditPatch's constructor instead.")
    class Builder {
        private var description: String? = null
        private var displayName: String? = null
        private var iconName: String? = null
        private var keyColor: String? = null
        private var subreddits: List<String>? = null
        private var visibility: String? = null
        private var weightingScheme: String? = null

        fun description(description: String?): Builder {
            this.description = description
            return this
        }

        fun displayName(displayName: String?): Builder {
            this.displayName = displayName
            return this
        }

        fun iconName(iconName: String?): Builder {
            this.iconName = iconName
            return this
        }


        fun keyColor(keyColor: String?): Builder {
            this.keyColor = keyColor
            return this
        }

        fun subreddits(subreddits: List<String>?): Builder {
            this.subreddits = subreddits
            return this
        }

        fun subreddits(vararg subreddits: String): Builder {
            return subreddits(listOf(*subreddits))
        }

        fun visibility(visibility: String?): Builder {
            this.visibility = visibility
            return this
        }

        fun weightingScheme(weightingScheme: String?): Builder {
            this.weightingScheme = weightingScheme
            return this
        }

        fun build(): MultiredditPatch {
            return MultiredditPatch(
                description = description,
                displayName = displayName,
                iconName = iconName,
                keyColor = keyColor,
                subreddits = subreddits?.map(::SubredditElement),
                visibility = visibility,
                weightingScheme = weightingScheme
            )
        }
    }
}
