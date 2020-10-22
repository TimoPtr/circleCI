/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import com.jakewharton.rxrelay2.BehaviorRelay;
import com.kolibree.android.network.utils.NetworkChecker;
import io.reactivex.Observable;
import javax.inject.Inject;

/** Created by miguelaragues on 22/12/17. */
class NetworkCheckerImpl implements NetworkChecker {

  private final Context context;
  private final BehaviorRelay<Boolean> connectivityRelay = BehaviorRelay.create();
  private NetworkInfo networkInfo;
  private final BroadcastReceiver connectivityStateReceiver =
      new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
          refreshNetworkInfo();

          connectivityRelay.accept(isConnected());
        }
      };
  private volatile Observable<Boolean> connectivityStateObservable;

  @Inject
  NetworkCheckerImpl(Context context) {
    this.context = context.getApplicationContext();

    refreshNetworkInfo();
  }

  private void refreshNetworkInfo() {
    ConnectivityManager connectivityManager =
        (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

    if (connectivityManager != null) {
      this.networkInfo = connectivityManager.getActiveNetworkInfo();
    } else {
      networkInfo = null;
    }
  }

  /**
   * Indicates whether network connectivity exists and it is possible to establish connections and
   * pass data.
   *
   * <p>Always call this before attempting to perform data transactions.
   *
   * @return {@code true} if network connectivity exists, {@code false} otherwise.
   */
  private boolean isConnected() {
    return networkInfo != null && networkInfo.isConnected();
  }

  @Override
  public boolean hasConnectivity() {
    refreshNetworkInfo();

    return isConnected();
  }

  @Override
  public Observable<Boolean> connectivityStateObservable() {
    if (connectivityStateObservable == null) {
      synchronized (this) {
        if (connectivityStateObservable == null) {
          refreshNetworkInfo();

          connectivityStateObservable =
              connectivityRelay
                  .startWith(isConnected())
                  .distinctUntilChanged()
                  .doOnSubscribe(disposable -> registerConnectivityStateReceiver())
                  .doFinally(this::onAllConnectivityStateObserversUnsubscribed)
                  .share();
        }
      }
    }

    return connectivityStateObservable;
  }

  private void onAllConnectivityStateObserversUnsubscribed() {
    connectivityStateObservable = null;

    unregisterConnectivityStateReceiver();
  }

  private void unregisterConnectivityStateReceiver() {
    context.unregisterReceiver(connectivityStateReceiver);
  }

  /**
   * Android 8 and above prevents most implicit BroadcastReceivers, so we need to register
   * explicitly
   */
  private void registerConnectivityStateReceiver() {
    context.registerReceiver(
        connectivityStateReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
  }
}
