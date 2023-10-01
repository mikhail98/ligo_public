package com.ligo.feature.searchplace.adapter

import com.ligo.data.model.Location

class SearchItem(
    val type: Type,
    val location: Location?,
    val iconUrl: String? = null,
) {

    enum class Type {
        SEARCH_ON_MAP, RECENT, RESULT
    }
}