package com.ligo.navigator

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.ligo.core.PermissionChecker
import com.ligo.feature.aboutapp.AboutAppFragment
import com.ligo.feature.auth.AuthFragment
import com.ligo.feature.camera.CameraActivity
import com.ligo.feature.chat.ChatFragment
import com.ligo.feature.chats.ChatsFragment
import com.ligo.feature.delivery.DeliveryFragment
import com.ligo.feature.drivertrip.DriverTripFragment
import com.ligo.feature.history.HistoryFragment
import com.ligo.feature.home.HomeFragment
import com.ligo.feature.main.MainActivity
import com.ligo.feature.main.R
import com.ligo.feature.onboarding.OnboardingFragment
import com.ligo.feature.parcelintrip.ParcelInTripFragment
import com.ligo.feature.parcelqr.ParcelQrFragment
import com.ligo.feature.profile.ProfileFragment
import com.ligo.feature.rejectparcel.RejectParcelFragment
import com.ligo.feature.searchfordriver.SearchForDriverFragment
import com.ligo.feature.searchplace.SearchPlaceFragment
import com.ligo.feature.searchplaceonmap.SearchPlaceOnMapFragment
import com.ligo.feature.selectrole.SelectRoleFragment
import com.ligo.feature.senderparcel.SenderParcelFragment
import com.ligo.feature.setupphone.SetupPhoneFragment
import com.ligo.feature.splash.SplashFragment
import com.ligo.navigator.api.CameraTask
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.OnboardingType
import com.ligo.navigator.api.Target
import com.ligo.tools.api.SearchPlaceRequest
import java.util.Stack

internal class Navigator : INavigator {

    override val topLevelFeature: Target
        get() = _featureStack.last()

    private var _featureStack: Stack<Target> = Stack()

    private var activity: AppCompatActivity? = null

    override fun setupActivity(activity: AppCompatActivity) {
        this.activity = activity
    }

    override fun open(target: Target) {
        if (target is Target.Splash) {
            _featureStack.clear()
        }
        _featureStack.add(target)
        when (target) {
            is Target.Auth -> openAuth()
            is Target.Home -> openHome()
            is Target.Splash -> openSplash()
            is Target.AboutApp -> openAboutApp()
            is Target.SetupPhone -> openSetupPhone()
            is Target.SelectRole -> openSelectRole()
            is Target.Chats -> openChats()
            is Target.Chat -> openChat(target.chatId)
            is Target.ParcelQr -> openParcelQr(target.data)
            is Target.EmailApp -> openEmailApp(target.mailTo)
            is Target.Camera -> openCamera(target.cameraTask)
            is Target.BrowserApp -> openBrowserApp(target.url)
            is Target.DriverTrip -> openDriverTrip(target.tripId)
            is Target.SearchPlace -> openSearchPlace(target.origin)
            is Target.ParcelInTrip -> openParcelInTrip(target.parcelId)
            is Target.RejectParcel -> openRejectParcel(target.parcelId)
            is Target.SenderParcel -> openSenderParcel(target.parcelId)
            is Target.PhoneCallApp -> openPhoneCall(target.phoneNumber)
            is Target.Onboarding -> openOnboarding(target.onboardingType)
            is Target.SearchForDriver -> openSearchForDriver(target.parcelId)
            is Target.SearchPlaceOnMap -> openSearchPlaceOnMap(target.origin)
            is Target.MapApp -> openMapApp(target.latitude, target.longitude, target.label)
        }
    }

    private fun openSplash() {
        SplashFragment.newInstance().open(FragmentAction.REPLACE)
    }

    private fun openAuth() {
        AuthFragment.newInstance().open(FragmentAction.REPLACE)
    }

    private fun openHome() {
        HomeFragment.newInstance().open(FragmentAction.REPLACE)
    }

    private fun openSetupPhone() {
        SetupPhoneFragment.newInstance()
            .open(FragmentAction.REPLACE, SetupPhoneFragment.TAG_BACKSTACK)
    }

    private fun openSelectRole() {
        SelectRoleFragment.newInstance().open(FragmentAction.ADD, SelectRoleFragment.TAG_BACKSTACK)
    }

    private fun openParcelInTrip(parcelId: String) {
        ParcelInTripFragment.newInstance(parcelId)
            .open(FragmentAction.ADD, ParcelInTripFragment.TAG_BACKSTACK)
    }

    private fun openRejectParcel(parcelId: String) {
        RejectParcelFragment.newInstance(parcelId)
            .open(FragmentAction.ADD, RejectParcelFragment.TAG_BACKSTACK)
    }

    private fun openCamera(cameraTask: CameraTask) {
        activity?.startActivity(CameraActivity.getIntent(activity ?: return, cameraTask))
    }

    private fun openSearchForDriver(parcelId: String) {
        SearchForDriverFragment.newInstance(parcelId)
            .open(FragmentAction.ADD, SearchForDriverFragment.TAG_BACKSTACK)
    }

    private fun openDriverTrip(tripId: String) {
        val permissionsResult = PermissionChecker.isLocationPermissionEnabled(activity ?: return)
        if (permissionsResult.first) {
            DriverTripFragment.newInstance(tripId)
                .open(FragmentAction.ADD, DriverTripFragment.TAG_BACKSTACK)
        }
    }

    private fun openSenderParcel(parcelId: String) {
        SenderParcelFragment.newInstance(parcelId)
            .open(FragmentAction.ADD, SenderParcelFragment.TAG_BACKSTACK)
    }

    private fun openOnboarding(onboardingType: OnboardingType) {
        OnboardingFragment.newInstance(onboardingType)
            .open(FragmentAction.ADD, OnboardingFragment.TAG_BACKSTACK)
    }

    private fun openParcelQr(data: String) {
        ParcelQrFragment.newInstance(data)
            .open(FragmentAction.ADD, ParcelQrFragment.TAG_BACKSTACK)
    }

    private fun openSearchPlace(origin: SearchPlaceRequest.Origin) {
        SearchPlaceFragment.newInstance(origin)
            .open(FragmentAction.ADD, SearchPlaceFragment.TAG_BACKSTACK)
    }

    private fun openSearchPlaceOnMap(origin: SearchPlaceRequest.Origin) {
        SearchPlaceOnMapFragment.newInstance(origin)
            .open(FragmentAction.ADD, SearchPlaceOnMapFragment.TAG_BACKSTACK)
    }

    private fun openAboutApp() {
        AboutAppFragment.newInstance()
            .open(FragmentAction.ADD, AboutAppFragment.TAG_BACKSTACK)
    }

    private fun openChats() {
        ChatsFragment.newInstance()
            .open(FragmentAction.ADD, ChatsFragment.TAG_BACKSTACK)
    }

    private fun openChat(parcelId: String) {
        ChatFragment.newInstance(parcelId)
            .open(FragmentAction.ADD, ChatFragment.TAG_BACKSTACK)
    }

    private fun openPhoneCall(phoneNumber: CharSequence) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
        activity?.startActivity(intent)
    }

    private fun openMapApp(latitude: Double, longitude: Double, label: String) {
        val uri = "http://maps.google.com/maps?q=loc:$latitude,$longitude($label)"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        activity?.startActivity(intent)
    }

    private fun openEmailApp(mailTo: String) {
        Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$mailTo")
            activity?.startActivity(this)
        }
    }

    private fun openBrowserApp(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        activity?.startActivity(browserIntent)
    }

    override fun <T : Fragment> closeFragment(fragment: Class<out T>) {
        _featureStack.pop()
        when (fragment) {
            AuthFragment::class.java, HomeFragment::class.java,
            DeliveryFragment::class.java, HistoryFragment::class.java, ProfileFragment::class.java,
            -> activity?.finish()

            ChatFragment::class.java -> closeChat()
            ChatsFragment::class.java -> closeChats()
            ParcelQrFragment::class.java -> closeParcelQr()
            AboutAppFragment::class.java -> closeAboutApp()
            OnboardingFragment::class.java -> closeOnboarding()
            SetupPhoneFragment::class.java -> closeSetupPhone()
            SelectRoleFragment::class.java -> closeSelectRole()
            DriverTripFragment::class.java -> closeDriverTrip()
            SearchPlaceFragment::class.java -> closeSearchPlace()
            SenderParcelFragment::class.java -> closeSenderParcel()
            RejectParcelFragment::class.java -> closeRejectParcel()
            ParcelInTripFragment::class.java -> closeParcelInTrip()
            SearchForDriverFragment::class.java -> closeSearchForDriver()
            SearchPlaceOnMapFragment::class.java -> closeSearchPlaceOnMap()
        }
    }

    override fun <T : Target> close(target: Class<out T>) {
        _featureStack.pop()
        when (target) {
            Target.Auth::class.java, Target.Home::class.java -> activity?.finish()
            Target.Chat::class.java -> closeChat()
            Target.Chats::class.java -> closeChats()
            Target.ParcelQr::class.java -> closeParcelQr()
            Target.AboutApp::class.java -> closeAboutApp()
            Target.Onboarding::class.java -> closeOnboarding()
            Target.SetupPhone::class.java -> closeSetupPhone()
            Target.SelectRole::class.java -> closeSelectRole()
            Target.DriverTrip::class.java -> closeDriverTrip()
            Target.SearchPlace::class.java -> closeSearchPlace()
            Target.SenderParcel::class.java -> closeSenderParcel()
            Target.RejectParcel::class.java -> closeRejectParcel()
            Target.ParcelInTrip::class.java -> closeParcelInTrip()
            Target.SearchForDriver::class.java -> closeSearchForDriver()
            Target.SearchPlaceOnMap::class.java -> closeSearchPlaceOnMap()
        }
    }

    private fun closeSenderParcel() {
        activity?.supportFragmentManager?.popBackStack(
            SenderParcelFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeDriverTrip() {
        activity?.supportFragmentManager?.popBackStack(
            DriverTripFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeSearchForDriver() {
        activity?.supportFragmentManager?.popBackStack(
            SearchForDriverFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeParcelInTrip() {
        activity?.supportFragmentManager?.popBackStack(
            ParcelInTripFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeSelectRole() {
        activity?.supportFragmentManager?.popBackStack(
            SelectRoleFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeSetupPhone() {
        activity?.supportFragmentManager?.popBackStack(
            SetupPhoneFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeAboutApp() {
        activity?.supportFragmentManager?.popBackStack(
            AboutAppFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeSearchPlace() {
        activity?.supportFragmentManager?.popBackStack(
            SearchPlaceFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeSearchPlaceOnMap() {
        activity?.supportFragmentManager?.popBackStack(
            SearchPlaceOnMapFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeOnboarding() {
        activity?.supportFragmentManager?.popBackStack(
            OnboardingFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeRejectParcel() {
        activity?.supportFragmentManager?.popBackStack(
            RejectParcelFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeParcelQr() {
        activity?.supportFragmentManager?.popBackStack(
            ParcelQrFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeChat() {
        activity?.supportFragmentManager?.popBackStack(
            ChatFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun closeChats() {
        activity?.supportFragmentManager?.popBackStack(
            ChatsFragment.TAG_BACKSTACK,
            FragmentManager.POP_BACK_STACK_INCLUSIVE
        )
    }

    private fun Fragment.open(
        action: FragmentAction = FragmentAction.REPLACE,
        tag: String? = null,
        fadeIn: Boolean = true,
        fadeOut: Boolean = true,
    ) {
        val fragmentManger = this@Navigator.activity?.supportFragmentManager
        fragmentManger?.commit {
            when {
                !fadeIn && !fadeOut -> Unit
                fadeIn && !fadeOut -> setCustomAnimations(
                    android.R.animator.fade_in,
                    android.R.animator.fade_out,
                )

                fadeIn && fadeOut -> setCustomAnimations(
                    android.R.animator.fade_in,
                    android.R.animator.fade_out,
                    android.R.animator.fade_in,
                    android.R.animator.fade_out,
                )
            }
            when (action) {
                FragmentAction.ADD -> add(R.id.container, this@open)
                FragmentAction.REPLACE -> replace(R.id.container, this@open)
            }

            tag?.apply { addToBackStack(this) }
        }
    }

    override fun provideMainAppIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java)
    }

    enum class FragmentAction {
        ADD, REPLACE
    }
}