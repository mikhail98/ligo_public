package com.ligo.feature.history.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.ligo.common.BaseViewHolder
import com.ligo.common.model.HistoryUiModel
import com.ligo.common.model.StatusUiModel
import com.ligo.core.getFormattedDate
import com.ligo.core.getTimeInMillis
import com.ligo.feature.history.databinding.ListItemHistoryBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject
import com.ligo.common.R as CommonR
import com.ligo.core.R as CoreR

class HistoryViewHolder(
    binding: ListItemHistoryBinding,
    onItemClickListener: Subject<HistoryUiModel>,
    override val localizationManager: ILocalizationManager,
) : BaseViewHolder<HistoryUiModel, ListItemHistoryBinding>(
    binding,
    onItemClickListener,
    localizationManager
) {

    companion object {
        fun fromParent(
            parent: ViewGroup,
            onItemClickListener: Subject<HistoryUiModel>,
            localizationManager: ILocalizationManager,
        ): HistoryViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListItemHistoryBinding.inflate(inflater, parent, false)
            return HistoryViewHolder(binding, onItemClickListener, localizationManager)
        }
    }

    override fun initView(item: HistoryUiModel, binding: ListItemHistoryBinding) {
        val uiStatus = item.uiStatus
        with(binding) {
            if (uiStatus is StatusUiModel.TripActive) {
                clRoot.setBackgroundColor(clRoot.context.getColor(CoreR.color.accent))
                tvDate.setTextColor(clRoot.context.getColor(CoreR.color.white))
                ivIcon.setBackgroundResource(CommonR.drawable.bg_circle_accent)
            } else {
                clRoot.setBackgroundColor(clRoot.context.getColor(CoreR.color.bg_black))
                tvDate.setTextColor(clRoot.context.getColor(CoreR.color.gray_80))
                ivIcon.setBackgroundResource(0)
            }

            tvRoute.text = item.routeText
            ivIcon.setImageResource(uiStatus.mainIconRes)
            ivStatus.setImageResource(uiStatus.iconRes)
            tvStatus.setLocalizedTextByKey(uiStatus.configStringKey)
            tvStatus.setTextColor(ContextCompat.getColor(tvStatus.context, uiStatus.textColorRes))
            cvStatus.setCardBackgroundColor(cvStatus.context.getColor(uiStatus.bgColorRes))

            tvDate.text = getFormattedDate(getTimeInMillis(item.createdAt))
        }
    }
}
