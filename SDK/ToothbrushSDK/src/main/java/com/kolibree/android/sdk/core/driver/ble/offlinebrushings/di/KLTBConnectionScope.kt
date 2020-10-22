/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.core.driver.ble.offlinebrushings.di

import com.kolibree.android.sdk.connection.KLTBConnection
import javax.inject.Scope

/**
 * Identifies a type that the injector only instantiates once per [KLTBConnection]
 */
@Scope
@Retention
internal annotation class KLTBConnectionScope
