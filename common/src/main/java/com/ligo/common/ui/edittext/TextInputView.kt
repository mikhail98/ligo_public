package com.ligo.common.ui.edittext

import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.animation.TranslateAnimation
import android.widget.FrameLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.ligo.common.R
import com.ligo.common.databinding.ViewTextInputViewBinding
import com.ligo.common.setOnTextChangedListener
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.showKeyboardFrom
import com.ligo.common.ui.edittext.entity.TextInputViewState
import com.ligo.core.dpToPx
import io.reactivex.rxjava3.disposables.CompositeDisposable
import com.ligo.core.R as CoreR

class TextInputView(context: Context, attributeSet: AttributeSet? = null) :
    FrameLayout(context, attributeSet) {

    companion object {
        private const val ANIM_DURATION = 150L
    }

    private val binding by lazy {
        ViewTextInputViewBinding.inflate(LayoutInflater.from(context), this)
    }
    private val tvHint by lazy { binding.tvHint }
    private val tvError by lazy { binding.tvError }
    private val etText by lazy { binding.etText }
    private val clBackground by lazy { binding.clBackground }
    private val ivDrawableRight by lazy { binding.ivDrawableRight }
    private val tvTextRight by lazy { binding.tvTextRight }

    private val currentState = TextInputViewState()

    private val colorWhite = ContextCompat.getColor(context, CoreR.color.white)
    private val colorSecondary = ContextCompat.getColor(context, CoreR.color.gray_80)
    private val colorErrorPrimary = ContextCompat.getColor(context, CoreR.color.red_error)
    private val colorErrorSecondary =
        ContextCompat.getColor(context, CoreR.color.red_error_secondary)
    private val colorHighlightedHint = ContextCompat.getColor(context, CoreR.color.blue_light)
    private val colorHighlightedText = ContextCompat.getColor(context, CoreR.color.blue_super_light)

    private val attributes by lazy {
        context.theme.obtainStyledAttributes(attributeSet, R.styleable.TextInput, 0, 0)
    }
    private var attrHint: String? = null
    private var drawableRight: Drawable? = null
    private var textRight: String? = null

    init {
        try {
            attrHint = attributes.getString(R.styleable.TextInput_hint)
            drawableRight = attributes.getDrawable(R.styleable.TextInput_drawableRight)
            textRight = attributes.getString(R.styleable.TextInput_textRight)
        } finally {
            attributes.recycle()
        }
        setHint(attrHint)

        clBackground.clipToOutline = true

        setOnTextChangedListener(etText) {
            updateViewWithNewState(currentState.copy(hasText = it.isNotEmpty()))
        }
        etText.setOnBackPressedListener {
            etText.clearFocus()
        }
        etText.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                updateViewWithNewState(currentState.copy(isActive = false))
            }
        }
        drawableRight?.let { drawable ->
            ivDrawableRight.setImageDrawable(drawable)
            ivDrawableRight.isVisible = true
        }

        textRight?.let { text ->
            tvTextRight.text = text
            tvTextRight.isVisible = true
        }
        setOnClickListener {
            updateViewWithNewState(currentState.copy(isActive = true))
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        etText.id = (System.currentTimeMillis() % 1000).toInt()
    }

    private fun animateToNoTextNoErrorInactive() {
        tvHint.animateTextColor(colorSecondary, colorWhite)
        tvHint.animateTextSize(12f, 14f)
        animateHintPosition(0f)
        etText.isVisible = false
    }

    private fun animateToTextNoError() {
        tvHint.animateTextColor(colorWhite, colorSecondary)
        tvHint.animateTextSize(14f, 12f)
        val offset = -8f - 1f.dpToPx()
        animateHintPosition(offset)
        etText.isVisible = true
    }

    private fun TextView.animateTextColor(fromColor: Int, toColor: Int) {
        val animator = ObjectAnimator.ofInt(this, "textColor", fromColor, toColor)
        animator.setEvaluator(ArgbEvaluator())
        animator.duration = ANIM_DURATION
        animator.start()
    }

    private fun TextView.animateTextSize(fromSize: Float, toSize: Float) {
        val animator = ObjectAnimator.ofFloat(this, "textSize", fromSize, toSize)
        animator.duration = ANIM_DURATION
        animator.start()
    }

    private fun animateHintPosition(offset: Float) {
        val deltaStart = 0f

        val positionAnim = TranslateAnimation(
            deltaStart,
            deltaStart,
            deltaStart,
            offset.dpToPx()
        )
        positionAnim.fillAfter = true
        positionAnim.duration = ANIM_DURATION
        tvHint.startAnimation(positionAnim)
    }

    private fun updateViewWithNewState(newState: TextInputViewState) {
        if (currentState == newState) return

        if (currentState.isActive && !newState.isActive) {
            if (!newState.hasText) {
                animateToNoTextNoErrorInactive()
            }
            etText.clearFocus()
        }
        if (!currentState.isActive && newState.isActive) {
            if (!newState.hasText) {
                animateToTextNoError()
            }
            showKeyboardWithDelay()
        }

        if (currentState.error != null && newState.error == null) {
            if (newState.hasText) {
                clBackground.setBackgroundResource(R.drawable.bg_input_filled)
            } else {
                clBackground.setBackgroundResource(R.drawable.bg_input)
            }
            tvError.text = null
            tvError.isVisible = false
            if (newState.hasText) {
                tvHint.animateTextColor(colorErrorSecondary, colorSecondary)
                etText.animateTextColor(colorErrorPrimary, colorWhite)
            } else {
                tvHint.animateTextColor(colorErrorPrimary, colorWhite)
                etText.animateTextColor(colorErrorPrimary, colorWhite)
            }
        }

        if (currentState.error == null && newState.error != null) {
            clBackground.setBackgroundResource(R.drawable.bg_input_error)
            tvError.text = newState.error
            tvError.isVisible = true
            if (newState.hasText) {
                tvHint.animateTextColor(colorSecondary, colorErrorSecondary)
                etText.animateTextColor(colorWhite, colorErrorPrimary)
            } else {
                tvHint.animateTextColor(colorWhite, colorErrorPrimary)
                etText.animateTextColor(colorWhite, colorErrorPrimary)
            }
        }

        if (currentState.hasText && !newState.hasText) {
            if (!currentState.isActive) {
                animateToNoTextNoErrorInactive()
            }
            clBackground.setBackgroundResource(R.drawable.bg_input)
        }
        if (!currentState.hasText && newState.hasText) {
            if (!currentState.isActive) {
                animateToTextNoError()
            }
            clBackground.setBackgroundResource(R.drawable.bg_input_filled)
        }

        if (newState.isHighlighted) {
            tvHint.animateTextColor(tvHint.currentHintTextColor, colorHighlightedHint)
            etText.animateTextColor(etText.currentTextColor, colorHighlightedText)
            clBackground.setBackgroundResource(R.drawable.bg_radio_btn_checked)
        }

        if (!newState.isHighlighted && currentState.isHighlighted) {
            currentState.isHighlighted = false
            clBackground.background = ContextCompat.getDrawable(context, R.drawable.bg_input)
            if (newState.hasText || newState.isActive) {
                tvHint.animateTextColor(tvHint.currentHintTextColor, colorSecondary)
                etText.animateTextColor(etText.currentTextColor, colorWhite)
            } else {
                tvHint.animateTextColor(tvHint.currentHintTextColor, colorWhite)
                etText.animateTextColor(etText.currentTextColor, colorWhite)
            }
        }

        currentState.isHighlighted = newState.isHighlighted
        currentState.hasText = newState.hasText
        currentState.error = newState.error
        currentState.isActive = newState.isActive
    }

    private fun showKeyboardWithDelay() {
        etText.requestFocus()
        showKeyboardFrom(etText)
        etText.setSelection(etText.text?.length ?: 0)
    }

    fun highlight(isHighlighted: Boolean = true) {
        updateViewWithNewState(newState = currentState.copy(isHighlighted = isHighlighted))
    }

    fun setHint(@StringRes textId: Int) {
        tvHint.setText(textId)
    }

    fun setHint(text: String?) {
        tvHint.text = text
    }

    fun setText(@StringRes textId: Int) {
        etText.setText(textId)
    }

    fun setText(text: String?) {
        etText.setText(text)
    }

    fun getText(): String = etText.text.toString()

    fun getError(): String = tvError.text.toString()

    fun getEditText(): BackPressedEditText = etText

    fun setErrorRes(@StringRes textId: Int?) {
        if (textId == null) {
            updateViewWithNewState(currentState.copy(error = null))
        } else {
            setError(context.getString(textId))
        }
    }

    fun setErrorRes(@StringRes textId: Int) {
        setError(context.getString(textId))
    }

    fun setError(text: String?) {
        updateViewWithNewState(currentState.copy(error = text))
    }

    fun setOnTivThrottleClickListener(disposable: CompositeDisposable, listener: () -> Unit) {
        getEditText().isFocusableInTouchMode = false
        getEditText().isFocusable = false
        setOnThrottleClickListener(disposable) {
            listener.invoke()
        }
        getEditText().setOnThrottleClickListener(disposable) {
            listener.invoke()
        }
    }
}