/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.badges

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.kolibree.R
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.databinding.ActivityBadgesPlaygroundBinding

internal class BadgesPlaygroundActivity :
    BaseMVIActivity<
        BadgesPlaygroundActivityViewState,
        BadgesPlaygroundActivityActions,
        BadgesPlaygroundActivityViewModel.Factory,
        BadgesPlaygroundActivityViewModel,
        ActivityBadgesPlaygroundBinding
        >() {

    override fun onCreate(savedInstanceState: Bundle?) {
        setFullScreenDecorView()
        super.onCreate(savedInstanceState)
    }

    override fun getViewModelClass(): Class<BadgesPlaygroundActivityViewModel> =
        BadgesPlaygroundActivityViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_badges_playground

    override fun execute(action: BadgesPlaygroundActivityActions) {
        // NO-OP
    }
}

fun startBadgesPlaygroundActivity(context: Context) =
    context.startActivity(Intent(context, BadgesPlaygroundActivity::class.java))
