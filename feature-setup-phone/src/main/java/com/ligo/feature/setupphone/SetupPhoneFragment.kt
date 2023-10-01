package com.ligo.feature.setupphone

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.hideKeyboardFrom
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.showKeyboardFrom
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.ui.button.ActionButton
import com.ligo.common.ui.edittext.listeners.PhoneTextWatcher
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.setupphone.databinding.FragmentSetupPhoneBinding
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class SetupPhoneFragment : BaseFragment<SetupPhoneFragmentViewModel>() {

    companion object {
        const val TAG_BACKSTACK = "setup_phone"

        fun newInstance(): Fragment {
            return SetupPhoneFragment()
        }
    }

    override val koinModule: Module = SetupPhoneModule
    override val viewModel by inject<SetupPhoneFragmentViewModel>()

    private var binding: FragmentSetupPhoneBinding? = null

    private var isPhoneValid = false

    override fun onResume() {
        super.onResume()
        viewModel.getPhoneValidationObservable()
            .subscribeAndDisposeAt(resumedCompositeDisposable, ::handlePhoneValidation)
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(SetupPhoneFragment::class.java)
            }
            btnNext.setOnThrottleClickListener(startedCompositeDisposable) {
                viewModel.proceedToSelectRole(etPhone.text.toString())
            }
        }
    }

    private fun handlePhoneValidation(isValid: Boolean) {
        isPhoneValid = isValid

        withBindingSafety(binding) {
            if (isValid) {
                ivPhoneValid.setVisibilityWithAlpha(true)
            } else {
                ivPhoneValid.setVisibilityWithAlpha(false)
            }
            btnNext.state = if (isValid) {
                ActionButton.State.PRIMARY
            } else {
                ActionButton.State.INACTIVE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSetupPhoneBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            setPhoneListeners(etPhone, viewModel::validatePhone, viewModel::proceedToSelectRole)
            etPhone.hint =
                localizationManager.getLocalized(ConfigStringKey.TYPE_YOUR_PHONE_HINT)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.TYPE_YOUR_PHONE_DESCRIPTION)
            btnNext.setLocalizedTextByKey(ConfigStringKey.NEXT)
        }
        return binding?.root
    }

    private fun setPhoneListeners(
        editText: EditText,
        textChanged: (String) -> Unit,
        action: (String) -> Unit,
    ) {
        editText.requestFocus()
        showKeyboardFrom(editText)
        editText.setOnEditorActionListener { _, actionId, _ ->
            val isDone = actionId == EditorInfo.IME_ACTION_NEXT
            if (isDone) {
                action.invoke(editText.text.toString())
                hideKeyboardFrom(editText)
                return@setOnEditorActionListener true
            }

            return@setOnEditorActionListener false
        }
        editText.addTextChangedListener(PhoneTextWatcher(textChanged))
    }
}