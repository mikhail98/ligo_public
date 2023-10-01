package com.ligo.feature.chats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.model.ChatListUiModel
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey.MESSAGES
import com.ligo.data.model.ConfigStringKey.NO_CHATS_DESCRIPTION
import com.ligo.data.model.ConfigStringKey.NO_CHATS_TITLE
import com.ligo.feature.chats.databinding.FragmentChatsBinding
import com.ligo.feature.chats.recycler.ChatsAdapter
import com.ligo.navigator.api.Target
import org.koin.android.ext.android.inject
import org.koin.core.module.Module

class ChatsFragment : BaseFragment<ChatsFragmentViewModel>() {

    companion object {

        const val TAG_BACKSTACK = "MessagesListFragment"

        fun newInstance(): Fragment {
            return ChatsFragment()
        }
    }

    override val koinModule: Module = ChatsModule
    override val viewModel by inject<ChatsFragmentViewModel>()

    private var binding: FragmentChatsBinding? = null

    private val chatsAdapter by lazy { ChatsAdapter(localizationManager) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoadingState)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChatsBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(MESSAGES)
            btnBack.clipToOutline = true
            rvMessages.setHasFixedSize(true)
            rvMessages.adapter = chatsAdapter

            tvNoChatsTitle.setLocalizedTextByKey(NO_CHATS_TITLE)
            tvNoChatsDescription.setLocalizedTextByKey(NO_CHATS_DESCRIPTION)
        }
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        viewModel.getOnChatItemsObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleChats)

        chatsAdapter.getOnItemClickObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleChatClicked)

        withBindingSafety(binding) {
            root.setOnThrottleClickListener(createdCompositeDisposable) {}
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(ChatsFragment::class.java)
            }
        }
    }

    private fun handleChats(chatItems: List<ChatListUiModel>) {
        withBindingSafety(binding) {
            val isChatsEmpty = chatItems.isEmpty()
            clNoChats.isVisible = isChatsEmpty
            chatsAdapter.setItems(chatItems)
        }
    }

    private fun handleChatClicked(chatItem: ChatListUiModel) {
        navigator.open(Target.Chat(chatItem.chatId))
    }
}
