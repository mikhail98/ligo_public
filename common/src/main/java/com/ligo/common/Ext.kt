package com.ligo.common

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.viewbinding.ViewBinding
import com.jakewharton.rxbinding4.view.clicks
import com.ligo.common.ui.edittext.listeners.TextListener
import com.ligo.core.printError
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

fun <T : Any> Observable<T>.subscribeAndDisposeAt(
    compositeDisposable: CompositeDisposable,
    action: (T) -> Unit,
) {
    observeOn(AndroidSchedulers.mainThread())
        .subscribe(action::invoke, ::printError)
        .also(compositeDisposable::add)
}

fun View.setOnThrottleClickListener(
    disposables: CompositeDisposable,
    listener: (Unit) -> Unit,
) {
    clicks()
        .throttleFirst(500, TimeUnit.MILLISECONDS)
        .subscribe(listener)
        .also(disposables::add)
}

fun View.setVisibilityWithAlpha(isVisible: Boolean, duration: Long = 150L) {
    if (isVisible && this.visibility != View.VISIBLE) {
        alpha = 0f
        this.isVisible = true
        animate().alpha(1f).setDuration(duration).start()
    } else if (!isVisible && this.visibility == View.VISIBLE) {
        alpha = 1f
        animate().alpha(0f).setDuration(duration).start()
        Handler(Looper.getMainLooper()).postDelayed({ this.isVisible = false }, duration)
    }
}

fun hideKeyboardFrom(view: View?) {
    view ?: return
    val imm = view.context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
}

fun showKeyboardFrom(view: View?) {
    view ?: return
    val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(view, 0)
}

fun setOnTextChangedListener(editText: EditText, listener: (String) -> Unit) {
    editText.addTextChangedListener(TextListener { listener.invoke(it) })
}

fun <VB : ViewBinding> withBindingSafety(binding: VB?, action: VB.() -> Unit) {
    binding?.let {
        action(it)
    }
}

fun getDebounceTextChangedObservable(
    editText: EditText,
    debounce: Long,
): Observable<String> {
    return Observable.create { emitter ->
        editText.doAfterTextChanged { editable -> emitter.onNext(editable.toString()) }
    }.debounce(debounce, TimeUnit.MILLISECONDS).map { it.trim() }.distinctUntilChanged()
        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
}

fun Animation.setAnimationListener(
    actionStart: () -> Unit = {},
    actionEnd: () -> Unit = {},
    actionRepeat: () -> Unit = {},
) {
    setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationStart(animation: Animation?) {
            actionStart.invoke()
        }

        override fun onAnimationEnd(animation: Animation?) {
            actionEnd.invoke()
        }

        override fun onAnimationRepeat(animation: Animation?) {
            actionRepeat.invoke()
        }
    })
}