package com.ligo.feature.camera

import android.content.ContentValues
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.withBindingSafety
import com.ligo.core.R
import com.ligo.core.printError
import com.ligo.feature.camera.databinding.ActivityCameraBinding
import com.ligo.navigator.api.CameraTask
import com.ligo.tools.api.PickPhoto
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import java.text.SimpleDateFormat
import java.util.Locale

class CameraActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_CAMERA_TASK = "EXTRA_CAMERA_TASK"

        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private const val DIRECTORY_NAME = "Pictures/LiGO"
        private const val MIME_TYPE = "image/jpeg"
        private const val JPG_EXT = ".jpg"

        fun getIntent(activity: AppCompatActivity, cameraTask: CameraTask): Intent {
            return Intent(activity, CameraActivity::class.java).apply {
                putExtra(EXTRA_CAMERA_TASK, cameraTask)
            }
        }
    }

    private val viewModel by inject<CameraFragmentViewModel>()

    private val startedCompositeDisposable = CompositeDisposable()

    private val binding by lazy { ActivityCameraBinding.inflate(layoutInflater) }

    private var imageCapture: ImageCapture? = null
    private var closeDisposable: Disposable? = null
    private var qrScannerDisposable: Disposable? = null

    private val cameraTask: CameraTask? by lazy { intent?.getParcelableExtra(EXTRA_CAMERA_TASK) }

    @ExperimentalGetImage
    override fun onCreate(savedInstanceState: Bundle?) {
        loadKoinModules(CameraModule)
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        val overlayResId = when (cameraTask?.type ?: return) {
            PickPhoto.Type.PHOTO_PASSPORT -> R.drawable.passport_overlay
            PickPhoto.Type.SCAN_QR -> R.drawable.qr_overlay
            PickPhoto.Type.AVATAR -> R.drawable.avatar_overlay
            PickPhoto.Type.REGULAR_PHOTO -> null
        }
        overlayResId?.apply(binding.ivOverlay::setBackgroundResource)
        binding.btnBack.clipToOutline = true

        binding.viewFinder.viewTreeObserver.addOnGlobalLayoutListener(
            object : OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    startCamera()
                    binding.viewFinder.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        )

        viewModel.getOnCloseObservable()
            .subscribe({ finish() }, ::printError)
            .also { closeDisposable = it }
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            root.setOnThrottleClickListener(startedCompositeDisposable) { }
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                finish()
            }
            ivOverlay.setOnThrottleClickListener(startedCompositeDisposable) { takePhoto() }
        }
    }

    @ExperimentalGetImage
    private fun startCamera() {
        val viewFinder = binding.viewFinder
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        val executor = ContextCompat.getMainExecutor(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(viewFinder.surfaceProvider)

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = when (cameraTask?.type ?: return@addListener) {
                PickPhoto.Type.AVATAR -> CameraSelector.DEFAULT_FRONT_CAMERA
                else -> CameraSelector.DEFAULT_BACK_CAMERA
            }

            try {
                cameraProvider.unbindAll()
                if (cameraTask?.type == PickPhoto.Type.SCAN_QR) {
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setTargetResolution(Size(viewFinder.width, viewFinder.height))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                    val analyzer = QrCodeAnalyzer()
                    imageAnalysis.setAnalyzer(executor, analyzer)

                    qrScannerDisposable?.dispose()
                    analyzer.getOnQrCodeObservable()
                        .subscribeOn(Schedulers.io())
                        .filter { it.isNotEmpty() }.distinctUntilChanged()
                        .subscribe(::handleQr, ::printError)
                        .also { qrScannerDisposable = it }
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
                    binding.ivTakePhoto.isVisible = false
                } else {
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
                }
            } catch (e: Exception) {
                printError(e)
            }
        }, executor)
    }

    private fun handleQr(data: String) {
        if (data.isEmpty()) return
        viewModel.qrScanned(data)
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis())

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, MIME_TYPE)
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, DIRECTORY_NAME)
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            contentValues
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(e: ImageCaptureException) {
                    printError(e)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val realPath = Environment.getExternalStorageDirectory().absolutePath.plus("/")
                        .plus(DIRECTORY_NAME).plus("/").plus(name).plus(JPG_EXT)

                    val uri = output.savedUri
                    if (uri != null) {
                        viewModel.photoTaken(uri, realPath)
                    }
                }
            }
        )
    }

    override fun onDestroy() {
        unloadKoinModules(CameraModule)
        super.onDestroy()
        closeDisposable?.dispose()
        qrScannerDisposable?.dispose()
    }
}