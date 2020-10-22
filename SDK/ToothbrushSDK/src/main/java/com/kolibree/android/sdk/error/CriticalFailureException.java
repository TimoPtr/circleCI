package com.kolibree.android.sdk.error;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.kolibree.android.failearly.FailEarly;

/**
 * Created by lookashc on 30/01/19.
 *
 * <p>Kolibree toothbrush SDK critical error - causes instant crash in dev builds. Throw it to
 * indicate that app is in the state in which you doesn't expect it to be.
 */
@Keep
@SuppressWarnings("all")
public class CriticalFailureException extends FailureReason {

  public CriticalFailureException(@NonNull String message, @NonNull Throwable throwable) {
    super(message, throwable);
    FailEarly.fail(null, throwable, "Critical error: " + message);
  }

  public CriticalFailureException(@NonNull String message) {
    super(message);
    FailEarly.fail(null, "Critical error: " + message);
  }

  public CriticalFailureException(@NonNull Throwable e) {
    super(e);
    FailEarly.fail(null, e, null);
  }
}
