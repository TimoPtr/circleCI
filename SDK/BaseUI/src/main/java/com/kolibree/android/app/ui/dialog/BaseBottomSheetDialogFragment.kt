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
import androidx.annotation.VisibleForTesting
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.auditor.Auditor
import com.kolibree.android.utils.DisposableScope
import io.reactivex.disposables.Disposable
import timber.log.Timber

/**
 * Created as base class for all BottomSheetDialogFragments
 */
@VisibleForApp
abstract class BaseBottomSheetDialogFragment : BottomSheetDialogFragment() {

    /**
     * Disposable scope that is available after onResume has been called.
     *
     * Cleared in [onPause] method.
     */
    @VisibleForTesting
    val onPauseDisposables = DisposableScope("onPause")

    override fun onCreate(savedInstanceState: Bundle?) {
        Timber.tag(TAG_LIFECYCLE).v("%s - onCreate", javaClass.simpleName)
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onStart", javaClass.simpleName)
        Auditor.instance().notifyFragmentStarted(this, activity)
        super.onStart()
    }

    override fun onResume() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onResume", javaClass.simpleName)
        Auditor.instance().notifyFragmentResumed(this, activity)
        super.onResume()
        onPauseDisposables.ready()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Auditor.instance().notifyFragmentViewCreated(view, this, activity)
    }

    override fun onPause() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onPause", javaClass.simpleName)
        Auditor.instance().notifyFragmentPaused(this, activity)
        super.onPause()
        onPauseDisposables.clear()
    }

    override fun onStop() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onStop", javaClass.simpleName)
        Auditor.instance().notifyFragmentStopped(this, activity)
        super.onStop()
    }

    override fun onDestroy() {
        Timber.tag(TAG_LIFECYCLE).v("%s - onDestroy", javaClass.simpleName)
        super.onDestroy()
    }

    /**
     * Add a disposable to [onPauseDisposables] scope.
     * You should use this method so that the subscription will be dispose in [onPause]
     */
    @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
    fun disposeOnPause(block: () -> Disposable?) {
        onPauseDisposables += block.invoke()
    }
}

private const val TAG_LIFECYCLE = "SheetDialogFragment"
