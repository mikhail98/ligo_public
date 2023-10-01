package com.ligo.data

import android.content.Context
import androidx.room.Room
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ligo.data.api.RetrofitBuilder
import com.ligo.data.coordinator.parcels.ISenderParcelsCoordinator
import com.ligo.data.coordinator.parcels.SenderParcelsCoordinator
import com.ligo.data.coordinator.trips.DriverTripCoordinator
import com.ligo.data.coordinator.trips.IDriverTripCoordinator
import com.ligo.data.preferences.EncryptedConfig
import com.ligo.data.preferences.app.AppPreferences
import com.ligo.data.preferences.app.IAppPreferences
import com.ligo.data.repo.auth.AuthRepo
import com.ligo.data.repo.auth.IAuthRepo
import com.ligo.data.repo.chats.ChatsRepo
import com.ligo.data.repo.chats.IChatsRepo
import com.ligo.data.repo.googleapis.GoogleApisRepo
import com.ligo.data.repo.googleapis.IGoogleApisRepo
import com.ligo.data.repo.parcel.IParcelRepo
import com.ligo.data.repo.parcel.ParcelRepo
import com.ligo.data.repo.trip.ITripRepo
import com.ligo.data.repo.trip.TripRepo
import com.ligo.data.repo.user.IUserRepo
import com.ligo.data.repo.user.UserRepo
import com.ligo.data.room.RouteDatabase
import com.ligo.data.socket.ISocketService
import com.ligo.data.socket.SocketService
import org.koin.dsl.module

val DataModule = module {
    single { RetrofitBuilder() }
    single<ISocketService> { SocketService(get()) }

    single<IDriverTripCoordinator> { DriverTripCoordinator(get(), get(), get(), get(), get()) }
    single<ISenderParcelsCoordinator> { SenderParcelsCoordinator(get(), get(), get(), get()) }

    single { get<RetrofitBuilder>().getAuthApi() }
    single { get<RetrofitBuilder>().getTripApi() }
    single { get<RetrofitBuilder>().getUserApi() }
    single { get<RetrofitBuilder>().getParcelApi() }
    single { get<RetrofitBuilder>().getGoogleMapsApi() }
    single { get<RetrofitBuilder>().getChatsApi() }

    single<IAuthRepo> { AuthRepo(get(), get()) }
    single<ITripRepo> { TripRepo(get(), get()) }
    single<IUserRepo> { UserRepo(get(), get()) }
    single<IChatsRepo> { ChatsRepo(get(), get()) }
    single<IParcelRepo> { ParcelRepo(get(), get()) }
    single<IGoogleApisRepo> { GoogleApisRepo(get(), get(), get()) }

    single<IAppPreferences> { AppPreferences(get()) }

    single {
        MasterKey.Builder(get())
            .setKeyGenParameterSpec(EncryptedConfig.createAES256GCMKeyGenParameterSpec())
            .build()
    }

    single {
        EncryptedSharedPreferences.create(
            get<Context>(),
            EncryptedConfig.PREFERENCES_NAME,
            get(),
            EncryptedConfig.keyEncryptor,
            EncryptedConfig.valueEncryptor
        )
    }

    single {
        Room.databaseBuilder(get(), RouteDatabase::class.java, RouteDatabase.NAME).build()
    }

    single {
        get<RouteDatabase>().routeDao()
    }
}