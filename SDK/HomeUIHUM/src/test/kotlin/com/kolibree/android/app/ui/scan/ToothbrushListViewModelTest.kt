/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.scan

import com.kolibree.android.app.test.BaseUnitTest

internal class ToothbrushListViewModelTest : BaseUnitTest() {

    private lateinit var viewModel: ToothbrushListViewModel

    override fun setup() {
        super.setup()

        viewModel = ToothbrushListViewModel(null)
    }
}
