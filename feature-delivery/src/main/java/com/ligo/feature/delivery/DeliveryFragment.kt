package com.ligo.feature.delivery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.ligo.common.BaseFragment
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.UserRole
import com.ligo.feature.delivery.databinding.FragmentDeliveryBinding
import com.ligo.subfeature.createparcel.CreateParcelFragment
import com.ligo.subfeature.createtrip.CreateTripFragment
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import android.R as AndroidR

class DeliveryFragment : BaseFragment<DeliveryFragmentViewModel>() {

    companion object {
        const val TAG = "parcel"

        fun newInstance(): Fragment {
            return DeliveryFragment()
        }
    }

    private var binding: FragmentDeliveryBinding? = null

    override val koinModule: Module = DeliveryModule
    override val viewModel by inject<DeliveryFragmentViewModel>()

    private var currentScreen = UserRole.SENDER

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDeliveryBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            switcher.init(
                localizationManager.getLocalized(ConfigStringKey.CREATE_TRIP),
                localizationManager.getLocalized(ConfigStringKey.SEND_PARCEL)
            )
            switcher.setChecked(currentScreen == UserRole.SENDER)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        showScreen(currentScreen)
        initViews()
    }

    private fun initViews() {
        withBindingSafety(binding) {
            switcher.setOnCheckChangeListener {
                showScreen(if (this) UserRole.SENDER else UserRole.DRIVER)
            }
        }
    }

    private fun showScreen(screen: UserRole) {
        currentScreen = screen
        val fragment = when (screen) {
            UserRole.DRIVER -> CreateTripFragment.newInstance()
            UserRole.SENDER -> CreateParcelFragment.newInstance()
        }
        childFragmentManager.commit {
            setCustomAnimations(AndroidR.animator.fade_in, AndroidR.animator.fade_out)
            replace(R.id.container, fragment)
        }
    }
}