package com.ligo.feature.chat.recycler

import android.view.LayoutInflater
import android.view.ViewGroup
import com.ligo.common.model.BaseChatMessageUiModel
import com.ligo.common.model.ChatMessageDateUiModel
import com.ligo.core.getFormattedDate
import com.ligo.core.getTimeInMillis
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.chat.databinding.ListItemChatDateBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

internal class ChatDateViewHolder(
    private val binding: ListItemChatDateBinding,
    private val onItemClickSubject: Subject<BaseChatMessageUiModel>,
    localizationManager: ILocalizationManager,
) : BaseChatViewHolder<ListItemChatDateBinding>(
    binding,
    onItemClickSubject,
    localizationManager
) {

    companion object {

        const val VIEW_TYPE = 1

        fun fromParent(
            parent: ViewGroup,
            onItemClickSubject: Subject<BaseChatMessageUiModel>,
            localizationManager: ILocalizationManager,
        ): ChatDateViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            return ChatDateViewHolder(
                binding = ListItemChatDateBinding.inflate(inflater, parent, false),
                onItemClickSubject = onItemClickSubject,
                localizationManager = localizationManager
            )
        }
    }

    override fun initView(item: BaseChatMessageUiModel, binding: ListItemChatDateBinding) {
        if (item is ChatMessageDateUiModel) {
            val dateFormat = "dd MMMM"
            val date = getFormattedDate(getTimeInMillis(item.createdAt), dateFormat)
            val currentDate = getFormattedDate(System.currentTimeMillis(), dateFormat)

            binding.tvDate.text = if (date == currentDate) {
                localizationManager.getLocalized(ConfigStringKey.TODAY)
            } else {
                date
            }
        }
    }
}