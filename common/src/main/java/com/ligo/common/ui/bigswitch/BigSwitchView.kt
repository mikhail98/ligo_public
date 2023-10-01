package com.ligo.common.ui.bigswitch

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.text.TextPaint
import android.util.AttributeSet
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.core.animation.doOnEnd
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.toRect
import androidx.core.view.doOnLayout
import com.ligo.common.R
import com.ligo.core.dpToPx
import com.ligo.core.R as CoreR

class BigSwitchView(context: Context, attributeSet: AttributeSet) : View(context, attributeSet) {

    companion object {
        private const val ANIMATION_DURATION = 200L
        private const val MAX_ALPHA = 255
        private const val INACTIVE_ALPHA = 0.5f

        private val TEXT_SIZE = 14f.dpToPx()
        private val BG_HEIGHT = 48f.dpToPx()
        private val HALF_VERTICAL = BG_HEIGHT / 2f
        private val BG_RADIUS = BG_HEIGHT / 2f
        private val SWITCH_HEIGHT = 44f.dpToPx()
        private val SWITCH_RADIUS = SWITCH_HEIGHT / 2f
        private val MARGIN = (BG_HEIGHT - SWITCH_HEIGHT) / 2f
        private val LOCKED_IMAGE_MARGIN = 8f.dpToPx()
        private val LOCKED_IMAGE_SIZE = 12f.dpToPx()
        private val LOCKED_IMAGE_TOP = (BG_HEIGHT - LOCKED_IMAGE_SIZE) / 2f
    }

    private var leftText: String = ""
    private var rightText: String = ""

    private var isLocked: Boolean = false
    private var isChecked: Boolean = false
    private var isAnimating: Boolean = false

    private var checkChangeListener: (Boolean.() -> Unit)? = null
    private var lockedClickListener: (() -> Unit)? = null

    private var drawData: DrawData? = null
    private var switchWidth = 0f
    private var halfScreen = 0f
    private var quarterScreen = 0f
    private var lockedLeft = 0f

    private val textBounds: Rect = Rect()

    private val valueAnimator = ValueAnimator().apply {
        this.duration = ANIMATION_DURATION
        this.interpolator = AccelerateDecelerateInterpolator()

        doOnEnd {
            isAnimating = false
        }
    }

    private val bgPaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, CoreR.color.gray_20)
        }
    }

    private val switchPaint by lazy {
        Paint().apply {
            color = ContextCompat.getColor(context, CoreR.color.gray_40)
        }
    }

    private fun getTextPaint(alpha: Float) = TextPaint().apply {
        this.isAntiAlias = true
        this.textAlign = Paint.Align.CENTER
        this.typeface = ResourcesCompat.getFont(context, R.font.manrope_regular)
        this.color = Color.WHITE
        this.alpha = (alpha * MAX_ALPHA).toInt()
        this.textSize = TEXT_SIZE
    }

    init {
        setOnClickListener {
            if (isLocked) {
                lockedClickListener?.invoke()
            } else {
                if (!isAnimating) {
                    isAnimating = true
                    setChecked(!isChecked)
                }
            }
        }
        doOnLayout {
            halfScreen = width / 2f
            quarterScreen = halfScreen / 2f
            switchWidth = (width - MARGIN * 4f) / 2f
            updateCheckedUI()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        drawData?.apply {
            canvas.drawRoundRect(bgRect, BG_RADIUS, BG_RADIUS, bgPaint)
            canvas.drawRoundRect(switchRect, SWITCH_RADIUS, SWITCH_RADIUS, switchPaint)

            canvas.drawText(leftText.text, leftText.x, leftText.y, leftText.paint)
            canvas.drawText(rightText.text, rightText.x, rightText.y, rightText.paint)

            lockedDrawable?.draw(canvas)
        }
    }

    fun setLocked(newState: Boolean) {
        isLocked = newState
        updateLockedUi()
    }

    private fun updateLockedUi() {
        if (isLocked) {
            val drawable = ContextCompat.getDrawable(context, R.drawable.ic_forbidden)?.mutate()
            val left = lockedLeft
            val top = LOCKED_IMAGE_TOP
            val right = left + LOCKED_IMAGE_SIZE
            val bottom = top + LOCKED_IMAGE_SIZE

            val rect = RectF(left, top, right, bottom)
            drawable?.bounds = rect.toRect()
            this.drawData = this.drawData?.copy(lockedDrawable = drawable)

            setChecked(true)
        } else {
            this.drawData = this.drawData?.copy(lockedDrawable = null)
            invalidate()
        }
    }

    private fun updateCheckedUI() {
        val startPos = if (isChecked) {
            width / 2f + MARGIN
        } else {
            MARGIN
        }
        val bgRect = RectF(0f, 0f, width.toFloat(), BG_HEIGHT)
        val switchRect = getSwitchRect(startPos)
        val textAlpha = if (isChecked) 1f else 0f

        drawData = DrawData(
            bgRect,
            switchRect,
            getTextData(leftText, quarterScreen, textAlpha, true),
            getTextData(rightText, quarterScreen + halfScreen, 1f - textAlpha)
        )
        updateLockedUi()
    }

    fun init(leftText: String, rightText: String) {
        this.leftText = leftText
        this.rightText = rightText
        invalidate()
    }

    fun setChecked(newState: Boolean) {
        isChecked = newState

        checkChangeListener?.invoke(isChecked)

        if (!isChecked) {
            valueAnimator.setFloatValues(1f, 0f)
        } else {
            valueAnimator.setFloatValues(0f, 1f)
        }

        valueAnimator.removeAllUpdateListeners()
        valueAnimator.addUpdateListener {
            val progress = it.animatedValue as Float
            val switchRect = getSwitchRect(MARGIN + halfScreen * progress)

            drawData = drawData?.copy(
                switchRect = switchRect,
                leftText = getTextData(leftText, quarterScreen, progress),
                rightText = getTextData(rightText, quarterScreen + halfScreen, 1f - progress)
            )
            postInvalidateOnAnimation()
        }
        valueAnimator.start()
    }

    private fun getSwitchRect(startPos: Float): RectF {
        return RectF(
            startPos,
            MARGIN,
            startPos + switchWidth,
            MARGIN + SWITCH_HEIGHT
        )
    }

    private fun getTextData(
        text: String,
        x: Float,
        alpha: Float,
        initTextEnd: Boolean = false,
    ): TextData {
        val textAlpha = INACTIVE_ALPHA + (1f - INACTIVE_ALPHA) * (1f - alpha)
        val textPaint = getTextPaint(textAlpha)
        textPaint.getTextBounds(text, 0, text.length, textBounds)
        val y = HALF_VERTICAL - textBounds.exactCenterY() + MARGIN

        if (initTextEnd) {
            lockedLeft = quarterScreen + textBounds.exactCenterX() + LOCKED_IMAGE_MARGIN
        }

        return TextData(text, x, y, textPaint)
    }

    fun setOnCheckChangeListener(listener: Boolean.() -> Unit) {
        this.checkChangeListener = listener
    }

    fun setOnLockedClickListener(listener: () -> Unit) {
        this.lockedClickListener = listener
    }

    internal data class DrawData(
        val bgRect: RectF,
        val switchRect: RectF,
        val leftText: TextData,
        val rightText: TextData,
        val lockedDrawable: Drawable? = null,
    )

    internal data class TextData(
        val text: String,
        val x: Float,
        val y: Float,
        val paint: Paint,
    )
}