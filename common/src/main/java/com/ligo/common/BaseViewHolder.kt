package com.ligo.common

import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.Subject

abstract class BaseViewHolder<VM : Any, VB : ViewBinding>(
    private val binding: VB,
    private val onItemClickSubject: Subject<VM>,
    override val localizationManager: ILocalizationManager,
) : RecyclerView.ViewHolder(binding.root), ViewContainer {

    protected val compositeDisposable = CompositeDisposable()

    open fun bindItem(item: VM) {
        binding.root.setOnThrottleClickListener(compositeDisposable) {
            onItemClickSubject.onNext(item)
        }
        initView(item, binding)
    }

    abstract fun initView(item: VM, binding: VB)

    fun recycle() {
        compositeDisposable.clear()
    }
}