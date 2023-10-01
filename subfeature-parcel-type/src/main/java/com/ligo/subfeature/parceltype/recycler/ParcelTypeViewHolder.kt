package com.ligo.subfeature.parceltype.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ligo.common.BaseViewHolder
import com.ligo.common.model.ParcelTypeUiModel
import com.ligo.common.setOnThrottleClickListener
import com.ligo.subfeature.parceltype.databinding.ListItemParcelTypeBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

class ParcelTypeViewHolder(
    private val binding: ListItemParcelTypeBinding,
    private val onItemClickSubject: Subject<ParcelTypeUiModel>,
    override val localizationManager: ILocalizationManager,
) : BaseViewHolder<ParcelTypeUiModel, ListItemParcelTypeBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {

    companion object {

        fun fromParent(
            parent: ViewGroup,
            onItemClickSubject: Subject<ParcelTypeUiModel>,
            localizationManager: ILocalizationManager,
        ): ParcelTypeViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ParcelTypeViewHolder(
                ListItemParcelTypeBinding.inflate(inflater, parent, false),
                onItemClickSubject,
                localizationManager
            )
        }
    }

    override fun bindItem(item: ParcelTypeUiModel) {
        super.bindItem(item)
        with(binding) {
            root.setOnThrottleClickListener(compositeDisposable) {
                rbSelectedParcelType.isChecked = !rbSelectedParcelType.isChecked
            }
            initView(item, this)
        }
    }

    override fun initView(item: ParcelTypeUiModel, binding: ListItemParcelTypeBinding) {
        with(binding) {
            ivIcon.setImageResource(item.iconResId)
            tvParcelType.text =
                localizationManager.getLocalized(item.titleKey)
            tvParcelTypeDescription.text =
                localizationManager.getLocalized(item.descriptionKey)

            rbSelectedParcelType.isChecked = item.isChecked

            rbSelectedParcelType.setOnCheckedChangeListener { _, isChecked ->
                onItemClickSubject.onNext(item.copy(isChecked = isChecked))
            }
        }
    }
}