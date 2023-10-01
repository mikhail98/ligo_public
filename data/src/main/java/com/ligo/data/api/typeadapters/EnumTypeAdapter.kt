package com.ligo.data.api.typeadapters

import android.util.Log
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

internal class EnumTypeAdapter(
    private val classOfT: Class<*>,
    private val delegate: TypeAdapter<Enum<*>>
) : TypeAdapter<Enum<*>>() {

    override fun write(writer: JsonWriter?, value: Enum<*>?) {
        delegate.write(writer, value)
    }

    @Suppress("UNCHECKED_CAST")
    override fun read(reader: JsonReader?): Enum<*>? {
        val value: Enum<*>? = delegate.read(reader)

        return value ?: defaultValue(classOfT, classOfT.enumConstants as Array<Enum<*>>)
    }

    private fun defaultValue(classOfT: Class<*>, enumConstants: Array<Enum<*>>): Enum<*>? {
        val defaultValueList = enumConstants.filter {
            val field = classOfT.getField(it.name)
            field.isAnnotationPresent(DefaultValue::class.java)
        }

        when {
            defaultValueList.isEmpty() -> logError(NoDefaultValueForEnumException(classOfT))
            defaultValueList.size > 1 -> logError(MultipleDefaultValuesEnumException(classOfT, defaultValueList))
        }

        return defaultValueList.firstOrNull()
    }

    private fun logError(exception: IllegalStateException) {
        Log.e("LOGRE: ", "EnumTypeAdapter: ${exception.printStackTrace()}")
    }
}
