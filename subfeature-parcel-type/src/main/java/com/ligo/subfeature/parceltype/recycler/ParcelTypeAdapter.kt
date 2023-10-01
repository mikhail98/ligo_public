package com.ligo.subfeature.parceltype.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ligo.common.BaseViewHolder
import com.ligo.common.model.ParcelTypeUiModel
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class ParcelTypeAdapter(
    private val localizationManager: ILocalizationManager,
    private val isInfo: Boolean,
    private var itemList: List<ParcelTypeUiModel> = listOf(),
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val onItemClickSubject: Subject<ParcelTypeUiModel> =
        PublishSubject.create<ParcelTypeUiModel>().toSerialized()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (isInfo) {
            ParcelTypeInfoViewHolder.fromParent(parent, localizationManager)
        } else {
            ParcelTypeViewHolder.fromParent(parent, onItemClickSubject, localizationManager)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ParcelTypeViewHolder -> holder.bindItem(itemList[position])
            is ParcelTypeInfoViewHolder -> holder.bindItem(itemList[position])
        }
    }

    override fun getItemCount() = itemList.size

    fun updateItemList(newList: List<ParcelTypeUiModel>) {
        this.itemList = newList
        notifyDataSetChanged()
    }

    fun updateItem(item: ParcelTypeUiModel) {
        val indexOf = itemList.indexOfFirst { it.type == item.type }
        if (indexOf != -1) {
            itemList[indexOf].isChecked = item.isChecked
            if (!item.isChecked) notifyItemChanged(indexOf)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is BaseViewHolder<*, *> -> holder.recycle()
        }
    }

    fun getOnItemClickObservable(): Observable<ParcelTypeUiModel> = onItemClickSubject
}