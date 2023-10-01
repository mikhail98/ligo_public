package com.ligo.common.ligo

import androidx.core.view.isVisible
import com.ligo.common.databinding.LayoutAvatarAndRatingBinding
import com.ligo.common.databinding.LayoutOrderInfoBinding
import com.ligo.common.databinding.LayoutParcelParamsBinding
import com.ligo.common.setOnThrottleClickListener
import com.ligo.core.dpToPx
import com.ligo.core.loadImageWithGlide
import com.ligo.data.model.ConfigStringKey.ORDER_INFORMATION
import com.ligo.data.model.ConfigStringKey.PARCEL_PHOTO_TITLE
import com.ligo.data.model.ConfigStringKey.ROUTE
import com.ligo.data.model.Location
import com.ligo.data.model.Parcel
import com.ligo.data.model.User
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.Target
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.disposables.CompositeDisposable

class FragmentHelper(
    private val navigator: INavigator,
    private val localizationManager: ILocalizationManager,
    private val startedCompositeDisposable: CompositeDisposable,
) {

    fun setRouteInfo(
        binding: LayoutOrderInfoBinding,
        startPoint: Location,
        endPoint: Location,
        parcelPhotoUrl: String?,
    ) {
        parcelPhotoUrl?.let { url ->
            binding.ivImage.loadImageWithGlide(url)
            binding.tvImageTitle.text = localizationManager.getLocalized(PARCEL_PHOTO_TITLE)
        }
        binding.clPhoto.isVisible = parcelPhotoUrl != null

        val fromCityName = startPoint.cityName
        binding.tvFromCity.text = if (fromCityName != null) {
            fromCityName + " (" + startPoint.fullName + ")"
        } else {
            startPoint.fullName
        }
        binding.tvFromAddress.text = startPoint.address

        val toCityName = endPoint.cityName
        binding.tvToCity.text = if (toCityName != null) {
            toCityName + " (" + endPoint.fullName + ")"
        } else {
            endPoint.fullName
        }
        binding.tvToAddress.text = endPoint.address

        val titleKey = if (parcelPhotoUrl == null) ROUTE else ORDER_INFORMATION
        binding.tvTitle.text = localizationManager.getLocalized(titleKey)

        binding.clStartPoint.setOnThrottleClickListener(startedCompositeDisposable) {
            navigator.open(
                Target.MapApp(
                    startPoint.latitude,
                    startPoint.longitude,
                    startPoint.address.orEmpty()
                )
            )
        }

        binding.clEndPoint.setOnThrottleClickListener(startedCompositeDisposable) {
            navigator.open(
                Target.MapApp(
                    endPoint.latitude,
                    endPoint.longitude,
                    endPoint.address.orEmpty()
                )
            )
        }
    }

    fun setParcelParams(
        binding: LayoutParcelParamsBinding,
        parcel: Parcel,
        onSizeClick: () -> Unit = {},
    ) {
        binding.tvPrice.text = with(parcel.price) { "$value $currency" }
        binding.tvWeight.apply {
            isVisible = parcel.weight != null
            text = "${parcel.weight} kg"
        }
        binding.tvSize.text = parcel.types.joinToString(",")

        binding.tvPrice.clipToOutline = true
        binding.tvWeight.clipToOutline = true
        binding.tvSize.clipToOutline = true

        binding.tvPrice.setOnThrottleClickListener(startedCompositeDisposable) { }
        binding.tvWeight.setOnThrottleClickListener(startedCompositeDisposable) { }
        binding.tvSize.setOnThrottleClickListener(startedCompositeDisposable) { onSizeClick.invoke() }
    }

    fun setAvatarAndRating(
        binding: LayoutAvatarAndRatingBinding,
        user: User,
    ) {
        with(binding) {
            tvUser.text = user.name
            setAvatar(ivAvatar, user.avatarPhoto, 8.dpToPx())
            setRating(user, rbRating, tvUserReviews, localizationManager)
        }
    }
}