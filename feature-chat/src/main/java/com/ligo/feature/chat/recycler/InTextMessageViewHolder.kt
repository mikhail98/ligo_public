package com.ligo.feature.chat.recycler

import android.text.util.Linkify
import android.view.LayoutInflater
import android.view.ViewGroup
import com.ligo.common.model.BaseChatMessageUiModel
import com.ligo.common.model.InChatMessageUiModel
import com.ligo.common.setOnThrottleClickListener
import com.ligo.core.R
import com.ligo.core.setLinkTextColorByRes
import com.ligo.feature.chat.databinding.ListItemInTextMessageBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

internal class InTextMessageViewHolder(
    private val binding: ListItemInTextMessageBinding,
    private val onItemClickSubject: Subject<BaseChatMessageUiModel>,
    localizationManager: ILocalizationManager,
) : BaseChatViewHolder<ListItemInTextMessageBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {

    companion object {

        const val VIEW_TYPE = 3

        fun fromParent(
            parent: ViewGroup,
            onItemClickSubject: Subject<BaseChatMessageUiModel>,
            localizationManager: ILocalizationManager,
        ): InTextMessageViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return InTextMessageViewHolder(
                binding = ListItemInTextMessageBinding.inflate(inflater, parent, false),
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
        binding: ListItemInTextMessageBinding,
    ) {
        if (item is InChatMessageUiModel) {
            binding.tvTime.text = item.time

            binding.tvText.text = item.text
            binding.tvText.setLinkTextColorByRes(R.color.white)
            Linkify.addLinks(
                binding.tvText,
                Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES or Linkify.PHONE_NUMBERS
            )
        }
    }
}