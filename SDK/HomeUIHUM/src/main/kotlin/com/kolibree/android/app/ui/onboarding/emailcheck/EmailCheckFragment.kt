/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.onboarding.emailcheck

import com.kolibree.android.app.base.EmptyBaseViewState
import com.kolibree.android.app.base.NoActions
import com.kolibree.android.app.ui.AnimatedBottomGroupFragment
import com.kolibree.android.app.widget.AnimatorGroup
import com.kolibree.android.homeui.hum.R
import com.kolibree.android.homeui.hum.databinding.FragmentEmailCheckBinding
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class EmailCheckFragment : AnimatedBottomGroupFragment<
    EmptyBaseViewState,
    NoActions,
    EmailCheckViewModel.Factory,
    EmailCheckViewModel,
    FragmentEmailCheckBinding
    >(), TrackableScreen {

    override fun getViewModelClass(): Class<EmailCheckViewModel> = EmailCheckViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_email_check

    override fun getScreenName(): AnalyticsEvent = EmailCheckAnalytics.main()

    override fun animatedBottomGroup(): AnimatorGroup? = null

    override fun execute(action: NoActions) {
        // no-op
    }
}
