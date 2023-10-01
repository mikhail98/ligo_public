package com.ligo.tools

import android.content.Context
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.ligo.core.PermissionChecker
import com.ligo.navigator.api.CameraTask
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.IPhotoManager
import com.ligo.tools.api.PickPhoto
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

internal class PhotoManager(
    private val context: Context,
    private val navigator: INavigator,
) : IPhotoManager {

    companion object {
        private const val INTENT_TYPE = "image/*"
    }

    private val photoSubject: Subject<PickPhoto> =
        PublishSubject.create<PickPhoto>().toSerialized()

    private var permissionsLauncher: ActivityResultLauncher<Array<String>>? = null
    private var galleryLauncher: ActivityResultLauncher<String>? = null
    private var source: PickPhoto.Source? = null
    private var origin: PickPhoto.Origin? = null
    private var type: PickPhoto.Type? = null

    override fun setupActivity(activity: AppCompatActivity) {
        galleryLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            photoTaken(uri, PickPhoto.Source.GALLERY, null)
        }
        permissionsLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            if (permissions.entries.all { it.value }) {
                when (source) {
                    PickPhoto.Source.CAMERA -> takePhotoFromCamera()
                    PickPhoto.Source.GALLERY -> takePhotoFromGallery()
                    else -> Unit
                }
            }
        }
    }

    override fun takePhoto(
        source: PickPhoto.Source,
        origin: PickPhoto.Origin,
        type: PickPhoto.Type,
    ) {
        this.source = source
        this.origin = origin
        this.type = type
        when (source) {
            PickPhoto.Source.CAMERA -> {
                val permissionsResult = PermissionChecker.isCameraPermissionEnabled(context)
                if (permissionsResult.first) {
                    takePhotoFromCamera()
                } else {
                    permissionsLauncher?.launch(permissionsResult.second)
                }
            }

            PickPhoto.Source.GALLERY -> {
                val permissionsResult = PermissionChecker.isMediaPermissionEnabled(context)
                if (permissionsResult.first) {
                    takePhotoFromGallery()
                } else {
                    permissionsLauncher?.launch(permissionsResult.second)
                }
            }
        }
    }

    private fun takePhotoFromCamera() {
        navigator.open(Target.Camera(CameraTask(type = type ?: return)))
    }

    private fun takePhotoFromGallery() {
        galleryLauncher?.launch(INTENT_TYPE)
    }

    override fun photoTaken(uri: Uri?, realPath: String?) {
        photoTaken(uri, PickPhoto.Source.CAMERA, realPath)
    }

    private fun photoTaken(uri: Uri?, source: PickPhoto.Source, realPath: String?) {
        val origin = origin
        if (uri != null && origin != null) {
            photoSubject.onNext(PickPhoto(uri, source, origin, realPath))
            this.source = null
            this.origin = null
            this.type = null
        }
    }

    override fun getOnPhotoPickedObservable(): Observable<PickPhoto> {
        return photoSubject
    }
}