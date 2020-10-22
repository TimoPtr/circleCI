/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto

import android.app.Activity
import android.os.Bundle
import androidx.annotation.Keep
import com.kolibree.android.app.base.BaseMVIActivity
import com.kolibree.android.app.ui.fragment.BaseFragment
import com.kolibree.android.plaqless.R
import com.kolibree.android.plaqless.databinding.ActivityPlaqlessHowToBinding
import com.kolibree.android.plaqless.howto.intro.PlaqlessHowToNavigator
import com.kolibree.android.plaqless.howto.intro.PlaqlessIntroFragment
import com.kolibree.android.plaqless.howto.intro.slides.SlidesFragment

/**
 * Once this activity is closed the return code will be ok if the user should land into TestBrushing
 */
@Keep
class PlaqlessHowToActivity :
    BaseMVIActivity<PlaqlessHowToViewState,
        PlaqlessHowToAction,
        PlaqlessHowToViewModel.Factory,
        PlaqlessHowToViewModel,
        ActivityPlaqlessHowToBinding>(),
    PlaqlessHowToNavigator {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, PlaqlessIntroFragment.newInstance())
                .commit()
        }
    }

    override fun getViewModelClass(): Class<PlaqlessHowToViewModel> = PlaqlessHowToViewModel::class.java

    override fun getLayoutId(): Int = R.layout.activity_plaqless_how_to

    override fun execute(action: PlaqlessHowToAction) {
        // no-op
    }

    override fun androidInjector() = fragmentInjector

    override fun navigateToSlides() = nextFragment(SlidesFragment.newInstance())

    override fun navigateToTestBrushing() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    private fun nextFragment(fragment: BaseFragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
