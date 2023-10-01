package com.ligo.common

import android.widget.TextView
import androidx.core.view.isVisible
import com.ligo.common.ui.edittext.TextInputView
import com.ligo.tools.api.ILocalizationManager

interface ViewContainer {

    val localizationManager: ILocalizationManager

    fun TextView.setLocalizedTextByKey(key: String) {
        text = localizationManager.getLocalized(key)
    }

    fun TextView.displayLocalizedText(key: String?) {
        isVisible = key != null
        key?.let { text = localizationManager.getLocalized(it) }
    }

    fun TextInputView.setLocalizedHintByKey(key: String) {
        setHint(localizationManager.getLocalized(key))
    }

    fun getLocalizedStringByKey(key: String): String {
        return localizationManager.getLocalized(key)
    }
}