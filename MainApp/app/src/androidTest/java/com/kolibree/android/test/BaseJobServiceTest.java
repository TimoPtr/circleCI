/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test;

import static com.kolibree.android.app.BaseKolibreeApplication.appComponent;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import com.kolibree.android.app.dagger.EspressoAppComponent;
import com.kolibree.android.test.dagger.EspressoDaggerInitializer;
import java.util.List;
import org.junit.Before;
import org.junit.runner.RunWith;

/** Created by miguelaragues on 14/3/18. */
@RunWith(AndroidJUnit4.class)
@TargetApi(VERSION_CODES.LOLLIPOP)
public abstract class BaseJobServiceTest {

  @Before
  public void setUp() {
    EspressoDaggerInitializer.initialize(context());
  }

  protected final Context context() {
    return InstrumentationRegistry.getInstrumentation().getTargetContext();
  }

  protected final EspressoAppComponent component() {
    return (EspressoAppComponent) appComponent;
  }

  protected final void launchJobAndWait(JobInfo jobInfo) throws InterruptedException {
    JobScheduler scheduler =
        (JobScheduler) context().getSystemService(Context.JOB_SCHEDULER_SERVICE);

    scheduler.schedule(jobInfo);

    while (jobPending(scheduler, jobInfo)) {
      Thread.sleep(50);
    }
  }

  private boolean jobPending(JobScheduler scheduler, JobInfo jobInfo) {
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      return scheduler.getPendingJob(jobInfo.getId()) != null;
    }

    List<JobInfo> scheduledJobs = scheduler.getAllPendingJobs();
    for (int i = 0, size = scheduledJobs.size(); i < size; i++) {
      if (scheduledJobs.get(i).getId() == jobInfo.getId()) {
        return true;
      }
    }

    return false;
  }
}
