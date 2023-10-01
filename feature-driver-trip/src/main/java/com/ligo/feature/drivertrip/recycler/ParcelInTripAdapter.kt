package com.ligo.feature.drivertrip.recycler

import android.annotation.SuppressLint
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ligo.common.model.ParcelInTripUiModel
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class ParcelInTripAdapter(
    private val localizationManager: ILocalizationManager,
) : RecyclerView.Adapter<ParcelInTripViewHolder>() {

    private val onItemClickSubject: Subject<ParcelInTripUiModel> =
        PublishSubject.create<ParcelInTripUiModel>().toSerialized()

    private val items = mutableListOf<ParcelInTripUiModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParcelInTripViewHolder {
        return ParcelInTripViewHolder.fromParent(parent, onItemClickSubject, localizationManager)
    }

    override fun onBindViewHolder(holder: ParcelInTripViewHolder, position: Int) {
        holder.bindItem(items[position])
    }

    override fun onViewRecycled(holder: ParcelInTripViewHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    override fun getItemCount() = items.size

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(newList: List<ParcelInTripUiModel>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun getOnItemClickedObservable(): Observable<ParcelInTripUiModel> = onItemClickSubject
}