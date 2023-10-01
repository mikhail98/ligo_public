package com.ligo.common.ui.recycler

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AutoScrollRecyclerView(
    context: Context,
    attributeSet: AttributeSet?,
) : RecyclerView(context, attributeSet) {

    private var lastVisiblePosition: Int = 0

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)

        if (changed) {
            smoothScrollToPosition(lastVisiblePosition)
        }
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)
        when (state) {
            SCROLL_STATE_IDLE -> updateLastVisiblePosition()
        }
    }

    private fun updateLastVisiblePosition() {
        (layoutManager as? LinearLayoutManager)?.let { lm ->
            lastVisiblePosition = lm.findLastVisibleItemPosition()
        }
    }

    fun scrollToBottom() {
        lastVisiblePosition = adapter?.itemCount?.minus(1) ?: 0
        if (lastVisiblePosition >= 0) {
            scrollToPosition(lastVisiblePosition)
        }
    }
}