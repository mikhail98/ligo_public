package com.ligo.feature.searchplace.adapter

import com.ligo.common.BaseViewHolder
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.searchplace.databinding.ListItemSearchOnMapBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

class SearchOnMapViewHolder(
    binding: ListItemSearchOnMapBinding,
    onItemClickSubject: Subject<SearchItem>,
    localizationManager: ILocalizationManager,
) : BaseViewHolder<SearchItem, ListItemSearchOnMapBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {
    override fun initView(item: SearchItem, binding: ListItemSearchOnMapBinding) {
        binding.tvSearchPlace.text =
            localizationManager.getLocalized(ConfigStringKey.SEARCH_PLACE_ON_MAP)
    }
}