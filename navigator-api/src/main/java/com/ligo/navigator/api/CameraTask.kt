package com.ligo.navigator.api

import android.os.Parcelable
import com.ligo.tools.api.PickPhoto
import kotlinx.parcelize.Parcelize

@Parcelize
class CameraTask(val type: PickPhoto.Type) : Parcelable