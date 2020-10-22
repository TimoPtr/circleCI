/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.launcher

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import com.google.android.play.core.install.model.ActivityResult.RESULT_IN_APP_UPDATE_FAILED
import com.kolibree.android.app.ui.activity.BaseDaggerActivity
import com.kolibree.android.extensions.forceDispose
import com.kolibree.android.tracker.TrackableScreen
import io.reactivex.disposables.Disposable
import javax.inject.Inject
import timber.log.Timber

internal class LauncherActivity : BaseDaggerActivity(), TrackableScreen {

    @Inject
    internal lateinit var viewModelFactory: LauncherViewModel.Factory

    private lateinit var viewModel: LauncherViewModel

    private var actionDisposable: Disposable? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(viewModelStore, viewModelFactory)
            .get(LauncherViewModel::class.java)
        lifecycle.addObserver(viewModel)
    }

    override fun onResume() {
        super.onResume()
        actionDisposable = viewModel.actionsObservable.subscribe(::execute, Timber::e)
    }

    private fun execute(action: LauncherActions) = when (action) {
        is LauncherActions.OnUpdateNeeded -> action.request.start(this)
    }

    override fun onPause() {
        actionDisposable.forceDispose()
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.w("request code $requestCode and result $resultCode")
        if (resultCode == RESULT_IN_APP_UPDATE_FAILED) {
            Timber.e("In App update failed")
        }
    }

    override fun getScreenName() =
        LauncherAnalytics.main()
}
