package com.ligo.tools.api

class SearchPlaceRequest(
    val origin: Origin,
) {
    enum class Origin {
        SEND_PARCEL_FROM,
        SEND_PARCEL_TO,
        START_TRIP_FROM,
        START_TRIP_TO
    }
}