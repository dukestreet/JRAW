package net.dean.jraw.databind

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import net.dean.jraw.models.internal.GenericJsonResponse
import net.dean.jraw.models.internal.ObjectBasedApiExceptionStub
import net.dean.jraw.models.internal.RedditExceptionStub
import java.lang.reflect.Type
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

/**
 * Deserializes [RedditExceptionStub] and its derivatives. API exceptions can come in a few flavors:
 */
internal class RedditExceptionStubAdapterFactory : JsonAdapter.Factory {

    override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
        val rawType = Types.getRawType(type)
        if (!RedditExceptionStub::class.java.isAssignableFrom(rawType)) return null

        val adapters = listOf(
            moshi.nextAdapter<ObjectBasedApiExceptionStub>(skipPast = this, annotations),
            moshi.nextAdapter<GenericJsonResponse>(skipPast = this, annotations),
        )
        return StubAdapter(adapters)
    }

    private class StubAdapter(private val delegates: List<JsonAdapter<out RedditExceptionStub<*>>>) :
        JsonAdapter<RedditExceptionStub<*>>() {
        override fun fromJson(reader: JsonReader): RedditExceptionStub<*>? {
            if (reader.peek() == JsonReader.Token.BEGIN_OBJECT) {
                val jsonValue = reader.readJsonValue()

                // The suggested implementation is simpler to read but much slower
                @Suppress("LoopToCallChain")
                for (adapter in delegates) {
                    val stub = adapter.fromJsonValue(jsonValue)!!
                    if (stub.containsError())
                        return stub
                }

            }

            return null
        }

        override fun toJson(writer: JsonWriter?, value: RedditExceptionStub<*>?) {
            TODO("not implemented")
        }
    }
}

@OptIn(ExperimentalStdlibApi::class)
private inline fun <reified T> Moshi.nextAdapter(
    skipPast: JsonAdapter.Factory,
    annotations: MutableSet<out Annotation>
): JsonAdapter<T> {
    return nextAdapter(skipPast, typeOf<T>().javaType, annotations)
}
