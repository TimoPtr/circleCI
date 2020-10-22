/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.job

import com.kolibree.android.worker.AppStartupWorkerConfigurator

internal typealias WorkerConfigurations = Set<@JvmSuppressWildcards AppStartupWorkerConfigurator>

internal fun WorkerConfigurations.configure() =
    this.forEach { it.configure() }
