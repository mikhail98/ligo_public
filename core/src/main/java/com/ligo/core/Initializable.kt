package com.ligo.core

interface Initializable {

    val initOnList: List<On>

    val connectOnList: List<On>

    val clearOnList: List<On>

    fun init()

    fun connect() {
        // do nothing
    }

    fun clear() {
        // do nothing
    }

    enum class On {
        CREATE, LOGIN, LOGOUT, NEVER
    }
}