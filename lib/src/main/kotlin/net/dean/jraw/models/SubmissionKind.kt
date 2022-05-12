package net.dean.jraw.models

import net.dean.jraw.MediaAssetUploadToken

enum class SubmissionKind {
    LINK, SELF
}

sealed interface SubmissionKind2 {
    data class Link(val url: String) : SubmissionKind2
    data class SelfText(val text: String) : SubmissionKind2

    /**
     * @param submissionFullName See [Submission.fullName].
     */
    data class CrossPost(val submissionFullName: String) : SubmissionKind2

    data class Media(val items: List<MediaAssetUploadToken>) : SubmissionKind2
}
