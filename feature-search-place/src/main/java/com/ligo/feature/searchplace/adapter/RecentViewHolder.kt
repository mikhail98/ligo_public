package com.ligo.feature.searchplace.adapter

import com.ligo.common.BaseViewHolder
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.searchplace.databinding.ListItemRecentSearchBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

class RecentViewHolder(
    binding: ListItemRecentSearchBinding,
    onItemClickSubject: Subject<SearchItem>,
    override val localizationManager: ILocalizationManager,
) : BaseViewHolder<SearchItem, ListItemRecentSearchBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {

    override fun initView(item: SearchItem, binding: ListItemRecentSearchBinding) {
        binding.tvRecent.text = localizationManager.getLocalized(ConfigStringKey.RECENT_SEARCH)
    }
}