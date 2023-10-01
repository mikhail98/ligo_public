package com.ligo.common.ligo

import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.ligo.common.R
import com.ligo.core.dpToPx
import com.ligo.core.loadImageWithGlide
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.User
import com.ligo.tools.api.ILocalizationManager
import java.math.RoundingMode
import java.text.DecimalFormat

fun setAvatar(imageView: ImageView, url: String?, padding: Int = 8.dpToPx()) {
    if (url == null) {
        imageView.setImageResource(R.drawable.ic_profile_white)
        imageView.setPadding(padding, padding, padding, padding)
    } else {
        imageView.setPadding(0, 0, 0, 0)
        imageView.loadImageWithGlide(url)
    }
}

fun setRating(
    user: User,
    ratingBar: RatingBar,
    tvReviews: TextView,
    localizationManager: ILocalizationManager,
) {
    val rating = if (user.ratings.isNotEmpty()) {
        val average = user.ratings.map { it.rating }.average().toFloat()
        ratingBar.rating = average

        with(DecimalFormat("#.##")) {
            roundingMode = RoundingMode.CEILING
            format(average).toDouble().toString()
        }
    } else {
        ratingBar.rating = 0f
        "---"
    }

    tvReviews.text = tvReviews.context.getString(
        R.string.profile_reviews_count,
        rating,
        user.ratings.size.toString(),
        localizationManager.getLocalized(ConfigStringKey.REVIEWS).lowercase()
    )
}