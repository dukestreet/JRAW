package net.dean.jraw.models

enum class SubmissionKind {
    LINK, SELF
}

sealed interface SubmissionKind2 {
    data class Link(
        val url: String
    ) : SubmissionKind2

    data class SelfText(
        val text: String
    ) : SubmissionKind2

    data class CrossPost(
        val submission: Submission,
        val toSubreddit: Subreddit
    ): SubmissionKind2
}
