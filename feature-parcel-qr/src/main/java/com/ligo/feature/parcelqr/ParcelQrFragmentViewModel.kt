package com.ligo.feature.parcelqr

import android.graphics.Bitmap
import com.ligo.common.BaseViewModel
import com.ligo.data.socket.ISocketService
import com.ligo.data.socket.event.IncomingSocketEvent
import com.ligo.google.api.IAnalytics
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IQrManager

class ParcelQrFragmentViewModel(
    navigator: INavigator,
    analytics: IAnalytics,
    private val qrManager: IQrManager,
    private val socketService: ISocketService,

) : BaseViewModel(navigator, analytics) {

    override fun onCreate() {
        socketService.getOnIncomingEventObservable()
            .subscribeAndDispose(::handleSocketEvent)
    }

    private fun handleSocketEvent(event: IncomingSocketEvent) {
        when (event) {
            is IncomingSocketEvent.ParcelPicked, is IncomingSocketEvent.ParcelRejected ->
                navigator.close(Target.ParcelQr::class.java)
            else -> Unit
        }
    }

    fun getQr(text: String): Bitmap {
        return qrManager.generateQr(text)
    }
}