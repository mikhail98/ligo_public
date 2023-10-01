package com.ligo.common.ui.button

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.textview.MaterialTextView
import com.ligo.common.R
import com.ligo.core.setTextColorByRes
import com.ligo.core.R as CoreR

class ActionButton(context: Context, attributeSet: AttributeSet?) :
    MaterialTextView(context, attributeSet) {

    private val attributes by lazy {
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.ActionButton, 0, 0)
    }

    var state: State = State.INACTIVE
        set(value) {
            field = value
            setAppearance()
        }

    init {
        gravity = Gravity.CENTER
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        typeface = ResourcesCompat.getFont(context, R.font.manrope_bold)
        with(TypedValue()) {
            context.theme.resolveAttribute(
                android.R.attr.selectableItemBackground,
                this,
                true
            )
            foreground = getDrawable(context, resourceId)
        }

        try {
            val stateAttr = attributes.getInt(R.styleable.ActionButton_actionStyle, 2)
            state = when (stateAttr) {
                0 -> State.PRIMARY
                1 -> State.SECONDARY
                2 -> State.INACTIVE
                3 -> State.WARNING
                else -> State.INACTIVE
            }
        } catch (e: Throwable) {
            setAppearance()
        } finally {
            attributes.recycle()
        }
    }

    override fun callOnClick(): Boolean {
        return state != State.INACTIVE
    }

    private fun setAppearance() {
        val textColorRes = when (state) {
            State.PRIMARY, State.SECONDARY, State.WARNING -> CoreR.color.white
            State.INACTIVE -> com.ligo.core.R.color.gray_80
        }

        val bgRes = when (state) {
            State.SECONDARY -> R.drawable.bg_btn_secondary
            State.INACTIVE -> R.drawable.bg_btn_inactive
            State.WARNING -> R.drawable.bg_btn_warning
            State.PRIMARY -> R.drawable.bg_btn_primary
        }

        setBackgroundResource(bgRes)
        setTextColorByRes(textColorRes)
    }

    enum class State {
        PRIMARY, SECONDARY, INACTIVE, WARNING
    }
}