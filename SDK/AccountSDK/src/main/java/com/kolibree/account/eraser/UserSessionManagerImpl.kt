package com.kolibree.account.eraser

import android.app.job.JobInfo
import android.app.job.JobScheduler
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.sdkws.core.SynchronizationScheduler
import javax.inject.Inject
import javax.inject.Provider

internal class UserSessionManagerImpl
@Inject constructor(
    private val jobScheduler: JobScheduler,
    private val clearUserContentJobInfo: Provider<JobInfo>,
    private val currentProfileProvider: CurrentProfileProvider,
    private val synchronizationScheduler: SynchronizationScheduler
) : UserSessionManager {

    override fun reset() {
        synchronizationScheduler.cancelAll()

        jobScheduler.schedule(clearUserContentJobInfo.get())

        currentProfileProvider.reset()
    }
}
