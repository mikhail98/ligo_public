package com.ligo.feature.chat.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ligo.common.model.BaseChatMessageUiModel
import com.ligo.common.model.ChatMessageDateUiModel
import com.ligo.common.model.InChatMessageUiModel
import com.ligo.common.model.OutChatMessageUiModel
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import io.reactivex.rxjava3.subjects.Subject

internal class ChatAdapter(private val localizationManager: ILocalizationManager) :
    RecyclerView.Adapter<BaseChatViewHolder<*>>() {

    private var chatItems = listOf<BaseChatMessageUiModel>()

    private val onItemClickSubject: Subject<BaseChatMessageUiModel> =
        PublishSubject.create<BaseChatMessageUiModel>().toSerialized()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): BaseChatViewHolder<*> {
        return when (viewType) {
            ChatDateViewHolder.VIEW_TYPE ->
                ChatDateViewHolder
                    .fromParent(parent, onItemClickSubject, localizationManager)

            OutTextMessageViewHolder.VIEW_TYPE ->
                OutTextMessageViewHolder
                    .fromParent(parent, onItemClickSubject, localizationManager)

            InTextMessageViewHolder.VIEW_TYPE ->
                InTextMessageViewHolder
                    .fromParent(parent, onItemClickSubject, localizationManager)

            OutPhotoMessageViewHolder.VIEW_TYPE ->
                OutPhotoMessageViewHolder
                    .fromParent(parent, onItemClickSubject, localizationManager)

            InPhotoMessageViewHolder.VIEW_TYPE ->
                InPhotoMessageViewHolder
                    .fromParent(parent, onItemClickSubject, localizationManager)

            else -> error("view type: $viewType does not supported")
        }
    }

    override fun onBindViewHolder(holder: BaseChatViewHolder<*>, position: Int) {
        holder.bindItem(chatItems[position])
    }

    override fun getItemCount() = chatItems.size

    override fun getItemViewType(position: Int): Int {
        return when (val item = chatItems[position]) {
            is ChatMessageDateUiModel -> ChatDateViewHolder.VIEW_TYPE
            is OutChatMessageUiModel -> if (item.text != null) {
                OutTextMessageViewHolder.VIEW_TYPE
            } else {
                OutPhotoMessageViewHolder.VIEW_TYPE
            }

            is InChatMessageUiModel -> if (item.text != null) {
                InTextMessageViewHolder.VIEW_TYPE
            } else {
                InPhotoMessageViewHolder.VIEW_TYPE
            }

            else -> 0
        }
    }

    fun updateList(data: List<BaseChatMessageUiModel>) {
        chatItems = data
        notifyDataSetChanged()
    }

    fun getOnItemClickObservable(): Observable<BaseChatMessageUiModel> {
        return onItemClickSubject
    }
}