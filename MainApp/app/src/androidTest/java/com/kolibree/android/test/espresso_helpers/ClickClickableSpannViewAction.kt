package com.kolibree.android.test.espresso_helpers

import android.text.SpannableString
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.UiController
import androidx.test.espresso.ViewAction
import org.hamcrest.Matcher
import org.hamcrest.Matchers

// https://stackoverflow.com/questions/38314077/how-to-click-a-clickablespan-using-espresso

fun clickClickableSpan(textToClick: CharSequence): ViewAction {
    return object : ViewAction {
        override fun getDescription() = "clicking on a ClickableSpan"

        override fun getConstraints(): Matcher<View> {
            return Matchers.instanceOf(TextView::class.java)
        }

        override fun perform(uiController: UiController, view: View) {
            val textView = view as TextView
            val spannableString = textView.text as SpannableString

            if (spannableString.length == 0) {
                // TextView is empty, nothing to do
                throw NoMatchingViewException.Builder()
                    .includeViewHierarchy(true)
                    .withRootView(textView)
                    .build()
            }

            // Get the links inside the TextView and check if we find textToClick
            val spans = spannableString.getSpans(0, spannableString.length, ClickableSpan::class.java)
            if (spans.size > 0) {
                var spanCandidate: ClickableSpan
                for (span in spans) {
                    spanCandidate = span
                    val start = spannableString.getSpanStart(spanCandidate)
                    val end = spannableString.getSpanEnd(spanCandidate)
                    val sequence = spannableString.subSequence(start, end)
                    if (textToClick.toString() == sequence.toString()) {
                        span.onClick(textView)
                        return
                    }
                }
            }

            // textToClick not found in TextView
            throw NoMatchingViewException.Builder()
                .includeViewHierarchy(true)
                .withRootView(textView)
                .build()
        }
    }
}
