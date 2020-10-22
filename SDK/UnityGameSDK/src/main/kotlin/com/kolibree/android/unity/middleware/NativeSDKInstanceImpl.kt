/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity.middleware

import com.kolibree.game.middleware.NativeSDKInstance
import com.kolibree.game.middleware.ResourcesInteractor
import com.kolibree.game.middleware.ToothbrushInteractor
import com.kolibree.game.middleware.WebServicesInteractor

internal class NativeSDKInstanceImpl(
    private val internalResourcesInteractor: ResourcesInteractor,
    private val internalToothbrushInteractor: ToothbrushInteractor,
    private val internalWebServicesInteractor: WebServicesInteractor
) : NativeSDKInstance() {

    override fun getResourcesInteractor(): ResourcesInteractor {
        return internalResourcesInteractor
    }

    override fun getToothbrushInteractor(): ToothbrushInteractor {
        return internalToothbrushInteractor
    }

    override fun getWebServicesInteractor(): WebServicesInteractor {
        return internalWebServicesInteractor
    }
}
