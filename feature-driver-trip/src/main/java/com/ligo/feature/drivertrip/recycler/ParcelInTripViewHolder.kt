package com.ligo.feature.drivertrip.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ligo.common.BaseViewHolder
import com.ligo.common.model.ParcelInTripUiModel
import com.ligo.common.model.StatusUiModel
import com.ligo.core.loadImageWithGlide
import com.ligo.feature.drivertrip.databinding.ListItemParcelInTripBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

class ParcelInTripViewHolder(
    binding: ListItemParcelInTripBinding,
    onItemClickSubject: Subject<ParcelInTripUiModel>,
    override val localizationManager: ILocalizationManager,
) : BaseViewHolder<ParcelInTripUiModel, ListItemParcelInTripBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {

    companion object {
        fun fromParent(
            parent: ViewGroup,
            onItemClickSubject: Subject<ParcelInTripUiModel>,
            localizationManager: ILocalizationManager,
        ): ParcelInTripViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ParcelInTripViewHolder(
                ListItemParcelInTripBinding.inflate(inflater, parent, false),
                onItemClickSubject,
                localizationManager
            )
        }
    }

    override fun initView(item: ParcelInTripUiModel, binding: ListItemParcelInTripBinding) {
        with(binding) {
            val context = root.context
            when (val status = item.status) {
                !is StatusUiModel.ParcelCanceled -> {
                    ivImage.loadImageWithGlide(item.parcelPhoto.orEmpty())
                    tvStatus.setLocalizedTextByKey(status.configStringKey)
                    tvStatus.setTextColor(ContextCompat.getColor(context, status.textColorRes))
                    ivStatus.setImageResource(status.iconRes)
                    cvStatus.setCardBackgroundColor(context.getColor(status.bgColorRes))

                    tvUnreadMessagesCount.isVisible = item.unreadMessageCount != 0
                    tvUnreadMessagesCount.text = item.unreadMessageCount.toString()

                    tvCity.text = item.cityName
                    tvAddress.text = item.address
                }

                else -> Unit
            }
        }
    }
}
