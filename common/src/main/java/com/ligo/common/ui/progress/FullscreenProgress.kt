package com.ligo.common.ui.progress

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.ligo.common.databinding.ViewFullscreenProgressBinding

class FullscreenProgress(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    private val binding by lazy {
        ViewFullscreenProgressBinding.inflate(LayoutInflater.from(context), this)
    }

    init {
        binding.root.setOnClickListener { }
    }
}