package com.kolibree.android.questionoftheday.ui

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object QuestionOfTheDayAnalytics {
    fun main() = AnalyticsEvent(name = "DailyQuestion")
    fun accept() = send(main() + "Accept")
    fun close() = send(main() + "Close")
    fun submit() = send(main() + "Submit")
    fun ok() = send(main() + "Ok")
    fun collect() = send(main() + "Collect")
}
