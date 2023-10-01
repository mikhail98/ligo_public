package com.ligo.tools.api

import com.ligo.core.Initializable

interface IInitializer {

    fun on(on: Initializable.On)
}