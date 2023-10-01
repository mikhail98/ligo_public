package com.ligo.tools.api

data class ScanQR(
    val data: String,
    val origin: Origin,
) {
    enum class Origin {
        PICK, DELIVERY
    }
}
