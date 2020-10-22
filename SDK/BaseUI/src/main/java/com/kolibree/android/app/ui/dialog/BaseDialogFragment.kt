/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 *
 */

package com.kolibree.android.app.ui.dialog

import android.os.Bundle
import android.view.View
import androidx.annotation.Keep
import androidx.fragment.app.DialogFragment
import com.kolibree.android.auditor.Auditor

/**
 * Created as base class for all DialogFragments, mainly to centralize tracking
 */
@Keep
abstract class BaseDialogFragment : DialogFragment() {
    /*
    Audit callbacks
     */
    override fun onResume() {
        Auditor.instance().notifyFragmentResumed(this, activity)
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Auditor.instance().notifyFragmentViewCreated(view, this, activity)
    }

    override fun onStart() {
        Auditor.instance().notifyFragmentStarted(this, activity)
        super.onStart()
    }

    override fun onPause() {
        Auditor.instance().notifyFragmentPaused(this, activity)
        super.onPause()
    }

    override fun onStop() {
        Auditor.instance().notifyFragmentStopped(this, activity)
        super.onStop()
    }
}
