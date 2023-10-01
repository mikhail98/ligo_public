package com.ligo.feature.chat.recycler

import androidx.viewbinding.ViewBinding
import com.ligo.common.BaseViewHolder
import com.ligo.common.model.BaseChatMessageUiModel
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.subjects.Subject

abstract class BaseChatViewHolder<VB : ViewBinding>(
    binding: VB,
    onItemClickSubject: Subject<BaseChatMessageUiModel>,
    localizationManager: ILocalizationManager,
) : BaseViewHolder<BaseChatMessageUiModel, VB>(binding, onItemClickSubject, localizationManager)