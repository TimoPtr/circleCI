package com.kolibree.sdkws.core;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.network.api.ApiError;

/** Created by aurelien on 29/09/15. */
@Keep
public abstract class KolibreeConnectorListener<T> {
  private boolean canceled;

  /** Call success() on UI thread */
  final void notifySuccess(final T data) {
    if (!canceled) {
      new Handler(Looper.getMainLooper())
          .post(
              new Runnable() {
                @Override
                public void run() {
                  onSuccess(data);
                }
              });
    }
  }

  /**
   * Call onError() on UI thread
   *
   * @param error
   */
  final void notifyError(@NonNull final ApiError error) {
    if (!canceled) {
      new Handler(Looper.getMainLooper())
          .post(
              new Runnable() {
                @Override
                public void run() {
                  onError(error);
                }
              });
    }
  }

  /** Cancel response */
  public final void cancel() {
    canceled = true;
  }

  /** Operation terminated successfully */
  public abstract void onSuccess(T data);

  /**
   * An error occurred
   *
   * @param error a detailed error message
   */
  public abstract void onError(@NonNull ApiError error);
}
