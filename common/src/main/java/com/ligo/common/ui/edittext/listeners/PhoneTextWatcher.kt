package com.ligo.common.ui.edittext.listeners

import android.text.Editable
import android.text.TextWatcher

class PhoneTextWatcher(private val listener: (phone: String) -> Unit) : TextWatcher {

    override fun afterTextChanged(s: Editable) {
        val lastIndex = s.lastIndexOf('+')
        when {
            lastIndex == -1 -> if (s.isNotEmpty()) s.insert(0, "+")
            lastIndex != 0 -> s.delete(lastIndex, lastIndex + 1)
        }
        listener.invoke(s.toString())
    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
    }
}