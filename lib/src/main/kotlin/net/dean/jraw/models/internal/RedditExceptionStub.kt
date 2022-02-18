package net.dean.jraw.models.internal

import net.dean.jraw.RedditException
import net.dean.jraw.http.NetworkException

interface RedditExceptionStub<T : RedditException> {
    fun containsError(): Boolean
    fun create(cause: NetworkException): T?
}
