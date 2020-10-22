package com.kolibree.android.sdk.core.notification;

import androidx.annotation.NonNull;

/**
 * Created by aurelien on 09/08/17.
 *
 * <p>Base interface for data or event listener notification
 *
 * @param <LT> Listener type
 */
public interface ListenerNotifier<LT> {

  /**
   * Notify a listener
   *
   * <p>Don't run long operations in this method to avoid bottlenecks performance issues
   *
   * @param listener non null listener to be notified
   */
  void notifyListener(@NonNull LT listener);
}
