package com.ligo.feature.searchplace.adapter

import android.location.Location
import com.ligo.common.BaseViewHolder
import com.ligo.common.withBindingSafety
import com.ligo.core.R
import com.ligo.core.loadImageWithGlide
import com.ligo.feature.searchplace.databinding.ListItemLocationSearchBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

class LocationSearchViewHolder(
    binding: ListItemLocationSearchBinding,
    private val userLocation: Location?,
    onItemClickSubject: Subject<SearchItem>,
    override val localizationManager: ILocalizationManager,
) : BaseViewHolder<SearchItem, ListItemLocationSearchBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {
    override fun initView(item: SearchItem, binding: ListItemLocationSearchBinding) {
        val location = item.location ?: return
        withBindingSafety(binding) {
            val iconUrl = item.iconUrl
            if (iconUrl != null) {
                ivIcon.loadImageWithGlide(iconUrl)
            } else {
                ivIcon.loadImageWithGlide(R.drawable.ic_pin_2)
            }
            tvName.text = location.cityName
            tvAddress.text = location.address

            val placeLocation = Location("").apply {
                latitude = location.latitude
                longitude = location.longitude
            }

            userLocation?.apply {
                val distance = placeLocation.distanceTo(this) / 1000
                tvDistance.text = String.format(
                    root.context.getString(R.string.round_with_2_digits),
                    distance
                )
            }
            if (userLocation == null) tvDistance.text = ""
        }
    }
}