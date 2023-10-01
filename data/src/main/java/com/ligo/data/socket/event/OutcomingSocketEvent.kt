package com.ligo.data.socket.event

import com.google.gson.annotations.SerializedName

sealed class OutcomingSocketEvent(val eventName: String, val data: Any?) : SocketEvent(eventName) {

    companion object {
        private const val ENTER_SOCKET = "enterSocket"
        private const val LEAVE_SOCKET = "leaveSocket"
    }

    class EnterSocket(data: Payload) : OutcomingSocketEvent(ENTER_SOCKET, data) {
        data class Payload(
            @SerializedName("userId") val userId: String,
        )
    }

    class LeaveSocket(data: Payload) : OutcomingSocketEvent(LEAVE_SOCKET, data) {
        data class Payload(
            @SerializedName("userId") val userId: String,
        )
    }
}