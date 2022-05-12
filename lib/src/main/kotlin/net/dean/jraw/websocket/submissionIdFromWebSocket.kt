package net.dean.jraw.websocket

import com.squareup.moshi.JsonClass
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dean.jraw.JrawUtils
import net.dean.jraw.RedditClient
import net.dean.jraw.http.NetworkAdapter
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// Demo: https://regex101.com/r/eSX1vm/1
private val submissionRegex = Regex("/r/(?:[a-zA-Z0-9-_.]+)/comments/(\\w+)/?.*")

internal suspend fun RedditClient.readSubmissionIdFromWebSocket(
    webSocketUrl: String
): String {
    val serializedMessage = suspendCancellableCoroutine<String> { continuation ->
        val listener = object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                continuation.resume(text)
                webSocket.cancel()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                if (continuation.isActive) {
                    continuation.resumeWithException(t)
                }
            }
        }
        val socket = http.connect(webSocketUrl, listener)
        continuation.invokeOnCancellation {
            socket.cancel()
        }
    }

    val message = JrawUtils.adapter<SubmitPostWebSocketMessage>().fromJson(serializedMessage)!!
    val submissionUrl = message.payload.redirect
    return submissionRegex.find(submissionUrl)!!.groupValues[1]
}

@JsonClass(generateAdapter = true)
internal data class SubmitPostWebSocketMessage(
    val payload: Payload
) {

    @JsonClass(generateAdapter = true)
    data class Payload(
        val redirect: String
    )
}
