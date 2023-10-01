package com.ligo.feature.chat.recycler

import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ligo.common.model.BaseChatMessageUiModel
import com.ligo.common.model.OutChatMessageUiModel
import com.ligo.common.setOnThrottleClickListener
import com.ligo.core.R
import com.ligo.core.setLinkTextColorByRes
import com.ligo.data.MessageReadStatus
import com.ligo.feature.chat.databinding.ListItemOutTextMessageBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

internal class OutTextMessageViewHolder(
    private val binding: ListItemOutTextMessageBinding,
    private val onItemClickSubject: Subject<BaseChatMessageUiModel>,
    localizationManager: ILocalizationManager,
) : BaseChatViewHolder<ListItemOutTextMessageBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {

    companion object {

        const val VIEW_TYPE = 2

        fun fromParent(
            parent: ViewGroup,
            onItemClickSubject: Subject<BaseChatMessageUiModel>,
            localizationManager: ILocalizationManager,
        ): OutTextMessageViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return OutTextMessageViewHolder(
                binding = ListItemOutTextMessageBinding.inflate(inflater, parent, false),
                onItemClickSubject = onItemClickSubject,
                localizationManager = localizationManager
            )
        }
    }

    override fun bindItem(item: BaseChatMessageUiModel) {
        binding.clRoot.setOnThrottleClickListener(compositeDisposable) {
            val newItem = (item as? OutChatMessageUiModel)?.copy(view = binding.clRoot)
            newItem?.let(onItemClickSubject::onNext)
        }
        initView(item, binding)
    }

    override fun initView(
        item: BaseChatMessageUiModel,
        binding: ListItemOutTextMessageBinding,
    ) {
        if (item is OutChatMessageUiModel) {
            binding.tvTime.text = item.time

            binding.tvText.text = item.text
            binding.tvText.setLinkTextColorByRes(R.color.white)
            Linkify.addLinks(
                binding.tvText,
                Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS
            )

            when (item.status) {
                MessageReadStatus.READ -> binding.ivStatus.setImageResource(R.drawable.ic_message_read)
                MessageReadStatus.SENT -> binding.ivStatus.setImageResource(R.drawable.ic_message_sent)
                MessageReadStatus.SENDING -> Unit
            }

            binding.ivStatus.isVisible =
                item.status == MessageReadStatus.SENT || item.status == MessageReadStatus.READ
            binding.progress.isVisible = item.status == MessageReadStatus.SENDING
        }
    }
}