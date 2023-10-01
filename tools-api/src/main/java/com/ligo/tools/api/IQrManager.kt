package com.ligo.tools.api

import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.core.Observable

interface IQrManager {

    fun setupActivity(activity: AppCompatActivity)

    fun generateQr(string: String): Bitmap

    fun scanQr(origin: ScanQR.Origin)

    fun qrScanned(data: String)

    fun showQr(data: String)

    fun getOnScannedQRObservable(): Observable<ScanQR>
}