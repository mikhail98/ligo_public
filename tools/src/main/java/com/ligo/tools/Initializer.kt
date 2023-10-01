package com.ligo.tools

import com.ligo.core.Initializable
import com.ligo.tools.api.IInitializer

internal class Initializer(
    private val initializableSet: Set<Initializable>,
) : IInitializer {

    override fun on(on: Initializable.On) {
        initializableSet.filter { it.initOnList.contains(on) }.forEach { it.init() }
        initializableSet.filter { it.connectOnList.contains(on) }.forEach { it.connect() }
        initializableSet.filter { it.clearOnList.contains(on) }.forEach { it.clear() }
    }
}