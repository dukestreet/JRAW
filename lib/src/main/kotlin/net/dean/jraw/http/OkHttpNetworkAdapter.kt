package net.dean.jraw.http

import okhttp3.*

/**
 * [NetworkAdapter] implementation backed by Square's fantastic [OkHttp](https://square.github.io/okhttp/)
 */
class OkHttpNetworkAdapter @JvmOverloads constructor(
    /** The value of the User-Agent header sent with every request */
    override var userAgent: UserAgent,
    /** A customized OkHttpClient instance. Defaults to a default client. */
    private val http: OkHttpClient = OkHttpClient()
) : NetworkAdapter {

    override fun execute(r: HttpRequest): HttpResponse {
        return HttpResponse(createCall(r).execute())
    }

    override fun connect(url: String, listener: WebSocketListener): WebSocket {
        val ws = http.newWebSocket(Request.Builder()
            .get()
            .url(url)
            .build(), listener)

        // Shutdown the ExecutorService so this program can terminate normally
        http.dispatcher.executorService.shutdown()

        return ws
    }

    private fun createCall(r: HttpRequest): Call =
        (if (r.basicAuth != null) createAuthenticatedClient(r.basicAuth) else http).newCall(compileRequest(r))

    private fun createAuthenticatedClient(data: BasicAuthData): OkHttpClient =
        http.newBuilder().authenticator(BasicAuthenticator(data)).build()

    private fun compileRequest(r: HttpRequest): Request =
        Request.Builder()
            .headers(r.headers.newBuilder().set("User-Agent", userAgent.value).build())
            .url(r.url)
            .method(r.method, r.body)
            .build()
}
