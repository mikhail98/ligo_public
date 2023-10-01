package com.ligo.feature.chat

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.widget.PopupWindow
import android.widget.TextView
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import com.ligo.common.setAnimationListener
import com.ligo.common.setOnThrottleClickListener
import com.ligo.core.dpToPx
import com.ligo.feature.chat.ChatPopupWindowHelper.ShowFrom.CURRENT_USER_MESSAGE
import com.ligo.feature.chat.ChatPopupWindowHelper.ShowFrom.OPTIONS_BTN
import com.ligo.feature.chat.ChatPopupWindowHelper.ShowFrom.OTHER_USER_MESSAGE
import com.ligo.tools.api.ILocalizationManager
import io.reactivex.rxjava3.disposables.CompositeDisposable

internal class ChatPopupWindowHelper(
    private val context: Context,
    @LayoutRes private val layoutResId: Int,
    private val hideAnimation: Animation,
) {

    private var popupView: View? = null
    private var popupWindow: PopupWindow? = null

    init {
        getPopupWindow()
    }

    private fun getPopupWindow(): PopupWindow? {
        return if (popupWindow != null) {
            popupWindow
        } else {
            val inflater =
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val customMenuView = inflater.inflate(layoutResId, null)
            popupView = customMenuView

            popupWindow = PopupWindow(
                customMenuView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                false
            ).apply {
                isOutsideTouchable = true
                animationStyle = R.style.PopupAnimation
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }

            customMenuView.clipToOutline = true

            popupWindow
        }
    }

    fun setItemClick(disposable: CompositeDisposable, @IdRes itemId: Int, action: () -> Unit) {
        popupView?.findViewById<ConstraintLayout>(itemId)
            ?.apply {
                setOnThrottleClickListener(disposable) {
                    popupView?.startAnimation(hideAnimation)

                    hideAnimation.setAnimationListener(actionEnd = {
                        popupWindow?.dismiss()
                        action.invoke()
                    })
                }
            }
    }

    fun localizeItems(localizationManager: ILocalizationManager, vararg items: Pair<Int, String>) {
        items.forEach { (viewId, configStringKey) ->
            popupView?.findViewById<TextView>(viewId)?.text =
                localizationManager.getLocalized(configStringKey)
        }
    }

    fun show(view: View, showFrom: ShowFrom) {
        val location = IntArray(2)
        view.getLocationOnScreen(location)

        val (x, y) = when (showFrom) {
            OPTIONS_BTN -> location[0] to location[1] + view.height + 6.dpToPx()
            CURRENT_USER_MESSAGE -> location[0] - 0.9 * view.width to location[1] + view.height + 2.dpToPx()
            OTHER_USER_MESSAGE -> location[0] + view.width / 2 to location[1] + view.height + 2.dpToPx()
        }

        popupWindow?.showAtLocation(view, Gravity.NO_GRAVITY, x.toInt(), y)
    }

    enum class ShowFrom {
        CURRENT_USER_MESSAGE,
        OTHER_USER_MESSAGE,
        OPTIONS_BTN
    }
}
