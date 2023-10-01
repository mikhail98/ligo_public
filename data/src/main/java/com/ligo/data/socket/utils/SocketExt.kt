package com.ligo.data.socket.utils

import com.google.gson.Gson
import com.ligo.core.printError
import com.ligo.data.BuildConfig
import com.ligo.data.socket.event.OutcomingSocketEvent
import io.socket.client.Ack
import io.socket.client.Socket
import org.json.JSONObject

fun Socket.onEvent(event: String, listener: String.() -> Unit) {
    on(event) { array ->
        try {
            if (!array.isNullOrEmpty()) {
                val data = array.first().toString()
                printSocketInData(event, data)
                listener.invoke(data)
            } else {
                handleSocketError(event)
            }
        } catch (e: Exception) {
            printError(e)
            handleSocketError(event)
        }
    }
}

fun <VM> Socket.onEvent(event: String, clazz: Class<VM>, listener: VM.() -> Unit) {
    on(event) { array ->
        try {
            if (!array.isNullOrEmpty()) {
                val data = array.first().toString()
                printSocketInData(event, data)
                try {
                    listener.invoke(Gson().fromJson(data, clazz))
                } catch (e: Exception) {
                    handleSocketError(event, data)
                    printError(e)
                }
            } else {
                handleSocketError(event)
            }
        } catch (e: Exception) {
            printError(e)
            handleSocketError(event)
        }
    }
}

private fun printSocketInData(event: String, data: Any? = null) {
    if (BuildConfig.DEBUG) {
        println("LOGRE:: socket in -> $event ||||  data -> $data")
    }
}

private fun handleSocketError(event: String, data: Any? = null) {
    if (BuildConfig.DEBUG) {
        val dataString = data?.toString() ?: "NO_DATA"
        println("LOGRE:: data for event $event cannot be parsed. Data -> $dataString")
    }
}

fun Socket.emitEvent(
    event: OutcomingSocketEvent,
    listener: (String.() -> Unit)? = null,
) {
    val data = event.data
    val dataJson = when {
        data is String -> data
        data != null -> JSONObject(Gson().toJson(data))
        else -> null
    }
    if (BuildConfig.DEBUG) {
        println("LOGRE:: socket out -> ${event.eventName} |||| data -> $dataJson")
    }
    if (dataJson != null) {
        emit(event.eventName, dataJson, Ack { array -> handleAck(event, array, listener) })
    } else {
        emit(event.eventName, Ack { array -> handleAck(event, array, listener) })
    }
}

private fun handleAck(
    event: OutcomingSocketEvent,
    array: Array<Any>,
    listener: (String.() -> Unit)?,
) {
    array.firstOrNull()?.apply {
        if (BuildConfig.DEBUG) {
            println("LOGRE:: socket out -> ${event.eventName} |||| Ack -> $this")
        }
        listener?.invoke(this.toString())
    }
}