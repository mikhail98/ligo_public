package com.ligo.feature.chat.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ligo.common.model.BaseChatMessageUiModel
import com.ligo.common.model.InChatMessageUiModel
import com.ligo.common.setOnThrottleClickListener
import com.ligo.core.loadImageWithGlide
import com.ligo.feature.chat.R
import com.ligo.feature.chat.databinding.ListItemInPhotoMessageBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

internal class InPhotoMessageViewHolder(
    private val binding: ListItemInPhotoMessageBinding,
    private val onItemClickSubject: Subject<BaseChatMessageUiModel>,
    localizationManager: ILocalizationManager,
) : BaseChatViewHolder<ListItemInPhotoMessageBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {

    companion object {

        const val VIEW_TYPE = 5

        fun fromParent(
            parent: ViewGroup,
            onItemClickSubject: Subject<BaseChatMessageUiModel>,
            localizationManager: ILocalizationManager,
        ): InPhotoMessageViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return InPhotoMessageViewHolder(
                binding = ListItemInPhotoMessageBinding.inflate(inflater, parent, false),
                onItemClickSubject = onItemClickSubject,
                localizationManager = localizationManager
            )
        }
    }

    override fun bindItem(item: BaseChatMessageUiModel) {
        binding.clRoot.setOnThrottleClickListener(compositeDisposable) {
            val newItem = (item as? InChatMessageUiModel)?.copy(view = binding.clRoot)
            newItem?.let(onItemClickSubject::onNext)
        }
        initView(item, binding)
    }

    override fun initView(
        item: BaseChatMessageUiModel,
        binding: ListItemInPhotoMessageBinding,
    ) {
        if (item is InChatMessageUiModel) {
            binding.ivImage.loadImageWithGlide(
                item.attachment?.mediaUrl.orEmpty(),
                com.ligo.core.R.drawable.placeholder
            )
            binding.tvTime.text = item.time
        }
    }
}