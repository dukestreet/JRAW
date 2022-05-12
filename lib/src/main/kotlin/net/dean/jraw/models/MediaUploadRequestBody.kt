package net.dean.jraw.models

import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink

class MediaUploadRequestBody(
    val fileName: String,
    val mediaType: MediaType,
    val fileSize: Long,
    val writeTo: (sink: BufferedSink) -> Unit
) {
    init {
        check(fileName.isNotBlank())
        check(fileSize > 0)
        check(mediaType.type == "image" || mediaType.type == "video")
    }
}
