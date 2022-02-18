package net.dean.jraw.databind

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.adapter
import net.dean.jraw.models.Listing
import net.dean.jraw.models.SimpleFlairInfo
import java.lang.reflect.Type

class SimpleFlairInfoListingAdapterFactory : JsonAdapter.Factory {

    override fun create(type: Type, annotations: MutableSet<out Annotation>?, moshi: Moshi): JsonAdapter<*>? {
        return when (type) {
          TYPE -> SimpleFlairInfoListingAdapter(moshi)
          else -> null
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private class SimpleFlairInfoListingAdapter(moshi: Moshi) : JsonAdapter<Listing<SimpleFlairInfo>>() {
        val listingItemDelegate = moshi.adapter<SimpleFlairInfo>()

        override fun toJson(writer: JsonWriter, value: Listing<SimpleFlairInfo>?) {
            if (value == null) {
                writer.nullValue()
            } else {
                writer.beginObject()
                writer.name("users")
                writer.beginArray()
                value.forEach {
                    listingItemDelegate.toJson(writer, it)
                }
                writer.endArray()
                writer.name("next")
                writer.value(value.nextName)
                writer.endObject()
            }
        }

        override fun fromJson(reader: JsonReader): Listing<SimpleFlairInfo> {
            reader.beginObject()

            var kind = reader.nextName()

            // skip "prev" if it is present
            if (kind == "prev") {
                reader.nextString()
                kind = reader.nextName()
            }

            if (kind != "users")
                throw IllegalArgumentException("Expecting `users` to be at ${reader.path}")

            val children: MutableList<SimpleFlairInfo> = ArrayList()

            reader.beginArray()
            while (reader.hasNext())
                children.add(listingItemDelegate.fromJson(reader)!!)
            reader.endArray()

            var next: String? = null
            if (reader.hasNext() && reader.nextName() == "next")
                next = if (reader.peek() == JsonReader.Token.NULL) reader.nextNull() else reader.nextString()

            reader.endObject()

            return Listing(nextName = next, children = children)
        }
    }

    /** */
    companion object {
        @JvmStatic private val TYPE = Types.newParameterizedType(Listing::class.java, SimpleFlairInfo::class.java)
    }

}
