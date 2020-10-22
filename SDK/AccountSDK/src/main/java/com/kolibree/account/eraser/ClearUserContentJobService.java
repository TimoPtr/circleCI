/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.account.eraser;

import static com.kolibree.android.commons.JobServiceIdConstants.CLEAR_USER_CONTENT;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.CallSuper;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.commons.interfaces.Truncable;
import com.kolibree.android.commons.interfaces.UserLogoutHook;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.sdkws.core.avro.AvroFileUploader;
import dagger.android.AndroidInjection;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import java.io.File;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Invoked when we want to clear user local stored content, it's responsible for truncating all
 * tables and deleting content such as shared preferences
 *
 * <p>Created by miguelaragues on 2/10/17.
 */
@RequiresApi(api = VERSION_CODES.LOLLIPOP)
@SuppressLint("SpecifyJobSchedulerIdRange")
@Keep
public class ClearUserContentJobService extends JobService {
  private static final String PROTECTED_PACKAGE_NAME = "cn.colgate.colgateconnect";
  final CompositeDisposable disposables = new CompositeDisposable();

  @VisibleForTesting final AtomicInteger observableCounter = new AtomicInteger(0);

  @Inject Set<Truncable> truncables;

  @Inject Set<UserLogoutHook> userLogoutHooks;

  @Inject ServiceProvider serviceProvider;

  @Inject AvroFileUploader avroFileUploader;

  @VisibleForTesting JobParameters parameters;

  boolean observableHadError = false;

  public static JobInfo.Builder createBuilder(@NonNull Context context) {
    JobInfo.Builder builder =
        new JobInfo.Builder(
                CLEAR_USER_CONTENT, new ComponentName(context, ClearUserContentJobService.class))
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE);

    builder.setOverrideDeadline(3000);

    return builder;
  }

  @Override
  public void onCreate() {
    AndroidInjection.inject(this);
    super.onCreate();
  }

  @Override
  public boolean onStartJob(JobParameters jobParameters) {
    this.parameters = jobParameters;

    truncateDatabase();

    clearPreferences();

    forgetToothbrushes();

    clearAvroFiles();

    runUserLogoutHook();

    return true;
  }

  @VisibleForTesting
  void runUserLogoutHook() {
    runCompletable(
        Observable.fromIterable(userLogoutHooks)
            .flatMapCompletable(UserLogoutHook::getLogoutHookCompletable, true));
  }

  @VisibleForTesting
  void clearAvroFiles() {
    runCompletable(Completable.fromAction(() -> avroFileUploader.deletePendingFiles()));
  }

  @VisibleForTesting
  void forgetToothbrushes() {
    DisposableUtils.addSafely(
        disposables,
        serviceProvider
            .connectOnce()
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(ignore -> onObservableStarted())
            .doFinally(this::onObservableCompleted)
            .doOnError(this::onObservableError)
            .subscribe(this::onServiceConnected, Throwable::printStackTrace));
  }

  @VisibleForTesting
  void onServiceConnected(KolibreeService service) {
    for (KLTBConnection connection : service.getKnownConnections()) {
      service.forget(connection);
    }
  }

  @CallSuper
  protected void clearPreferences() {
    Context context = getApplicationContext();
    if (context.getPackageName().equals(PROTECTED_PACKAGE_NAME)) {
      return;
    }

    File dir = new File(context.getFilesDir().getParent() + "/shared_prefs/");
    String[] children = dir.list();
    for (int i = 0; i < children.length; i++) {
      // clear each of the prefrances
      String preferencesName = children[i].replace(".xml", "");
      if (shouldDeletePreferencesFile(preferencesName)) {
        context.getSharedPreferences(preferencesName, Context.MODE_PRIVATE).edit().clear().commit();
      }
    }
  }

  private boolean shouldDeletePreferencesFile(String preferencesName) {
    return !preferencesName.contains("secret_");
  }

  @Override
  public boolean onStopJob(JobParameters jobParameters) {
    disposables.dispose();

    return true;
  }

  @VisibleForTesting
  void truncateDatabase() {
    Completable[] truncablesArray = new Completable[truncables.size()];
    int index = 0;
    for (Truncable truncable : truncables) {
      truncablesArray[index++] = truncable.truncate();
    }

    runCompletable(Completable.mergeArrayDelayError(truncablesArray));
  }

  @VisibleForTesting
  void runCompletable(Completable completable) {
    DisposableUtils.addSafely(
        disposables,
        completable
            .subscribeOn(Schedulers.io())
            .doOnSubscribe(ignore -> onObservableStarted())
            .doFinally(this::onObservableCompleted)
            .doOnError(this::onObservableError)
            .subscribe(() -> {}, Timber::e));
  }

  @VisibleForTesting
  void onObservableError(Throwable t) {
    t.printStackTrace();

    observableHadError = true;
  }

  @VisibleForTesting
  void onObservableStarted() {
    observableCounter.incrementAndGet();
  }

  @VisibleForTesting
  void onObservableCompleted() {
    if (observableCounter.decrementAndGet() == 0) {
      jobFinished(parameters, observableHadError);
    }
  }
}
