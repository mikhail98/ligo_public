package com.ligo.feature.chat.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import com.ligo.common.model.BaseChatMessageUiModel
import com.ligo.common.model.OutChatMessageUiModel
import com.ligo.common.setOnThrottleClickListener
import com.ligo.core.R
import com.ligo.core.loadImageWithGlide
import com.ligo.data.MessageReadStatus
import com.ligo.feature.chat.databinding.ListItemOutPhotoMessageBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

internal class OutPhotoMessageViewHolder(
    private val binding: ListItemOutPhotoMessageBinding,
    private val onItemClickSubject: Subject<BaseChatMessageUiModel>,
    localizationManager: ILocalizationManager,
) : BaseChatViewHolder<ListItemOutPhotoMessageBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {

    companion object {

        const val VIEW_TYPE = 4

        fun fromParent(
            parent: ViewGroup,
            onItemClickSubject: Subject<BaseChatMessageUiModel>,
            localizationManager: ILocalizationManager,
        ): OutPhotoMessageViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return OutPhotoMessageViewHolder(
                binding = ListItemOutPhotoMessageBinding.inflate(inflater, parent, false),
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
        binding: ListItemOutPhotoMessageBinding,
    ) {
        if (item is OutChatMessageUiModel) {
            binding.ivImage.loadImageWithGlide(
                item.attachment?.mediaUrl.orEmpty(),
                R.drawable.placeholder
            )
            binding.tvTime.text = item.time
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