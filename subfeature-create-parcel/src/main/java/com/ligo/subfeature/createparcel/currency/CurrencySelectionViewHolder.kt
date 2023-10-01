package com.ligo.subfeature.createparcel.currency

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ligo.common.BaseViewHolder
import com.ligo.common.setOnThrottleClickListener
import com.ligo.subfeature.createparcle.databinding.ListItemCurrencyBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

class CurrencySelectionViewHolder(
    private val binding: ListItemCurrencyBinding,
    private val onItemClickSubject: Subject<CurrencyItemUi>,
    override val localizationManager: ILocalizationManager,
) : BaseViewHolder<CurrencyItemUi, ListItemCurrencyBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {

    companion object {

        fun fromParent(
            parent: ViewGroup,
            onItemClickSubject: Subject<CurrencyItemUi>,
            localizationManager: ILocalizationManager,
        ): CurrencySelectionViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return CurrencySelectionViewHolder(
                ListItemCurrencyBinding.inflate(inflater, parent, false),
                onItemClickSubject,
                localizationManager
            )
        }
    }

    override fun bindItem(item: CurrencyItemUi) {
        super.bindItem(item)
        with(binding) {
            root.setOnThrottleClickListener(compositeDisposable) {
                if (!rbSelectedCurrency.isChecked) {
                    rbSelectedCurrency.isChecked = true
                }
            }
            initView(item, this)
        }
    }

    override fun initView(
        item: CurrencyItemUi,
        binding: ListItemCurrencyBinding,
    ) {
        with(binding) {
            ivCurrency.setImageResource(item.currency.iconRes)
            tvCurrencyCode.text = item.currency.code
            tvCurrencyName.setLocalizedTextByKey(item.currency.fullNameKey)
            rbSelectedCurrency.isChecked = item.isSelected
            rbSelectedCurrency.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) onItemClickSubject.onNext(item)
            }
        }
    }
}