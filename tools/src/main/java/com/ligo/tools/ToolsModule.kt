package com.ligo.tools

import com.ligo.chats.coordinator.IChatsCoordinator
import com.ligo.data.coordinator.parcels.ISenderParcelsCoordinator
import com.ligo.data.coordinator.trips.IDriverTripCoordinator
import com.ligo.data.socket.ISocketService
import com.ligo.google.api.IRemoteConfig
import com.ligo.tools.api.ICompressor
import com.ligo.tools.api.IInitializer
import com.ligo.tools.api.ILocalizationManager
import com.ligo.tools.api.ILocationTracker
import com.ligo.tools.api.INotificationManager
import com.ligo.tools.api.IPhotoManager
import com.ligo.tools.api.IPlaceSearchManager
import com.ligo.tools.api.IQrManager
import org.koin.dsl.module

val ToolsModule = module {
    single<INotificationManager> {
        NotificationManager(
            get(),
            get(),
            get(),
            get(),
            get()
        )
    }
    single<IPlaceSearchManager> { PlaceSearchManager() }
    single<ILocationTracker> { LocationTracker(get(), get(), get(), get()) }
    single<IPhotoManager> { PhotoManager(get(), get()) }
    single<IQrManager> { QrManager(get(), get()) }
    single<ICompressor> { Compressor(get()) }
    single<ILocalizationManager> { LocalizationManager(get(), get()) }

    single<IInitializer> {
        Initializer(
            setOf(
                get<IRemoteConfig>(),
                get<ISocketService>(),
                get<com.ligo.chats.coordinator.IChatsCoordinator>(),
                get<IDriverTripCoordinator>(),
                get<ISenderParcelsCoordinator>()
            )
        )
    }
}