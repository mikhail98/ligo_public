package com.ligo.feature.rejectparcel

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ligo.common.BaseViewHolder
import com.ligo.core.R
import com.ligo.feature.rejectparcel.databinding.ListItemRejectReasonBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

class RejectParcelReasonViewHolder(
    binding: ListItemRejectReasonBinding,
    onItemClickListener: Subject<RejectReasonUi>,
    override val localizationManager: ILocalizationManager,
) : BaseViewHolder<RejectReasonUi, ListItemRejectReasonBinding>(
    binding,
    onItemClickListener,
    localizationManager
) {

    companion object {
        fun fromParent(
            parent: ViewGroup,
            onItemClickListener: Subject<RejectReasonUi>,
            localizationManager: ILocalizationManager,
        ): RejectParcelReasonViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListItemRejectReasonBinding.inflate(inflater, parent, false)
            return RejectParcelReasonViewHolder(binding, onItemClickListener, localizationManager)
        }
    }

    override fun initView(item: RejectReasonUi, binding: ListItemRejectReasonBinding) {
        with(binding) {
            ivSelected.setImageResource(
                if (item.isChecked) R.drawable.rb_checked else R.drawable.rb_unchecked
            )
            tvReason.text = getLocalizedStringByKey(item.textConfigStringKey)
        }
    }
}