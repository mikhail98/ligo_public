package com.ligo.feature.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.R
import com.ligo.common.ligo.setAvatar
import com.ligo.common.ligo.setRating
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.alert.showDialog
import com.ligo.common.withBindingSafety
import com.ligo.core.dpToPx
import com.ligo.core.loadImageWithGlide
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.ConfigStringKey.FINISH_ACTIVE_TRIP_TO_DELETE_ACCOUNT
import com.ligo.data.model.User
import com.ligo.feature.profile.databinding.FragmentProfileBinding
import com.ligo.navigator.api.Target
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class ProfileFragment : BaseFragment<ProfileFragmentViewModel>() {

    companion object {
        const val TAG = "ProfileFragment"

        fun newInstance(): Fragment {
            return ProfileFragment()
        }
    }

    override val koinModule: Module = ProfileModule
    override val viewModel by inject<ProfileFragmentViewModel>()

    private var binding: FragmentProfileBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoadingState)

        viewModel.getDeleteAccountRequestResultObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleDeleteAccountRequest)

        viewModel.getUserObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleUser)

        viewModel.getAvatarLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleAvatarLoading)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    private fun handleDeleteAccountRequest(result: DeleteAccountRequestResult) {
        when (result) {
            DeleteAccountRequestResult.NOT_ALLOWED_DRIVER_HAS_ACTIVE_TRIPS -> {
                showSnackByStringKey(FINISH_ACTIVE_TRIP_TO_DELETE_ACCOUNT)
            }

            DeleteAccountRequestResult.ALLOWED -> {
                val prefix =
                    localizationManager.getLocalized(ConfigStringKey.ALERT_CONFIRMATION_ACTION_PREFIX)
                val confirmationMessage =
                    localizationManager.getLocalized(ConfigStringKey.DELETE_ACCOUNT)
                val title =
                    localizationManager.getLocalized(ConfigStringKey.ALERT_CONFIRMATION_TITLE)
                val okBtnText = localizationManager.getLocalized(ConfigStringKey.CONFIRM)
                val cancelBtnText = localizationManager.getLocalized(ConfigStringKey.CANCEL)
                val message = "$prefix $confirmationMessage"

                context?.showDialog(title, message, okBtnText, cancelBtnText) {
                    viewModel.deleteAccount()
                }
            }
        }
    }

    private fun handleUser(user: User) {
        withBindingSafety(binding) {
            tvUser.text = user.name
            tvUserEmail.text = user.email
            initAvatarLayout(user.avatarPhoto)
            setRating(user, rbRating, tvUserReviews, localizationManager)
        }
    }

    private fun handleAvatarLoading(data: Pair<Boolean, String?>) {
        withBindingSafety(binding) {
            if (data.first) {
                cvUpdatePhotoProgress.setVisibilityWithAlpha(true)
                cvUpdateAvatar.setVisibilityWithAlpha(false)
            } else {
                cvUpdatePhotoProgress.setVisibilityWithAlpha(false)
                cvUpdateAvatar.setVisibilityWithAlpha(true)
                setAvatar(ivAvatar, data.second)
            }
        }
    }

    private fun handleUnreadChatCount(unreadChatCount: Int) {
        withBindingSafety(binding) {
            tvUnreadChatCount.isVisible = unreadChatCount > 0
            tvUnreadChatCount.text = unreadChatCount.toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        viewModel.fetchUser()
        withBindingSafety(binding) {
            clTestPush.isVisible = BuildConfig.DEBUG
            clParseLocalization.isVisible = BuildConfig.DEBUG
            tvLogout.setLocalizedTextByKey(ConfigStringKey.LOGOUT)
            tvTitle.setLocalizedTextByKey(ConfigStringKey.PROFILE)
            tvAboutApp.setLocalizedTextByKey(ConfigStringKey.ABOUT_APP)
            tvContactUs.setLocalizedTextByKey(ConfigStringKey.CONTACT_US)
            tvDeleteAccount.setLocalizedTextByKey(ConfigStringKey.DELETE_ACCOUNT)
            tvPrivacyPolicy.setLocalizedTextByKey(ConfigStringKey.PRIVACY_POLICY)
            tvMessages.setLocalizedTextByKey(ConfigStringKey.MESSAGES)
        }
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            clAboutApp.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.open(Target.AboutApp)
            }
            clLogout.setOnThrottleClickListener(startedCompositeDisposable) { viewModel.logout() }
            clDeleteAccount.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.requestDeleteAccount()
            }
            clPrivacyPolicy.setOnThrottleClickListener(startedCompositeDisposable) {
                val url = localizationManager.getLocalized(ConfigStringKey.PRIVACY_POLICY_URL)
                navigator.open(Target.BrowserApp(url))
            }
            clChats.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.open(Target.Chats)
            }

            clContactUs.setOnThrottleClickListener(startedCompositeDisposable) {
                val email = localizationManager.getLocalized(ConfigStringKey.CONTACT_US_EMAIL)
                navigator.open(Target.EmailApp(email))
            }

            clParseLocalization.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.parseLocalization()
            }
            clTestPush.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.sendTestPush()
            }
            cvUpdateAvatar.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.takeAvatar()
            }
            ivAvatar.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.takeAvatar()
            }
        }

        viewModel.getUnreadChatCountObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleUnreadChatCount)

        viewModel.getChatAvailableObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable) {
                binding?.clChats?.isVisible = it
            }
    }

    private fun initAvatarLayout(photoUrl: String?) {
        withBindingSafety(binding) {
            if (photoUrl != null) {
                ivAvatar.setPadding(0, 0, 0, 0)
                ivAvatar.loadImageWithGlide(photoUrl)
            } else {
                cvUpdateAvatar.isVisible = false
                ivAvatar.loadImageWithGlide(R.drawable.ic_photo_camera)
                val paddingPixel = 8.dpToPx()
                ivAvatar.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel)
            }
        }
    }
}