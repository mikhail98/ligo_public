package com.ligo.common

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ligo.navigator.api.INavigator
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.disposables.CompositeDisposable
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.core.module.Module

abstract class BaseBottomSheetDialogFragment<VM : BaseViewModel> :
    BottomSheetDialogFragment(), ViewContainer {

    abstract val koinModule: Module
    abstract val viewModel: VM

    override val localizationManager: ILocalizationManager by inject()

    protected open val cancellable: Boolean = true

    val createdCompositeDisposable = CompositeDisposable()
    val startedCompositeDisposable = CompositeDisposable()
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

    override fun onCreate(savedInstanceState: Bundle?) {
        loadKoinModules(koinModule)
        super.onCreate(savedInstanceState)
        childFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)

        viewModel.onCreate()
        isCancelable = cancellable
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog
        dialog.setOnShowListener { _ ->
            val bs = dialog.findViewById<View>(R.id.design_bottom_sheet) as FrameLayout
            BottomSheetBehavior.from(bs).state = BottomSheetBehavior.STATE_EXPANDED
        }
        return dialog
    }

    override fun onStop() {
        super.onStop()
        startedCompositeDisposable.clear()
    }

    override fun onPause() {
        super.onPause()
        resumedCompositeDisposable.clear()
    }

    override fun onDestroy() {
        unloadKoinModules(koinModule)
        super.onDestroy()
        createdCompositeDisposable.clear()
    }
}