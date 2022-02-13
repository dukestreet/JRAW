package net.dean.jraw.databind

/**
 * This annotation's presence on a class symbolizes that the JSON is enveloped. See [RedditModelAdapterFactory]
 * for more.
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.CLASS)
annotation class RedditModel(
    /**
     * Most models are wrapped in an envelope that provides us with some basic type information. However, there are some
     * exceptions (namely [net.dean.jraw.models.WikiRevision].
     */
    val enveloped: Boolean = true
)
