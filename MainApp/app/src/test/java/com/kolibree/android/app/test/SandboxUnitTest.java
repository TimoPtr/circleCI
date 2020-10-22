/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.test;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import org.junit.Ignore;
import org.junit.Test;
import org.threeten.bp.Clock;
import org.threeten.bp.DateTimeUtils;
import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.ZonedDateTime;
import timber.log.Timber;

/**
 * Sandbox to quickly try stuff
 *
 * <p>Created by miguelaragues on 10/1/18.
 */
@Ignore("This is only a playground")
public class SandboxUnitTest extends BaseUnitTest {

  @Test
  public void flatmapCompletable() {
    Single.just(1)
        .flatMapCompletable(
            value -> {
              if (value == 1) {
                return Completable.complete();
              }

              return Completable.error(new Throwable("Error"));
            })
        .subscribe(() -> {}, Throwable::printStackTrace);
  }

  @Test
  public void combineLatest() {
    PublishSubject<Integer> integerPublishSubject = PublishSubject.create();
    PublishSubject<Long> longPublishSubject = PublishSubject.create();

    TestObserver<String> observer =
        Observable.combineLatest(
                integerPublishSubject,
                longPublishSubject,
                (integer, aLong) -> "combining " + integer + " and " + aLong)
            .subscribeOn(Schedulers.io())
            .test();

    observer.assertEmpty();

    integerPublishSubject.onNext(1);

    observer.assertEmpty();

    longPublishSubject.onNext(2L);

    observer.assertValueCount(1);
  }

  @Test
  public void completedSubject() {
    ReplaySubject<Integer> integerBehaviorSubject = ReplaySubject.create(1);

    integerBehaviorSubject.onNext(5);

    Timber.d("pre complete Values is " + integerBehaviorSubject.test().values().get(0));

    Timber.d("pre complete Values size " + integerBehaviorSubject.test().values().size());

    integerBehaviorSubject.onComplete();

    Timber.d("Values size " + integerBehaviorSubject.test().values().size());

    Timber.d("post complete Values is " + integerBehaviorSubject.test().values().get(0));
  }

  @Test
  public void flatmap() {
    PublishSubject<Integer> subject = PublishSubject.create();

    Disposable disposable =
        subject
            .flatMap(ignore -> Observable.interval(30, TimeUnit.MILLISECONDS))
            .subscribe(
                value -> {
                  Timber.d("Received value %s", value);

                  if (value == 5) {
                    subject.onComplete();
                    Timber.d("Send on complete");
                  }
                },
                Throwable::printStackTrace,
                () -> Timber.d("Completed"));

    subject.subscribe(
        value -> {
          Timber.d("Second disposables value %s", value);
        },
        Throwable::printStackTrace,
        () -> Timber.d("Second Completed"));

    subject.onNext(1);
  }

  @Test
  public void timestamp() {
    long timestampUTC = 1542299610000L;

    ZonedDateTime userTimestampAtZone1 =
        Instant.ofEpochMilli(timestampUTC).atZone(ZoneId.systemDefault());

    ZonedDateTime userTimestampAtZone2 =
        ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestampUTC), ZoneId.systemDefault());

    Timber.d("Timestamp 1 %s", userTimestampAtZone1);
    Timber.d("Timestamp 2 %s", userTimestampAtZone2);
  }

  @Test
  public void date() {
    long timestamp = Clock.systemUTC().millis();

    Timber.d("Original timestamp %s", timestamp);

    Timber.d(new Date(timestamp).toString());

    ZonedDateTime datetime = ZonedDateTime.now(Clock.systemUTC());
    Date date = DateTimeUtils.toDate(datetime.toInstant());

    Timber.d("Zoned %s", datetime);

    Timber.d("Current timezone %s", ZoneId.systemDefault());

    ZonedDateTime dateUtc = datetime.withZoneSameInstant(ZoneOffset.UTC);
    ZonedDateTime dateDefault = datetime.withZoneSameInstant(ZoneId.systemDefault());

    Timber.d("\nZone at utc %s", dateUtc);
    Timber.d("\nZone at default %s", dateDefault);
    Timber.d("\nZone to date utc %s", DateTimeUtils.toDate(dateUtc.toInstant()));
    Timber.d("\nZone to date %s", DateTimeUtils.toDate(dateDefault.toInstant()));
  }
}
