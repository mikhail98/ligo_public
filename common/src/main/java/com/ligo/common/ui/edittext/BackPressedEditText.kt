package com.ligo.common.ui.edittext

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.appcompat.widget.AppCompatEditText

class BackPressedEditText : AppCompatEditText {

    private var onImeBack: ((text: String) -> Unit)? = null

    fun setOnBackPressedListener(listener: (text: String) -> Unit) {
        onImeBack = listener
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            onImeBack?.invoke(this.text.toString())
        }
        return super.dispatchKeyEvent(event)
    }
}