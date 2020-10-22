package com.kolibree.android.sdk.util;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.util.concurrent.TimeUnit;
import org.reactivestreams.Publisher;

public final class RetryWithDelay implements Function<Flowable<Throwable>, Publisher<?>> {
  private final int maxRetries;
  private final int retryDelayMillis;
  private int retryCount;

  public RetryWithDelay(final int maxRetries, final int retryDelayMillis) {
    this.maxRetries = maxRetries;
    this.retryDelayMillis = retryDelayMillis;
    this.retryCount = 0;
  }

  @Override
  public Publisher<?> apply(Flowable<Throwable> attempts) throws Exception {
    return attempts.flatMap(
        (Function<Throwable, Publisher<?>>)
            throwable -> {
              if (++retryCount < maxRetries) {
                // When this Observable calls onNext, the original
                // Observable will be retried (i.e. re-subscribed).
                return Flowable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
              }

              // Max retries hit. Just pass the error along.
              return Flowable.error(throwable);
            });
  }
}
