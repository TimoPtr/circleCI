/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */
package com.kolibree.android.app.dagger

import dagger.Module
import dagger.Provides
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.Executors

/**
 * Provides the scheduler to be past to RXjava timeout operators
 *
 * If we don't replace this scheduler in Espresso tests, the main thread is blocked and the tests
 * don't run
 *
 * Created by miguelaragues on 26/2/18.
 */
@Module
object SingleThreadSchedulerModule {

    @Provides
    @SingleThreadScheduler
    fun scheduler(): Scheduler = Schedulers.from(Executors.newSingleThreadExecutor())
}
