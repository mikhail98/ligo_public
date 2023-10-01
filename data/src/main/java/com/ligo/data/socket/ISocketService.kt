package com.ligo.data.socket

import com.ligo.core.Initializable
import com.ligo.data.socket.event.IncomingSocketEvent
import com.ligo.data.socket.event.OutcomingSocketEvent
import io.reactivex.rxjava3.core.Observable

interface ISocketService : Initializable {

    fun emitEvent(event: OutcomingSocketEvent)

    fun getOnIncomingEventObservable(): Observable<IncomingSocketEvent>
}