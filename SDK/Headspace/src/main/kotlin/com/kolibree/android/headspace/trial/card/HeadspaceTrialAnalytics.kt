/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.headspace.trial.card

import com.kolibree.android.tracker.Analytics.send
import com.kolibree.android.tracker.AnalyticsEvent

internal object HeadspaceTrialAnalytics {
    private fun main() = AnalyticsEvent(name = "Headspace")

    fun showDescription() = send(main() + "ShowMore")
    fun hideDescription() = send(main() + "ShowLess")

    fun unlock() = send(main() + "UnlockCode")

    fun copyCode() = send(main() + "CopyCode")
    fun visitHeadspace() = send(main() + "VisitHeadSpace")
    fun quit() = send(main() + "Quit")
    fun confirmQuit() = send(main() + "RemoveCode")
    fun dismissQuit() = send(main() + "KeepCode")
}
