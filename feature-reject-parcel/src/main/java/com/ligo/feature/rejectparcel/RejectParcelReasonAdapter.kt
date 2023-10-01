package com.ligo.feature.rejectparcel

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class RejectParcelReasonAdapter(
    val items: List<RejectReasonUi> = mutableListOf(),
    private val localizationManager: ILocalizationManager,
) : RecyclerView.Adapter<RejectParcelReasonViewHolder>() {

    private val onItemClickSubject: Subject<RejectReasonUi> =
        PublishSubject.create<RejectReasonUi>().toSerialized()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RejectParcelReasonViewHolder {
        return RejectParcelReasonViewHolder.fromParent(
            parent,
            onItemClickSubject,
            localizationManager
        )
    }

    override fun onBindViewHolder(holder: RejectParcelReasonViewHolder, position: Int) {
        holder.bindItem(items[position])
    }

    override fun onViewRecycled(holder: RejectParcelReasonViewHolder) {
        super.onViewRecycled(holder)
        holder.recycle()
    }

    fun updateReasonsList(item: RejectReasonUi) {
        val indexOfSelected = items.indexOfFirst { it.isChecked }

        when (val indexOf = items.indexOf(item)) {
            indexOfSelected -> {
                items[indexOf].isChecked = !item.isChecked
            }

            else -> {
                if (indexOfSelected != -1) {
                    items[indexOfSelected].isChecked = false
                    notifyItemChanged(indexOfSelected)
                }
                if (indexOf != -1) {
                    items[indexOf].isChecked = true
                    notifyItemChanged(indexOf)
                }
            }
        }
    }

    override fun getItemCount() = items.size

    fun getOnItemClickObservable(): Observable<RejectReasonUi> = onItemClickSubject
}