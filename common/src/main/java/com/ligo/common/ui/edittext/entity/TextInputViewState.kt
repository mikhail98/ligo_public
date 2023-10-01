package com.ligo.common.ui.edittext.entity

data class TextInputViewState(
    var isActive: Boolean = false,
    var isHighlighted: Boolean = false,
    var hasText: Boolean = false,
    var error: String? = null
)