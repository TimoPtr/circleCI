/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.disconnection

import com.kolibree.android.sdk.disconnection.LostConnectionHandlerModule
import dagger.Module

@Module(includes = [LostConnectionHandlerModule::class])
abstract class LostConnectionModule
