package com.ligo.common

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.annotation.CallSuper
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.snackbar.Snackbar
import com.ligo.data.model.ApiThrowable
import com.ligo.data.model.ConfigStringKey.API_ERROR_SOMETHING_WENT_WRONG
import com.ligo.google.api.IAnalytics.Events.ACTION_SCREEN_OPENED
import com.ligo.navigator.api.INavigator
import com.ligo.navigator.api.PushArgs
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module
import com.ligo.core.R as CoreR

abstract class BaseFragment<VM : BaseViewModel> : Fragment(), ViewContainer {

    abstract val viewModel: VM
    abstract val koinModule: Module

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            navigator.closeFragment(this@BaseFragment::class.java)
        }
    }

    override val localizationManager: ILocalizationManager by inject()

    protected open var registerBackpressureCallback: Boolean = true

    val startedCompositeDisposable = CompositeDisposable()
    val createdCompositeDisposable = CompositeDisposable()
    val resumedCompositeDisposable = CompositeDisposable()

    val navigator: INavigator
        get() = viewModel.navigator

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
            onChildFragmentStarted(f)
        }

        override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
            if (f is BaseFragment<*>) {
                f.startedCompositeDisposable.clear()
            }
        }
    }

    protected open fun onChildFragmentStarted(f: Fragment) {}

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        loadKoinModules(koinModule)
        super.onCreate(savedInstanceState)
        if (registerBackpressureCallback) {
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }
        childFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)

        viewModel.onCreate()
        viewModel.logEvent(ACTION_SCREEN_OPENED + "_" + javaClass.simpleName)

        viewModel.getOnApiErrorObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleError)

        viewModel.getSnackbarObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::showSnackByStringKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setOnClickListener(null)
    }

    override fun onPause() {
        super.onPause()
        resumedCompositeDisposable.clear()
    }

    override fun onStop() {
        super.onStop()
        startedCompositeDisposable.clear()
    }

    @CallSuper
    override fun onDestroy() {
        unloadKoinModules(koinModule)
        super.onDestroy()
        viewModel.onDestroy()
        createdCompositeDisposable.clear()
    }

    protected fun getDrawable(@DrawableRes res: Int) =
        ContextCompat.getDrawable(requireContext(), res)

    protected fun showSnackByStringKey(
        configStringKeyMessage: String,
        length: Int = Snackbar.LENGTH_SHORT,
        @ColorRes bgColor: Int = CoreR.color.gray_58,
        @ColorRes textColor: Int = CoreR.color.white,
    ) {
        view?.let {
            val message = getLocalizedStringByKey(configStringKeyMessage)
            Snackbar.make(it, message, length).setBackgroundTint(requireContext().getColor(bgColor))
                .setTextColor(requireContext().getColor(textColor)).show()
        }
    }

    protected open fun handleError(throwable: Throwable) {
        val errorMessageConfigKeyCode = when (throwable) {
            is ApiThrowable -> throwable.errorLocalizationKey
            else -> API_ERROR_SOMETHING_WENT_WRONG
        }
        showSnackByStringKey(errorMessageConfigKeyCode)
    }

    protected fun getPushArgs(): PushArgs {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            activity?.intent?.getParcelableExtra(INavigator.EXTRA_PUSH_ARGS, PushArgs::class.java)
        } else {
            activity?.intent?.getParcelableExtra(INavigator.EXTRA_PUSH_ARGS)
        } ?: PushArgs()
    }
}