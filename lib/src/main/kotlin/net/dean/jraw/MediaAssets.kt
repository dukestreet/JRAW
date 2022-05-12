package net.dean.jraw

import net.dean.jraw.http.HttpRequest
import net.dean.jraw.models.MediaUploadLeaseResponse
import net.dean.jraw.models.MediaUploadRequestBody
import net.dean.jraw.references.SubredditReference
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okio.BufferedSink

class MediaAssets(private val reddit: RedditClient) {
    /**
     * Uploads an image or a video to Reddit's storage. The uploaded media item can be posted
     * in a submission using [SubredditReference.submit].
     *
     * Uses undocumented APIs copied from PRAW so this may break in the future.
     * [Reference](https://github.com/praw-dev/praw/blob/af494cfc37d352ea97a0cb4d1b589b3a3ea42ef6/praw/models/reddit/subreddit.py#L653)
     */
    fun upload(body: MediaUploadRequestBody): MediaAssetUploadToken {
        val uploadLease = reddit.request {
            it.path("/api/media/asset.json")
            it.post(
                mapOf(
                    "filepath" to body.fileName,
                    "mimetype" to body.mediaType.toString()
                )
            )
        }.deserialize<MediaUploadLeaseResponse>()

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .apply {
                uploadLease.args.formDataParts.forEach {
                    addFormDataPart(name = it.name, value = it.value)
                }
            }
            .addFormDataPart(name = "file", filename = body.fileName, body = toOkHttpBody(body))
            .build()

        // Assume success if this request succeeds.
        // RedditClient will throw for non-2xx responses.
        reddit.request(
            HttpRequest.Builder()
                .rawJson(false)
                .url(uploadLease.args.uploadUrl)
                .post(requestBody)
                .build()
        )

        // todo: use xml serialization to get the url instead.
        return MediaAssetUploadToken(
            url = uploadLease.args.uploadUrl + "/" + uploadLease.args.formDataParts.single { it.name == "key" }.value,
            mediaType = body.mediaType,
            assetId = uploadLease.asset.id,
        )
    }

    private fun toOkHttpBody(body: MediaUploadRequestBody): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType = body.mediaType
            override fun contentLength(): Long = body.fileSize
            override fun writeTo(sink: BufferedSink) = body.writeTo(sink)
        }
    }
}

/**
 * A media that has been uploaded to Reddit's storage, but can't be
 * publicly viewed it until it is posted using [SubredditReference.submit].
 */
data class MediaAssetUploadToken(
    internal val url: String,
    internal val mediaType: MediaType,
    internal val assetId: String,
)
