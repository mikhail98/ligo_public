package com.ligo.feature.chats.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.ligo.common.BaseViewHolder
import com.ligo.common.ligo.setAvatar
import com.ligo.common.model.ChatListUiModel
import com.ligo.core.R
import com.ligo.core.loadImageWithGlide
import com.ligo.data.MessageReadStatus
import com.ligo.data.model.Attachment
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.chats.databinding.ListItemChatsChatBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject
import java.util.Locale

class ChatViewHolder(
    onItemClickListener: Subject<ChatListUiModel>,
    binding: ListItemChatsChatBinding,
    override val localizationManager: ILocalizationManager,
) : BaseViewHolder<ChatListUiModel, ListItemChatsChatBinding>(
    binding,
    onItemClickListener,
    localizationManager
) {

    companion object {

        fun fromParent(
            parent: ViewGroup,
            onItemClickListener: Subject<ChatListUiModel>,
            localizationManager: ILocalizationManager,
        ): ChatViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListItemChatsChatBinding.inflate(inflater, parent, false)
            return ChatViewHolder(onItemClickListener, binding, localizationManager)
        }
    }

    override fun initView(
        item: ChatListUiModel,
        binding: ListItemChatsChatBinding,
    ) {
        setAvatar(binding.ivAvatar, item.avatarUrl, 12)
        binding.tvTitle.text = item.title
        binding.tvTime.text = item.chatTime

        setupPreview(item, binding.ivPreview)
        setupDescription(item, binding.tvDescription)
        setupStatus(item, binding.ivMessageStatus)

        binding.tvUnreadMessagesCount.isVisible = true && item.unreadCount != 0
        binding.tvUnreadMessagesCount.text = item.unreadCount.toString()
    }

    private fun setupDescription(item: ChatListUiModel, tvDescription: TextView) {
        val attachmentName = item.attachment?.type?.name?.lowercase()
            ?.replaceFirstChar { it.titlecase(Locale.getDefault()) }
        tvDescription.text = attachmentName
            ?: if (item.messageText.isNullOrEmpty()) {
                localizationManager.getLocalized(ConfigStringKey.START_CHAT_NOW)
            } else {
                item.messageText.orEmpty()
            }
    }

    private fun setupPreview(item: ChatListUiModel, ivPreview: ImageView) {
        val attachment = item.attachment
        when (attachment?.type) {
            Attachment.Type.PHOTO, Attachment.Type.VIDEO -> {
                ivPreview.loadImageWithGlide(attachment.mediaUrl.orEmpty())
            }

            Attachment.Type.AUDIO -> {
                ivPreview.loadImageWithGlide(R.drawable.ic_voicemail)
            }

            Attachment.Type.LOCATION -> {
                ivPreview.loadImageWithGlide(com.ligo.common.R.drawable.ic_location_marker_accent)
            }

            else -> Unit
        }
        ivPreview.isVisible = item.attachment != null
    }

    private fun setupStatus(item: ChatListUiModel, ivMessageStatus: ImageView) {
        val statusIcon = when (item.status) {
            MessageReadStatus.READ -> R.drawable.ic_message_read
            MessageReadStatus.SENT -> R.drawable.ic_message_sent
            else -> 0
        }

        ivMessageStatus.isVisible = statusIcon != 0
        if (statusIcon != 0) ivMessageStatus.setImageResource(statusIcon)
    }
}
