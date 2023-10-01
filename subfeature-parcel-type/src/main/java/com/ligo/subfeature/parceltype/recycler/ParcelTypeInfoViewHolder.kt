package com.ligo.subfeature.parceltype.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ligo.common.BaseViewHolder
import com.ligo.common.model.ParcelTypeUiModel
import com.ligo.common.withBindingSafety
import com.ligo.subfeature.parceltype.databinding.ListItemParcelTypeInfoBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.PublishSubject

class ParcelTypeInfoViewHolder(
    private val binding: ListItemParcelTypeInfoBinding,
    override val localizationManager: ILocalizationManager,
) : BaseViewHolder<ParcelTypeUiModel, ListItemParcelTypeInfoBinding>(
    binding,
    PublishSubject.create(),
    localizationManager
) {

    companion object {
        fun fromParent(
            parent: ViewGroup,
            localizationManager: ILocalizationManager,
        ): ParcelTypeInfoViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ParcelTypeInfoViewHolder(
                ListItemParcelTypeInfoBinding.inflate(inflater, parent, false),
                localizationManager
            )
        }
    }

    override fun bindItem(item: ParcelTypeUiModel) {
        super.bindItem(item)
        withBindingSafety(binding) {
            ivIcon.setImageResource(item.iconResId)
            tvParcelType.setLocalizedTextByKey(item.titleKey)
            tvParcelTypeDescription.setLocalizedTextByKey(item.descriptionKey)
        }
    }

    override fun initView(item: ParcelTypeUiModel, binding: ListItemParcelTypeInfoBinding) {
        // do nothing
    }
}