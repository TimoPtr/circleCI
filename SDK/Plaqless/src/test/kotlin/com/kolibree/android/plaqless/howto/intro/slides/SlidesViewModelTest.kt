/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.plaqless.howto.intro.slides

import com.kolibree.android.app.test.BaseUnitTest

internal class SlidesViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: SlidesViewModel

    override fun setup() {
        super.setup()

        viewModel = SlidesViewModel(null)
    }
}
