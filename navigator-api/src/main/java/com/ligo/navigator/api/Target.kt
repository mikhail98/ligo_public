package com.ligo.navigator.api

import com.ligo.tools.api.SearchPlaceRequest

sealed class Target {
    object Splash : Target()

    object Auth : Target()

    object SetupPhone : Target()

    object SelectRole : Target()

    object Home : Target()

    class Camera(val cameraTask: CameraTask) : Target()

    class DriverTrip(val tripId: String) : Target()

    class SenderParcel(val parcelId: String) : Target()

    class ParcelQr(val data: String) : Target()

    class SearchForDriver(val parcelId: String) : Target()

    class ParcelInTrip(val parcelId: String) : Target()

    class SearchPlace(val origin: SearchPlaceRequest.Origin) : Target()

    class SearchPlaceOnMap(val origin: SearchPlaceRequest.Origin) : Target()

    object AboutApp : Target()

    class Onboarding(val onboardingType: OnboardingType) : Target()

    class RejectParcel(val parcelId: String) : Target()

    object Chats : Target()

    class Chat(val chatId: String) : Target()

    class PhoneCallApp(val phoneNumber: CharSequence) : Target()

    class MapApp(val latitude: Double, val longitude: Double, val label: String) : Target()

    class EmailApp(val mailTo: String) : Target()

    class BrowserApp(val url: String) : Target()
}