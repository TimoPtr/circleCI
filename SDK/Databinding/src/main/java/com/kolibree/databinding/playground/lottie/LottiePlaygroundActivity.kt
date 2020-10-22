/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.databinding.playground.lottie

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.dagger.scopes.ActivityScope
import com.kolibree.databinding.R
import com.kolibree.databinding.databinding.ActivityLottiePlaygroundBinding
import dagger.Module
import dagger.android.ContributesAndroidInjector

internal class LottiePlaygroundActivity :
    BaseMVIActivity<
        LottiePlaygroundViewState,
        LottiePlaygroundActions,
        LottiePlaygroundViewModel.Factory,
        LottiePlaygroundViewModel,
        ActivityLottiePlaygroundBinding>() {

    override fun getViewModelClass(): Class<LottiePlaygroundViewModel> = LottiePlaygroundViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_lottie_playground

    override fun execute(action: LottiePlaygroundActions) {
        // no-op
    }
}

@Keep
@Module
abstract class LottiePlaygroundBindingModule {

    @ActivityScope
    @ContributesAndroidInjector
    internal abstract fun bindLottiePlaygroundActivity(): LottiePlaygroundActivity
}

@Keep
fun startLottiePlaygroundIntent(context: Context) {
    context.startActivity(Intent(context, LottiePlaygroundActivity::class.java))
}
