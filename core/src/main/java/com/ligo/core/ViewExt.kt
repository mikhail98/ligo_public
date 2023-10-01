package com.ligo.core

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition

fun ImageView.loadImageWithGlide(@DrawableRes res: Int) {
    Glide.with(context).load(res).into(this)
}

fun ImageView.loadImageWithGlide(url: String, @DrawableRes placeholder: Int? = null) {
    Glide.with(context)
        .load(url)
        .apply { placeholder?.let(this::placeholder) }
        .into(this)
}

fun Context.loadRoundImageWithGlide(iconUrl: String, onResourceLoaded: (Bitmap) -> Unit) {
    Glide.with(this)
        .asBitmap()
        .centerCrop()
        .circleCrop()
        .load(iconUrl)
        .into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(
                resource: Bitmap,
                transition: Transition<in Bitmap>?,
            ) {
                onResourceLoaded.invoke(resource)
            }

            override fun onLoadCleared(placeholder: Drawable?) = Unit
        })
}

fun TextView.setTextColorByRes(@ColorRes res: Int) {
    setTextColor(ContextCompat.getColor(context, res))
}

fun TextView.setLinkTextColorByRes(@ColorRes res: Int) {
    setLinkTextColor(ContextCompat.getColor(context, res))
}