package com.ligo.feature.searchplace.adapter

import android.location.Location
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ligo.common.BaseViewHolder
import com.ligo.feature.searchplace.databinding.ListItemLocationSearchBinding
import com.ligo.feature.searchplace.databinding.ListItemRecentSearchBinding
import com.ligo.feature.searchplace.databinding.ListItemSearchOnMapBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

class SearchPlaceAdapter(private val localizationManager: ILocalizationManager) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val onItemClickSubject: Subject<SearchItem> =
        PublishSubject.create<SearchItem>().toSerialized()

    private val items = mutableListOf<SearchItem>()
    private var userLocation: Location? = null

    fun setItems(newList: List<SearchItem>) {
        items.clear()
        items.addAll(newList)
        notifyDataSetChanged()
    }

    fun setUserLocation(location: Location?) {
        this.userLocation = location
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position].type) {
            SearchItem.Type.SEARCH_ON_MAP -> ViewTypes.SEARCH_ON_MAP
            SearchItem.Type.RECENT -> ViewTypes.RECENT
            SearchItem.Type.RESULT -> ViewTypes.RESULT
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ViewTypes.SEARCH_ON_MAP -> SearchOnMapViewHolder(
                ListItemSearchOnMapBinding.inflate(inflater, parent, false),
                onItemClickSubject,
                localizationManager
            )

            ViewTypes.RECENT -> RecentViewHolder(
                ListItemRecentSearchBinding.inflate(inflater, parent, false),
                onItemClickSubject,
                localizationManager
            )

            ViewTypes.RESULT -> LocationSearchViewHolder(
                ListItemLocationSearchBinding.inflate(inflater, parent, false),
                userLocation,
                onItemClickSubject,
                localizationManager
            )

            else -> error("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = items[position]
        when (holder) {
            is RecentViewHolder -> holder.bindItem(item)
            is SearchOnMapViewHolder -> holder.bindItem(item)
            is LocationSearchViewHolder -> holder.bindItem(item)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        when (holder) {
            is BaseViewHolder<*, *> -> holder.recycle()
        }
    }

    private object ViewTypes {
        const val SEARCH_ON_MAP = 0
        const val RECENT = 1
        const val RESULT = 2
    }

    fun getOnItemClickedObservable(): Observable<SearchItem> = onItemClickSubject
}