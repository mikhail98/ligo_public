package com.ligo.feature.history.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ligo.common.model.HistoryUiModel
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class HistoryAdapter(
    private val localizationManager: ILocalizationManager,
    private val items: MutableList<HistoryUiModel> = mutableListOf(),
) : RecyclerView.Adapter<HistoryViewHolder>() {

    private val onItemClickSubject: Subject<HistoryUiModel> =
        PublishSubject.create<HistoryUiModel>().toSerialized()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        return HistoryViewHolder.fromParent(parent, onItemClickSubject, localizationManager)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bindItem(items[position])
    }

    override fun onViewRecycled(holder: HistoryViewHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    override fun getItemCount() = items.size

    fun setItems(data: List<HistoryUiModel>) {
        items.clear()
        items.addAll(data)
        notifyDataSetChanged()
    }

    fun getOnItemClickedObservable(): Observable<HistoryUiModel> = onItemClickSubject
}
