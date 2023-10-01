package com.ligo.data.socket

import com.ligo.core.Initializable
import com.ligo.data.BuildConfig
import com.ligo.data.Constants
import com.ligo.data.model.LocationUpdate
import com.ligo.data.model.Message
import com.ligo.data.model.MessagesWereReadPayload
import com.ligo.data.model.Parcel
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.socket.event.IncomingSocketEvent
import com.ligo.data.socket.event.OutcomingSocketEvent
import com.ligo.data.socket.utils.emitEvent
import com.ligo.data.socket.utils.onEvent
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import io.socket.client.IO
import io.socket.client.Socket

internal class SocketService(private val appPreferences: IAppPreferences) : ISocketService {

    companion object {
        private const val IO_OPTIONS_TRANSPORTS_WEBSOCKET = "websocket"
        private const val IO_OPTIONS_TRANSPORTS_POLLING = "polling"
    }

    override val initOnList: List<Initializable.On> = listOf(Initializable.On.CREATE)
    override val connectOnList: List<Initializable.On> = listOf(Initializable.On.LOGIN)
    override val clearOnList: List<Initializable.On> = listOf(Initializable.On.LOGOUT)

    private var socket: Socket? = null

    private val incomingEventSubject: Subject<IncomingSocketEvent> =
        PublishSubject.create<IncomingSocketEvent>().toSerialized()

    override fun init() {
        if (socket != null) return
        socket = IO.socket(Constants.getBaseSocketUrl(), getOptions())
        socket?.on(Socket.EVENT_CONNECT) {
            if (BuildConfig.DEBUG) {
                println("LOGRE:: socket connected at: ${Constants.getBaseSocketUrl()}")
            }
            appPreferences.getUser()?.apply {
                emitEvent(
                    OutcomingSocketEvent.EnterSocket(
                        OutcomingSocketEvent.EnterSocket.Payload(_id)
                    )
                )
            }
        }
        socket?.onEvent(
            IncomingSocketEvent.SOCKET_ENTERED,
            IncomingSocketEvent.SocketEntered.Payload::class.java
        ) {
            handleSocketEnteredEvent(this)
        }
        socket?.onEvent(IncomingSocketEvent.PARCEL_AVAILABLE, Parcel::class.java) {
            handleParcelAvailableEvent(this)
        }
        socket?.onEvent(IncomingSocketEvent.PARCEL_ACCEPTED, Parcel::class.java) {
            handleParcelAccepted(this)
        }
        socket?.onEvent(IncomingSocketEvent.PARCEL_CANCELLED, Parcel::class.java) {
            handleParcelCancelled(this)
        }
        socket?.onEvent(IncomingSocketEvent.PARCEL_REJECTED, Parcel::class.java) {
            handleParcelRejected(this)
        }
        socket?.onEvent(IncomingSocketEvent.PARCEL_PICKED, Parcel::class.java) {
            handleParcelPicked(this)
        }
        socket?.onEvent(IncomingSocketEvent.PARCEL_DELIVERED, Parcel::class.java) {
            handleParcelDelivered(this)
        }
        socket?.onEvent(IncomingSocketEvent.DRIVER_LOCATION_UPDATED, LocationUpdate::class.java) {
            handleDriverLocationUpdated(this)
        }
        socket?.onEvent(IncomingSocketEvent.MESSAGE_RECEIVED, Message::class.java) {
            handleMessageReceived(this)
        }
        socket?.onEvent(
            IncomingSocketEvent.MESSAGES_WERE_READ,
            MessagesWereReadPayload::class.java
        ) {
            handleMessagesWereRead(this)
        }

        socket?.on(Socket.EVENT_DISCONNECT) {
            if (BuildConfig.DEBUG) {
                println("LOGRE:: socket disconnected")
            }
        }
    }

    private fun getOptions() = IO.Options().apply {
        transports = arrayOf(
            IO_OPTIONS_TRANSPORTS_WEBSOCKET,
            IO_OPTIONS_TRANSPORTS_POLLING
        )
    }

    override fun connect() {
        if (socket?.connected() == false) {
            println("LOGRE:: socket connection at: ${Constants.getBaseSocketUrl()}")
            socket?.connect()
        }
    }

    override fun emitEvent(event: OutcomingSocketEvent) {
        socket?.emitEvent(event)
    }

    override fun clear() {
        socket?.disconnect()
    }

    private fun handleSocketEnteredEvent(data: IncomingSocketEvent.SocketEntered.Payload) {
        incomingEventSubject.onNext(IncomingSocketEvent.SocketEntered(data.isSuccess))
    }

    private fun handleParcelAvailableEvent(data: Parcel) {
        incomingEventSubject.onNext(IncomingSocketEvent.ParcelAvailable(data))
    }

    private fun handleParcelAccepted(data: Parcel) {
        incomingEventSubject.onNext(IncomingSocketEvent.ParcelAccepted(data))
    }

    private fun handleParcelPicked(data: Parcel) {
        incomingEventSubject.onNext(IncomingSocketEvent.ParcelPicked(data))
    }

    private fun handleParcelDelivered(data: Parcel) {
        incomingEventSubject.onNext(IncomingSocketEvent.ParcelDelivered(data))
    }

    private fun handleParcelCancelled(data: Parcel) {
        incomingEventSubject.onNext(IncomingSocketEvent.ParcelCancelled(data))
    }

    private fun handleParcelRejected(data: Parcel) {
        incomingEventSubject.onNext(IncomingSocketEvent.ParcelRejected(data))
    }

    private fun handleDriverLocationUpdated(data: LocationUpdate) {
        incomingEventSubject.onNext(IncomingSocketEvent.DriverLocationUpdated(data))
    }

    private fun handleMessageReceived(data: Message) {
        incomingEventSubject.onNext(IncomingSocketEvent.MessageReceived(data))
    }

    private fun handleMessagesWereRead(data: MessagesWereReadPayload) {
        incomingEventSubject.onNext(IncomingSocketEvent.MessagesWereRead(data.chatId))
    }

    override fun getOnIncomingEventObservable(): Observable<IncomingSocketEvent> =
        incomingEventSubject
}