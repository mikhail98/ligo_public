package com.ligo.feature.chat

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils.loadAnimation
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.hideKeyboardFrom
import com.ligo.common.ligo.setAvatar
import com.ligo.common.model.BaseChatMessageUiModel
import com.ligo.common.model.ChatUiModel
import com.ligo.common.model.InChatMessageUiModel
import com.ligo.common.model.OutChatMessageUiModel
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.core.dpToPx
import com.ligo.data.MessageReadStatus
import com.ligo.data.model.ConfigStringKey
import com.ligo.data.model.ConfigStringKey.CALL_DRIVER
import com.ligo.data.model.ConfigStringKey.CALL_SENDER
import com.ligo.data.model.ConfigStringKey.COPY
import com.ligo.data.model.ConfigStringKey.GO_TO_PARCEL
import com.ligo.data.model.ConfigStringKey.NO_MESSAGE_DESCRIPTION
import com.ligo.data.model.ConfigStringKey.NO_MESSAGE_TITLE
import com.ligo.data.model.ConfigStringKey.REPORT_ISSUE
import com.ligo.data.model.ConfigStringKey.SHOW_DRIVER_ON_A_MAP
import com.ligo.data.model.ConfigStringKey.SHOW_SENDER_ON_A_MAP
import com.ligo.feature.chat.ChatPopupWindowHelper.ShowFrom.CURRENT_USER_MESSAGE
import com.ligo.feature.chat.ChatPopupWindowHelper.ShowFrom.OPTIONS_BTN
import com.ligo.feature.chat.ChatPopupWindowHelper.ShowFrom.OTHER_USER_MESSAGE
import com.ligo.feature.chat.databinding.FragmentChatBinding
import com.ligo.feature.chat.recycler.ChatAdapter
import com.ligo.subfeatureselectphotosource.SelectPhotoSourceBottomSheetDialogFragment
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

class ChatFragment : BaseFragment<ChatFragmentViewModel>() {

    companion object {

        const val TAG_BACKSTACK = "ChatFragment"

        private const val ARGS_KEY_CHAT_ID = "ARGS_KEY_CHAT_ID"

        fun newInstance(chatId: String): Fragment {
            return ChatFragment().apply {
                arguments = bundleOf(ARGS_KEY_CHAT_ID to chatId)
            }
        }
    }

    override val koinModule: Module = ChatModule
    override val viewModel by inject<ChatFragmentViewModel>()

    private val adapter by lazy { ChatAdapter(localizationManager) }
    private var binding: FragmentChatBinding? = null

    private val chatId by lazy { arguments?.getString(ARGS_KEY_CHAT_ID).orEmpty() }

    private val popupOptionsHelper by lazy {
        val hideAnimation = loadAnimation(requireContext(), R.anim.animation_menu_options_close)
        ChatPopupWindowHelper(requireContext(), R.layout.menu_chat_options, hideAnimation)
    }

    private val popupMessageOptionsHelper: ChatPopupWindowHelper
        get() {
            val hideAnimation = loadAnimation(requireContext(), R.anim.animation_menu_options_close)
            return ChatPopupWindowHelper(
                requireContext(),
                R.layout.menu_message_options,
                hideAnimation
            )
        }

    override fun onChildFragmentStarted(f: Fragment) {
        super.onChildFragmentStarted(f)
        when (f) {
            is SelectPhotoSourceBottomSheetDialogFragment -> {
                f.getOnSelectedPhotoSourceObservable()
                    .subscribeAndDisposeAt(f.startedCompositeDisposable, viewModel::sourceSelected)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentChatBinding.inflate(layoutInflater, container, false)
        withBindingSafety(binding) {
            btnSend.clipToOutline = true
            btnBack.clipToOutline = true
            btnOptions.clipToOutline = true
            btnAttachment.clipToOutline = true

            etMessage.hint = localizationManager.getLocalized(ConfigStringKey.WRITE_A_MESSAGE)
            etMessage.setOnBackPressedListener { etMessage.clearFocus() }
        }
        return binding?.root
    }

    override fun onStart() {
        super.onStart()
        withBindingSafety(binding) {
            root.setOnThrottleClickListener(createdCompositeDisposable) {}
            initOptionsPopup()
            btnBack.setOnThrottleClickListener(startedCompositeDisposable) {
                navigator.closeFragment(ChatFragment::class.java)
            }
            btnSend.setOnThrottleClickListener(startedCompositeDisposable) {
                val text = etMessage.text.toString().trim()

                if (text.isNotBlank()) {
                    viewModel.sendMessage(text)
                    etMessage.text = null
                }
            }
            btnAttachment.setOnThrottleClickListener(startedCompositeDisposable) {
                etMessage.clearFocus()
                hideKeyboardFrom(etMessage)
                SelectPhotoSourceBottomSheetDialogFragment.newInstance()
                    .show(childFragmentManager, SelectPhotoSourceBottomSheetDialogFragment.TAG)
            }
            btnOptions.setOnThrottleClickListener(startedCompositeDisposable) {
                popupOptionsHelper.show(btnOptions, OPTIONS_BTN)
            }
        }
        viewModel.getChatObservable(chatId)
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleChat)

        adapter.getOnItemClickObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, ::handleItemClick)

        viewModel.getMessageReceivedObservable()
            .subscribeAndDisposeAt(startedCompositeDisposable, viewModel::readMessages)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.rvMessages?.adapter = adapter
    }

    private fun handleChat(chatOptional: Optional<ChatUiModel>) {
        val chat = chatOptional.getOrNull() ?: return
        withBindingSafety(binding) {
            tvUserName.text = chat.userName
            setAvatar(ivAvatar, chat.avatarUrl, 8.dpToPx())
            tvStartPoint.text = chat.startPoint
            tvEndPoint.text = chat.endPoint

            localizeOptionsPopup(chat)

            val messagesIsEmpty = chat.messageList.isEmpty()
            rvMessages.isVisible = !messagesIsEmpty
            svNoMessage.isVisible = messagesIsEmpty
            if (messagesIsEmpty) {
                tvNoMessagesTitle.setLocalizedTextByKey(NO_MESSAGE_TITLE)
                tvNoMessagesDescription.setLocalizedTextByKey(NO_MESSAGE_DESCRIPTION)
            } else {
                adapter.updateList(chat.messageList)
            }
            if (!messagesIsEmpty) rvMessages.scrollToBottom()
        }
    }

    private fun localizeOptionsPopup(chat: ChatUiModel) {
        val callText = if (chat.isDriver) CALL_SENDER else CALL_DRIVER
        val showLocationText = if (chat.isDriver) SHOW_SENDER_ON_A_MAP else SHOW_DRIVER_ON_A_MAP
        popupOptionsHelper.localizeItems(
            localizationManager,
            R.id.tvCall to callText,
            R.id.tvShowLocation to showLocationText,
            R.id.tvParcel to GO_TO_PARCEL,
            R.id.tvReportIssue to REPORT_ISSUE
        )
    }

    private fun initOptionsPopup() {
        popupOptionsHelper.setItemClick(createdCompositeDisposable, R.id.clCall) {
            viewModel.call()
        }
        popupOptionsHelper.setItemClick(createdCompositeDisposable, R.id.clShowLocation) {
            viewModel.showLocation()
        }
        popupOptionsHelper.setItemClick(createdCompositeDisposable, R.id.clShowParcel) {
            viewModel.showParcel()
        }
        popupOptionsHelper.setItemClick(createdCompositeDisposable, R.id.clReportIssue) {
            // do nothing
        }
    }

    private fun handleItemClick(item: BaseChatMessageUiModel) {
        when (item) {
            is OutChatMessageUiModel -> showMessagePopupMenu(item, CURRENT_USER_MESSAGE)
            is InChatMessageUiModel -> showMessagePopupMenu(item, OTHER_USER_MESSAGE)
            else -> Unit
        }
    }

    private fun showMessagePopupMenu(
        item: BaseChatMessageUiModel,
        showFrom: ChatPopupWindowHelper.ShowFrom,
    ) {
        with(popupMessageOptionsHelper) {
            localizeItems(localizationManager, R.id.tvCopy to COPY)
            setItemClick(createdCompositeDisposable, R.id.clCopy) {
                when (item) {
                    is OutChatMessageUiModel -> {
                        val text = item.text
                        val mediaUrl = item.attachment?.mediaUrl
                        val location = item.attachment?.location
                        when {
                            location != null -> Unit
                            text != null -> copyToClipboard(text)
                            mediaUrl != null -> copyToClipboard(mediaUrl)
                        }
                    }

                    is InChatMessageUiModel -> {
                        val text = item.text
                        val mediaUrl = item.attachment?.mediaUrl
                        val location = item.attachment?.location
                        when {
                            location != null -> Unit
                            text != null -> copyToClipboard(text)
                            mediaUrl != null -> copyToClipboard(mediaUrl)
                        }
                    }

                    else -> Unit
                }
            }
            when (item) {
                is OutChatMessageUiModel -> {
                    val mediaUrl = item.attachment?.mediaUrl
                    if (item.status != MessageReadStatus.SENDING || mediaUrl == null) {
                        item.view?.let { show(it, showFrom) }
                    } else {
                        Unit
                    }
                }

                is InChatMessageUiModel -> item.view?.let { show(it, showFrom) }

                else -> Unit
            }
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard =
            requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(text, text)
        clipboard.setPrimaryClip(clip)
    }
}