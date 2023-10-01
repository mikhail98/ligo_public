package com.ligo.feature.auth

import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.ligo.common.BaseFragment
import com.ligo.common.setOnThrottleClickListener
import com.ligo.common.setVisibilityWithAlpha
import com.ligo.common.subscribeAndDisposeAt
import com.ligo.common.withBindingSafety
import com.ligo.data.model.ConfigStringKey
import com.ligo.feature.auth.databinding.FragmentAuthBinding
import org.koin.android.ext.android.inject
import org.koin.core.module.Module
import com.ligo.core.R as CoreR

class AuthFragment : BaseFragment<AuthFragmentViewModel>() {

    companion object {
        fun newInstance(): Fragment {
            return AuthFragment()
        }
    }

    override val koinModule: Module = AuthModule
    override val viewModel by inject<AuthFragmentViewModel>()

    private var binding: FragmentAuthBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.signOutGoogle()
        viewModel.getOnLoadingObservable()
            .subscribeAndDisposeAt(createdCompositeDisposable, ::handleLoadingState)
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding?.progress?.setVisibilityWithAlpha(isLoading)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentAuthBinding.inflate(inflater, container, false)
        withBindingSafety(binding) {
            tvTitle.setLocalizedTextByKey(ConfigStringKey.AUTH_TITLE)
            tvGoogle.setLocalizedTextByKey(ConfigStringKey.CONTINUE_WITH_GOOGLE)
            tvDescription.setLocalizedTextByKey(ConfigStringKey.AUTH_DESCRIPTION)

            clGoogle.clipToOutline = true

            val prefix =
                localizationManager.getLocalized(ConfigStringKey.BY_CONTINUING_YOU_ACCEPT)
            val privacyPolicy =
                localizationManager.getLocalized(ConfigStringKey.PRIVACY_POLICY)
            val privacyPolicyUrl =
                localizationManager.getLocalized(ConfigStringKey.PRIVACY_POLICY_URL)
            val text = "$prefix <a href=\"$privacyPolicyUrl\">$privacyPolicy<a>"

            setHtmlWithLinks(tvPolicy, text)
        }
        return binding?.root
    }

    private fun setHtmlWithLinks(textView: TextView, sourceText: String) {
        val htmlText = Html.fromHtml(sourceText, Html.FROM_HTML_MODE_LEGACY) as Spannable
        textView.setLinkTextColor(ContextCompat.getColor(textView.context, CoreR.color.white))
        textView.movementMethod = LinkMovementMethod.getInstance()

        for (u in htmlText.getSpans(0, htmlText.length, URLSpan::class.java)) {
            htmlText.setSpan(
                object : UnderlineSpan() {
                    override fun updateDrawState(tp: TextPaint) {
                        tp.isUnderlineText = false
                    }
                },
                htmlText.getSpanStart(u),
                htmlText.getSpanEnd(u),
                0
            )
        }
        textView.text = htmlText
    }

    override fun onStart() {
        super.onStart()
        binding?.clGoogle?.setOnThrottleClickListener(startedCompositeDisposable) {
            viewModel.signInGoogle()
        }
    }
}