package com.ligo.feature.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.ligo.core.printError
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

@ExperimentalGetImage
class QrCodeAnalyzer : ImageAnalysis.Analyzer {

    private val qrCodeSubject: Subject<String> = PublishSubject.create<String>().toSerialized()

    override fun analyze(imageProxy: ImageProxy) {
        val options =
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()

        val scanner = BarcodeScanning.getClient(options)
        val mediaImage = imageProxy.image
        mediaImage?.apply {
            val image = InputImage.fromMediaImage(this, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    barcodes.firstOrNull()?.rawValue?.apply(qrCodeSubject::onNext)
                }
                .addOnFailureListener(::printError)
                .addOnCompleteListener {
                    close()
                    imageProxy.close()
                }
        }
    }

    fun getOnQrCodeObservable(): Observable<String> = qrCodeSubject
}