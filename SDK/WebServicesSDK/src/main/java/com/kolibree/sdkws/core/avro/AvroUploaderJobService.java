/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core.avro;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.PersistableBundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.annotation.VisibleForApp;
import com.kolibree.android.commons.JobServiceIdConstants;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.sdkws.core.IKolibreeConnector;
import dagger.android.AndroidInjection;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import javax.inject.Inject;
import timber.log.Timber;

/** Created by miguelaragues on 6/3/18. */
@VisibleForApp
@SuppressLint("SpecifyJobSchedulerIdRange")
public class AvroUploaderJobService extends JobService {

  private static final String EXTRA_FILE_NAME = "extra_file_name";

  @VisibleForTesting Boolean paramsContainValidFile = null;
  @Inject AvroFileUploader avroFileUploader;
  @Inject IKolibreeConnector connector;
  @VisibleForTesting Disposable uploadFileDisposable;

  static JobInfo uploaderJobInfo(@NonNull Context context, @NonNull File file) {
    int jobId = file.hashCode();
    FailEarly.failInConditionMet(
        JobServiceIdConstants.contains(jobId),
        "ID is within the range of the JobService that we use");

    JobInfo.Builder builder =
        new JobInfo.Builder(jobId, new ComponentName(context, AvroUploaderJobService.class))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true);

    builder.setExtras(AvroUploaderJobService.createExtras(file));

    return builder.build();
  }

  private static PersistableBundle createExtras(@NonNull File file) {
    PersistableBundle bundle = new PersistableBundle();

    bundle.putString(EXTRA_FILE_NAME, file.getAbsolutePath());

    return bundle;
  }

  @Override
  public void onCreate() {
    AndroidInjection.inject(this);
    super.onCreate();
  }

  @Override
  public boolean onStartJob(JobParameters params) {
    if (!isUserLoggedIn()) {
      jobCompletedAndDoesNotNeedReschedule(params);

      avroFileUploader.deletePendingFiles();

      return false;
    }

    paramsContainValidFile = startUploadingAvroFile(params);

    return paramsContainValidFile;
  }

  @VisibleForTesting
  boolean isUserLoggedIn() {
    return connector.hasConnectedAccount();
  }

  /**
   * Starts uploading the avro file if it's still in the file system. Otherwise, it returns false
   *
   * @return true if the file path specified in the params is still valid, false otherwise
   */
  @VisibleForTesting
  boolean startUploadingAvroFile(JobParameters params) {
    final File validatedFile = extractAndValidateFile(params);

    if (validatedFile == null) {
      jobCompletedAndDoesNotNeedReschedule(params);

      return false;
    }

    uploadFileDisposable =
        avroFileUploader
            .uploadFileAndDeleteOnSuccess(validatedFile)
            .subscribeOn(Schedulers.io())
            .subscribe(
                () -> jobCompletedAndDoesNotNeedReschedule(params),
                t -> {
                  Timber.e(t);

                  jobFinished(params, true);
                });

    return true;
  }

  @VisibleForTesting
  void jobCompletedAndDoesNotNeedReschedule(JobParameters params) {
    jobFinished(params, false);
  }

  @Nullable
  @VisibleForTesting
  File extractAndValidateFile(JobParameters params) {
    String filePath = params.getExtras().getString(EXTRA_FILE_NAME);
    if (filePath == null) {
      return null;
    }

    File file = new File(filePath);

    if (!file.exists()) {
      return null;
    }

    return file;
  }

  /**
   * Tells the system if this job should be rescheduled or not.
   *
   * <p>If the file the parameters point doesn't exist, we don't want to try again
   *
   * @return true if the avro file exists and we want this job to be executed in the future, false
   *     otherwise
   */
  @Override
  public boolean onStopJob(JobParameters params) {
    if (uploadFileDisposable != null && !uploadFileDisposable.isDisposed()) {
      uploadFileDisposable.dispose();
    }

    return paramsContainValidFile == null ? true : paramsContainValidFile;
  }
}
