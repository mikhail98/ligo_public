package com.ligo.feature.chats.recycler

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ligo.common.model.ChatListUiModel
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject

internal class ChatsAdapter(
    private val localizationManager: ILocalizationManager,
) : RecyclerView.Adapter<ChatViewHolder>() {

    private var items: List<ChatListUiModel> = mutableListOf()
    private val onItemClickSubject = PublishSubject.create<ChatListUiModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        return ChatViewHolder.fromParent(parent, onItemClickSubject, localizationManager)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bindItem(item = items[position])
    }

    override fun getItemCount() = items.size

    fun setItems(items: List<ChatListUiModel>) {
        this.items = items
        notifyDataSetChanged()
    }

    fun getOnItemClickObservable(): Observable<ChatListUiModel> =
        onItemClickSubject
}
