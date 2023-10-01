package com.ligo.tools.api

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import io.reactivex.rxjava3.core.Observable

interface IPhotoManager {

    fun setupActivity(activity: AppCompatActivity)

    fun takePhoto(
        source: PickPhoto.Source,
        origin: PickPhoto.Origin,
        type: PickPhoto.Type = PickPhoto.Type.REGULAR_PHOTO,
    )

    fun photoTaken(uri: Uri?, realPath: String?)

    fun getOnPhotoPickedObservable(): Observable<PickPhoto>
}