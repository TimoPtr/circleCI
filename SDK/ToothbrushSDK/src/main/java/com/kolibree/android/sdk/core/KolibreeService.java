package com.kolibree.android.sdk.core;

import static com.kolibree.android.TimberTagKt.bluetoothTagFor;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Keep;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.app.NotificationCompat.Builder;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.sdk.KolibreeAndroidSdk;
import com.kolibree.android.sdk.R;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.scan.ToothbrushScanResult;
import com.kolibree.android.translationssupport.Translations;
import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Flowable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;
import timber.log.Timber;

/**
 * Created by aurelien on 07/01/2017.
 *
 * <p>Kolibree auto connection service
 */
@Keep
public class KolibreeService extends Service {

  private static final String TAG = bluetoothTagFor(KolibreeService.class);

  private static final String NOTIFICATION_CHANNEL_ID = "com.kolibree.serviceNotificationChannel";

  /**
   * package protected variable to limit who can bind to the service.
   *
   * <p>3rd party apps, please use ServiceProvider to bind to this service
   */
  static final String KOLIBREE_BINDING_EXTRA = "kolibree_binding_allowed";

  @Inject InternalKLTBConnectionPoolManager kltbConnectionPoolManager;

  @Inject Set<BackgroundJobManager> backgroundJobManagerSet;

  private Binder binder;

  private CountDownTimer stopServiceTimer;

  private static final int FOREGROUND_ID = 566; // random

  @VisibleForTesting final CompositeDisposable disposables = new CompositeDisposable();

  private boolean taskRemoved;

  private Handler mainThreadHandler = new Handler();

  private boolean serviceStartedAsForeground;

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(Translations.wrapContext(base));
  }

  @Override
  public void onCreate() {
    super.onCreate();

    KolibreeAndroidSdk.getSdkComponent().inject(this);

    Timber.tag(TAG).d("Kolibree service created ");

    init();
  }

  @VisibleForTesting
  void init() {
    for (BackgroundJobManager backgroundJobManager : backgroundJobManagerSet) {
      backgroundJobManager.cancelJob();
    }

    createBinder();

    createConnections();
  }

  @VisibleForTesting
  void createBinder() {
    binder = new KolibreeBinder();
  }

  @VisibleForTesting
  void createConnections() {
    DisposableUtils.addSafely(
        disposables,
        kltbConnectionPoolManager
            .init()
            .subscribeOn(Schedulers.io())
            .subscribe(this::refreshForegroundState, Timber::e));
  }

  @VisibleForTesting
  void refreshForegroundState() {
    runOnMainThread(
        () -> {
          if (getKnownConnections().isEmpty()) {
            stopServiceAsForeground();
          } else {
            startServiceAsForeground();
          }
        });
  }

  @VisibleForTesting
  void runOnMainThread(@NonNull Runnable runnable) {
    mainThreadHandler.post(runnable);
  }

  @MainThread
  @VisibleForTesting
  void stopServiceAsForeground() {
    stopForeground(true);

    serviceStartedAsForeground = false;
  }

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    return START_NOT_STICKY;
  }

  @MainThread
  @VisibleForTesting
  void startServiceAsForeground() {
    if (!serviceStartedAsForeground) {
      //noinspection deprecation Channel is set below
      final Builder notificationBuilder =
          new Builder(this)
              .setOngoing(true)
              .setContentTitle(getResources().getString(R.string.running_in_background))
              .setSmallIcon(R.drawable.foreground_service_icon);

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        createNotificationChannel();
        notificationBuilder.setChannelId(NOTIFICATION_CHANNEL_ID);
      }

      startForeground(FOREGROUND_ID, notificationBuilder.build());

      serviceStartedAsForeground = true;
    }
  }

  @Override
  public void onDestroy() {
    closeConnections();

    for (BackgroundJobManager backgroundJobManager : backgroundJobManagerSet) {
      backgroundJobManager.scheduleJob(this);
    }

    super.onDestroy();
  }

  @VisibleForTesting
  void closeConnections() {
    disposables.dispose();

    kltbConnectionPoolManager.close();
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    verifyClient(intent);

    Timber.tag(TAG).d("onBind");

    // now, start the service so that we can set it as a foreground service
    Intent serviceIntent = new Intent(this, KolibreeService.class);
    try { // Why try ? https://kolibree.atlassian.net/browse/KLTB002-7055
      startService(serviceIntent);
    } catch (IllegalStateException e) {
      FailEarly.fail(
          "Trying to start as a foreground service when the app is in background!"
              + "Original error is "
              + e.getMessage());
    }

    return binder;
  }

  @VisibleForTesting
  void verifyClient(Intent intent) throws IllegalAccessError {
    if (!intent.getBooleanExtra(KOLIBREE_BINDING_EXTRA, false)) {
      throw new IllegalAccessError("Use ServiceProvider to bind to this service");
    }
  }

  @Override
  public boolean onUnbind(Intent intent) {
    Timber.tag(TAG).d("onUnbind, task removed? %s", taskRemoved);
    if (taskRemoved) {
      stopKolibreeService();
    } else {
      startDestroyTimer();
    }

    return true;
  }

  @Override
  public void onRebind(Intent intent) {
    super.onRebind(intent);
    Timber.tag(TAG).d("onRebind");

    stopDestroyTimer();
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    super.onTaskRemoved(rootIntent);

    taskRemoved = true;

    stopSelf();
  }

  private static final int AUTO_DESTROY_SECONDS = 10;

  private synchronized void startDestroyTimer() {
    Timber.tag(TAG).d("startDestroyTimer");
    stopServiceTimer =
        new CountDownTimer(
            TimeUnit.SECONDS.toMillis(AUTO_DESTROY_SECONDS),
            TimeUnit.SECONDS.toMillis(AUTO_DESTROY_SECONDS)) {
          @Override
          public void onTick(long l) {}

          @Override
          public void onFinish() {
            Timber.tag(TAG)
                .i(
                    "KolibreeService has been "
                        + AUTO_DESTROY_SECONDS
                        + " seconds without bounded subscribers, stopping service");
            stopKolibreeService();
          }
        };

    stopServiceTimer.start();
  }

  public boolean isTerminating() {
    return stopServiceTimer != null;
  }

  private void stopKolibreeService() {
    runOnMainThread(this::stopServiceAsForeground);

    stopSelf();
  }

  private synchronized void stopDestroyTimer() {
    Timber.tag(TAG).d("stopDestroyTimer %s", stopServiceTimer);
    if (stopServiceTimer != null) {
      stopServiceTimer.cancel();
    }
  }

  /**
   * Create a new connection to a toothbrush.
   *
   * @param scanResult non null ToothbrushScanResult
   */
  @NonNull
  public KLTBConnection createAndEstablishConnection(
      @NonNull final ToothbrushScanResult scanResult) {
    return createAndEstablishConnection(
        scanResult.getMac(), scanResult.getModel(), scanResult.getName());
  }

  /**
   * Create a new connection to a toothbrush.
   *
   * @param mac mac addr of the toothbrush
   * @param model model of the toothbrush
   * @param name name of the toothbrush
   * @return created connection KLTBConnection
   */
  @NonNull
  public KLTBConnection createAndEstablishConnection(
      @NonNull String mac, ToothbrushModel model, String name) {
    return createAndEstablishConnection(mac, name, model);
  }

  /**
   * If there's already a KLBConnection for the <code>mac</code> address, return it.
   *
   * <p>Otherwise, attempt to establish a connection with the toothbrush with the associated mac
   *
   * @param mac non null MAC address
   * @param name non null name
   * @param model non null ToothbrushModel
   * @return non null KLTBConnectionImpl. Connection may not be active.
   */
  @NonNull
  private KLTBConnection createAndEstablishConnection(
      @NonNull String mac, @NonNull String name, @NonNull ToothbrushModel model) {
    InternalKLTBConnection connection =
        kltbConnectionPoolManager.createAndEstablish(mac, name, model);

    refreshForegroundState();

    return connection;
  }

  /**
   * Get a connection.
   *
   * @param mac non null String MAC address
   * @return non null KLTBConnection if it was previously created, false otherwise
   */
  @Nullable
  public KLTBConnection getConnection(@NonNull String mac) {
    return kltbConnectionPoolManager.get(mac);
  }

  /**
   * Forget a toothbrush (will no longer try to connect to it).
   *
   * @param mac toothbrush's MAC address
   */
  public void forget(@NonNull String mac) {
    refreshForegroundStateOnSuccessfulForget(kltbConnectionPoolManager.forget(mac));
  }

  /**
   * Forget a toothbrush (will no longer try to connect to it).
   *
   * @param connection non null KLTBConnection
   */
  public void forget(@NonNull final KLTBConnection connection) {
    refreshForegroundStateOnSuccessfulForget(kltbConnectionPoolManager.forget(connection));
  }

  public Completable forgetCompletable(@NonNull String mac) {
    return kltbConnectionPoolManager
        .forget(mac)
        .onErrorResumeNext(onForgetToothbrushErrorFunction())
        .doOnComplete(this::refreshForegroundState);
  }

  private void refreshForegroundStateOnSuccessfulForget(@NonNull Completable forgetCompletable) {
    DisposableUtils.addSafely(
        disposables,
        forgetCompletable
            .subscribeOn(Schedulers.computation())
            .onErrorResumeNext(onForgetToothbrushErrorFunction())
            .subscribe(this::refreshForegroundState, Timber::e));
  }

  @NotNull
  private Function<Throwable, CompletableSource> onForgetToothbrushErrorFunction() {
    return throwable -> {
      if (throwable instanceof UnknownToothbrushException) {
        return Completable.complete();
      }

      return Completable.error(throwable);
    };
  }

  /**
   * Get all handled connections.
   *
   * @return non null KLTBConnection list
   */
  @NonNull
  public List<KLTBConnection> getKnownConnections() {
    return kltbConnectionPoolManager.getKnownConnections();
  }

  /**
   * @return Flowable that will emit List of InternalKLTBConnection with the known connections each
   *     time a connection is added or removed from the pool. The list can be empty.
   *     <p>It won't emit a new list on connection state change.
   *     <p>It won't complete unless there's an error, even if this instance is closed.
   */
  @NonNull
  public Flowable<? extends List<KLTBConnection>> getKnownConnectionsOnceAndStream() {
    return kltbConnectionPoolManager.getKnownConnectionsOnceAndStream();
  }

  /**
   * Create and setup a NotificationChannel for the KolibreeService notification
   *
   * <p>Mandatory for Android >= 8.1
   */
  @TargetApi(Build.VERSION_CODES.O)
  private void createNotificationChannel() {
    final NotificationChannel notificationChannel =
        new NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getApplicationContext().getString(R.string.push_notification_channel_kolibree_service),
            NotificationManager.IMPORTANCE_LOW); // Low = don't vibrate
    notificationChannel.enableLights(true);
    notificationChannel.setLightColor(Color.RED);
    notificationChannel.setShowBadge(true);
    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);

    final NotificationManager manager =
        (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

    if (manager != null) {
      manager.createNotificationChannel(notificationChannel);
    }
  }

  public final class KolibreeBinder extends Binder {

    @NonNull
    public KolibreeService getService() {
      return KolibreeService.this;
    }
  }
}
