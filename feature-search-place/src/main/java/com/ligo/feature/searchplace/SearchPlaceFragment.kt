package com.ligo.feature.searchplace

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.getDebounceTextChangedObservable
import com.ligo.common.hideKeyboardFrom
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.searchplace.adapter.SearchItem
import com.ligo.feature.searchplace.adapter.SearchPlaceAdapter
import com.ligo.feature.searchplace.databinding.FragmentSearchPlaceBinding
import com.ligo.google.api.ILocationManager
import com.ligo.navigator.api.Target
import com.ligo.tools.api.SearchPlaceRequest
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class SearchPlaceFragment : BaseFragment<SearchPlaceFragmentViewModel>() {
    companion object {
        const val TAG_BACKSTACK = "search_place"

        private const val ARGS_ORIGIN = "args_origin"

        fun newInstance(origin: SearchPlaceRequest.Origin): Fragment {
            return SearchPlaceFragment().apply {
                arguments = bundleOf(ARGS_ORIGIN to origin)
            }
        }
    }

    override val koinModule: Module = SearchPlaceModule
    override val viewModel by inject<SearchPlaceFragmentViewModel>()

    private var binding: FragmentSearchPlaceBinding? = null

    private val locationManager by inject<ILocationManager>()
    private val searchAdapter by lazy { SearchPlaceAdapter(localizationManager) }

    private val origin by lazy { arguments?.getSerializable(ARGS_ORIGIN) as SearchPlaceRequest.Origin }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoading)

        viewModel.getOnSearchItemsObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, searchAdapter::setItems)

        searchAdapter.getOnItemClickedObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::onItemSelected)

        locationManager.fetchLastKnownLocation(searchAdapter::setUserLocation)
    }

    private fun handleLoading(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    private fun onItemSelected(item: SearchItem) {
        when (item.type) {
            SearchItem.Type.SEARCH_ON_MAP -> {
                hideKeyboardFrom(binding?.etSearch)
                navigator.open(Target.SearchPlaceOnMap(origin))
            }

            SearchItem.Type.RESULT -> viewModel.onLocationSelected(item.location, origin)
            else -> Unit
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSearchPlaceBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            etSearch.hint = localizationManager.getLocalized(ConfigStringKey.SEARCH)
        }
        return binding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        withBindingSafety(binding) {
            btnBack.clipToOutline = true
            rvLocationSearch.adapter = searchAdapter
        }
        viewModel.search("")
    }

    override fun onStart() {
        super.onStart()

        withBindingSafety(binding) {
            getDebounceTextChangedObservable(etSearch, 500L)
                .subscribeAndDisposeAt(startedCompositeDisposable, viewModel::search)

            root.setOnThrottleClickListener(startedCompositeDisposable) { }
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(SearchPlaceFragment::class.java)
            }
        }
    }
}