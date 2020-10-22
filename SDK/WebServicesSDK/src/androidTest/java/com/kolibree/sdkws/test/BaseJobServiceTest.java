/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.test;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import java.util.List;

/** Created by miguelaragues on 14/3/18. */
@TargetApi(VERSION_CODES.LOLLIPOP)
public abstract class BaseJobServiceTest extends BaseDaggerInstrumentationTest {

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
