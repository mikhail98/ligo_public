package com.ligo.data.socket.event

import com.google.gson.annotations.SerializedName
import com.ligo.data.model.LocationUpdate
import com.ligo.data.model.Message
import com.ligo.data.model.Parcel

sealed class IncomingSocketEvent(eventName: String) : SocketEvent(eventName) {

    companion object {
        const val SOCKET_ENTERED = "socketEntered"
        const val PARCEL_AVAILABLE = "parcelAvailable"
        const val PARCEL_ACCEPTED = "parcelAccepted"
        const val PARCEL_CANCELLED = "parcelCancelled"
        const val PARCEL_REJECTED = "parcelRejected"
        const val PARCEL_PICKED = "parcelPicked"
        const val PARCEL_DELIVERED = "parcelDelivered"
        const val DRIVER_LOCATION_UPDATED = "driverLocationUpdated"
        const val MESSAGE_RECEIVED = "newMessage"
        const val MESSAGES_WERE_READ = "messagesWereRead"
    }

    data class SocketEntered(
        val isSuccess: Boolean,
    ) : IncomingSocketEvent(SOCKET_ENTERED) {
        data class Payload(
            @SerializedName("isSuccess") val isSuccess: Boolean,
        )
    }

    data class ParcelAvailable(
        val parcel: Parcel,
    ) : IncomingSocketEvent(PARCEL_AVAILABLE)

    data class ParcelCancelled(
        val parcel: Parcel,
    ) : IncomingSocketEvent(PARCEL_CANCELLED)

    data class ParcelRejected(
        val parcel: Parcel,
    ) : IncomingSocketEvent(PARCEL_REJECTED)

    data class ParcelAccepted(
        val parcel: Parcel,
    ) : IncomingSocketEvent(PARCEL_ACCEPTED)

    data class ParcelPicked(
        val parcel: Parcel,
    ) : IncomingSocketEvent(PARCEL_PICKED)

    data class ParcelDelivered(
        val parcel: Parcel,
    ) : IncomingSocketEvent(PARCEL_DELIVERED)

    data class DriverLocationUpdated(
        val locationUpdate: LocationUpdate,
    ) : IncomingSocketEvent(DRIVER_LOCATION_UPDATED)

    data class MessageReceived(
        val message: Message
    ) : IncomingSocketEvent(MESSAGE_RECEIVED)

    data class MessagesWereRead(
        val chatId: String
    ) : IncomingSocketEvent(MESSAGES_WERE_READ)
}
