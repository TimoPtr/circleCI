/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.async;

import static com.kolibree.android.commons.JobServiceIdConstants.APP_CLEAR_USER_CONTENT;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import com.kolibree.account.eraser.ClearUserContentJobService;
import com.kolibree.android.app.ui.kolibree_pro.KolibreeProReminders;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

/**
 * Invoked when we want to clear user local stored content, it's responsible for truncating all
 * tables and deleting content such as shared preferences
 *
 * <p>Created by miguelaragues on 2/10/17.
 */
@SuppressLint("SpecifyJobSchedulerIdRange")
@RequiresApi(api = VERSION_CODES.LOLLIPOP)
public class AppClearUserContentJobService extends ClearUserContentJobService {
  private static final long MAX_SECONDS_DELAY = 3;

  @Inject KolibreeProReminders kolibreeProReminders;

  public static JobInfo.Builder createBuilder(@NonNull Context context) {
    JobInfo.Builder builder =
        new JobInfo.Builder(
                APP_CLEAR_USER_CONTENT,
                new ComponentName(context, AppClearUserContentJobService.class))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);

    builder.setOverrideDeadline(TimeUnit.SECONDS.toMillis(MAX_SECONDS_DELAY));

    return builder;
  }

  @CallSuper
  protected void clearPreferences() {
    super.clearPreferences();

    kolibreeProReminders.stopAllPeriodicReminders();
  }
}
