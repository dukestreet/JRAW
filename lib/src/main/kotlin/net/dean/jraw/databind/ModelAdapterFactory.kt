package net.dean.jraw.databind

import com.ryanharter.auto.value.moshi.MoshiAdapterFactory
import com.squareup.moshi.JsonAdapter

/**
 * Add this factory to your `Moshi.Builder` to enable serializing all `@AutoValue` models. See
 * [here](https://github.com/rharter/auto-value-moshi#factory) for more details.
 */
@MoshiAdapterFactory
abstract class ModelAdapterFactory : JsonAdapter.Factory {
    companion object {
        fun create(): JsonAdapter.Factory {
            return AutoValueMoshi_ModelAdapterFactory()
        }
    }
}
