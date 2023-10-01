package com.ligo.tools

import android.content.Context
import android.graphics.Bitmap
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ligo.core.PermissionChecker
import com.ligo.navigator.api.CameraTask
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IQrManager
import com.ligo.tools.api.PickPhoto
import com.ligo.tools.api.ScanQR
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject
import net.glxn.qrgen.android.QRCode

internal class QrManager(
    private val context: Context,
    private val navigator: INavigator,
) : IQrManager {

    companion object {
        private const val QR_CODE_SIZE = 512
    }

    private val scannedQRSubject: Subject<ScanQR> =
        PublishSubject.create<ScanQR>().toSerialized()

    private var permissionsLauncher: ActivityResultLauncher<Array<String>>? = null
    private var origin: ScanQR.Origin? = null

    override fun setupActivity(activity: AppCompatActivity) {
        permissionsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.entries.all { it.value }) {
                scanQr()
            }
        }
    }

    override fun generateQr(string: String): Bitmap {
        return QRCode.from(string).withSize(QR_CODE_SIZE, QR_CODE_SIZE).bitmap()
    }

    override fun scanQr(origin: ScanQR.Origin) {
        this.origin = origin
        val permissionsResult =
            PermissionChecker.isCameraPermissionEnabled(context)
        if (permissionsResult.first) {
            scanQr()
        } else {
            permissionsLauncher?.launch(permissionsResult.second)
        }
    }

    private fun scanQr() {
        navigator.open(Target.Camera(CameraTask(type = PickPhoto.Type.SCAN_QR)))
    }

    override fun qrScanned(data: String) {
        val origin = origin
        if (origin != null) {
            scannedQRSubject.onNext(ScanQR(data, origin))
        } else {
            scannedQRSubject.onError(Throwable())
        }
    }

    override fun showQr(data: String) {
        navigator.open(Target.ParcelQr(data))
    }

    override fun getOnScannedQRObservable(): Observable<ScanQR> = scannedQRSubject
}