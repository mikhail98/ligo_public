package com.ligo.tools.api

import com.ligo.data.model.Location

class SearchPlaceResult(
    val location: Location?,
    val origin: SearchPlaceRequest.Origin,
)