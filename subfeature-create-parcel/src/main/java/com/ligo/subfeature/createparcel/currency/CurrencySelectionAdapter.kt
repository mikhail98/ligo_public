package com.ligo.subfeature.createparcel.currency

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ligo.google.api.RemoteConfigCurrency
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class CurrencySelectionAdapter(
    private val localizationManager: ILocalizationManager,
    private var items: List<CurrencyItemUi> = listOf()
) : RecyclerView.Adapter<CurrencySelectionViewHolder>() {

    private val onItemClickSubject: Subject<CurrencyItemUi> =
        PublishSubject.create<CurrencyItemUi>().toSerialized()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencySelectionViewHolder =
        CurrencySelectionViewHolder.fromParent(parent, onItemClickSubject, localizationManager)

    override fun onBindViewHolder(holder: CurrencySelectionViewHolder, position: Int) {
        holder.bindItem(items[position])
    }

    override fun getItemCount() = items.size

    fun updateCurrencyList(newList: List<CurrencyItemUi>) {
        this.items = newList
        notifyDataSetChanged()
    }

    fun selectCurrency(selectedCurrency: RemoteConfigCurrency) {
        val selectedIndex = items.indexOfFirst { it.isSelected }
        items.forEach { it.isSelected = it.currency == selectedCurrency }
        if (selectedIndex != -1) notifyItemChanged(selectedIndex)
    }

    fun getOnItemClickObservable(): Observable<CurrencyItemUi> = onItemClickSubject
}