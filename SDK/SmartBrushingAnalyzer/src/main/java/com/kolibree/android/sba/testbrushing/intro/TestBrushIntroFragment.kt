package com.kolibree.android.sba.testbrushing.intro

import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.ContextCompat
import com.kolibree.android.extensions.setOnDebouncedClickListener
import com.kolibree.android.sba.R
import com.kolibree.android.sba.databinding.FragmentTestBrushIntroBinding
import com.kolibree.android.sba.testbrushing.base.ViewAction
import com.kolibree.android.sba.testbrushing.base.mvi.BaseTestBrushingFragment
import com.kolibree.android.sba.testbrushing.tracker.TestBrushingEventTracker
import com.kolibree.android.tracker.Analytics

/**
 * Test Brushing introduction screen
 *
 * No logic associated to this screen, pure static
 */
internal class TestBrushIntroFragment : BaseTestBrushingFragment<
    TestBrushIntroViewState,
    TestBrushIntroViewModel,
    TestBrushIntroViewModel.Factory,
    FragmentTestBrushIntroBinding
    >() {

    private var isStartBrushingClickable = true

    override fun getViewModelClass() = TestBrushIntroViewModel::class.java

    override fun getLayoutId() = R.layout.fragment_test_brush_intro

    override fun execute(action: ViewAction) {
        // no-op
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.testBrushingIntroStart.setOnDebouncedClickListener {
            if (isStartBrushingClickable) {
                viewModel.userClickNext()
            }
            isStartBrushingClickable = false
        }

        colorTipText()

        initDoItLaterButton()

        Analytics.send(TestBrushingEventTracker.introScreen())
    }

    private fun initDoItLaterButton() {
        binding.testBrushingIntroDoLater.setOnDebouncedClickListener {
            viewModel.userClickDoLater()
        }
    }

    override fun onResume() {
        super.onResume()
        isStartBrushingClickable = true
    }

    private fun colorTipText() {
        context?.let {
            val tipText = SpannableStringBuilder(getString(R.string.test_brushing_tip))
            val textToColor = """40%"""

            val colorSpan = ForegroundColorSpan(ContextCompat.getColor(it, R.color.colorPrimary))
            val boldSpan = StyleSpan(Typeface.BOLD)

            val startIndex = tipText.indexOf(textToColor)

            if (startIndex >= 0) { // support for custom text through TranslationsSupport
                val endIndex = startIndex + textToColor.length

                tipText.setSpan(colorSpan, startIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE)
                tipText.setSpan(boldSpan, 0, tipText.length, SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            binding.testBrushingIntroTip.text = tipText
        }
    }
}
