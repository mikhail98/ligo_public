package com.ligo.data.api.typeadapters

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken

internal class EnumTypeAdapterFactory : TypeAdapterFactory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val rawType = type.rawType

        return if (rawType.isEnum) {
            val delegate = gson.getDelegateAdapter(this, type) as TypeAdapter<Enum<*>>
            EnumTypeAdapter(rawType, delegate) as TypeAdapter<T>
        } else {
            null
        }
    }
}
