package com.kolibree.android.sdk.error;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

/**
 * Created by aurelien on 25/08/16.
 *
 * <p>Kolibree toothbrush SDK error
 */
@Keep
public class FailureReason extends Exception {

  public FailureReason(@NonNull String message, @NonNull Throwable throwable) {
    super(message, throwable);
  }

  public FailureReason(@NonNull String message) {
    super(message);
  }

  public FailureReason(@NonNull Throwable e) {
    super("Critical error", e);
  }
}
