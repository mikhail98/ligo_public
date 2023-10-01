package com.ligo.demoapp

import com.ligo.chats.coordinator.ChatsCoordinatorModule
import com.ligo.data.DataModule
import com.ligo.google.GoogleModule
import com.ligo.navigator.NavigatorModule
import com.ligo.toggler.TogglerModule
import com.ligo.tools.ToolsModule
import org.koin.core.module.Module

object KoinModules {

    fun getModules(): List<Module> {
        val modulesList = mutableListOf<Module>()

        modulesList.add(DataModule)

        modulesList.add(GoogleModule)

        modulesList.add(ToolsModule)

        modulesList.add(TogglerModule)

        modulesList.add(NavigatorModule)

        modulesList.add(ChatsCoordinatorModule)

        return modulesList
    }
}