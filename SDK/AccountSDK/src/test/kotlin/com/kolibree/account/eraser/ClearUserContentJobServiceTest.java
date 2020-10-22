/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.account.eraser;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.job.JobParameters;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.failearly.FailEarly;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate;
import com.kolibree.android.test.utils.failearly.delegate.TestDelegate;
import com.kolibree.sdkws.core.avro.AvroFileUploader;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import java.util.Arrays;
import java.util.HashSet;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

/** Created by miguelaragues on 7/3/18. */
public class ClearUserContentJobServiceTest extends BaseUnitTest {

  private FakeClearUserContentJobService clearContentJobService =
      spy(new FakeClearUserContentJobService());

  @Test
  public void onStartJob_invokesKolibreeConnectorLogout() {
    JobParameters params = mock(JobParameters.class);
    doNothing().when(clearContentJobService).truncateDatabase();
    doNothing().when(clearContentJobService).runUserLogoutHook();
    doNothing().when(clearContentJobService).forgetToothbrushes();
    doNothing().when(clearContentJobService).clearPreferences();
    doNothing().when(clearContentJobService).clearAvroFiles();

    clearContentJobService.onStartJob(params);
  }

  @Test
  public void onStartJob_invokesTruncateDatabase() {
    JobParameters params = mock(JobParameters.class);
    doNothing().when(clearContentJobService).truncateDatabase();
    doNothing().when(clearContentJobService).runUserLogoutHook();
    doNothing().when(clearContentJobService).forgetToothbrushes();
    doNothing().when(clearContentJobService).clearPreferences();
    doNothing().when(clearContentJobService).clearAvroFiles();

    mockFacadeConnector();

    clearContentJobService.onStartJob(params);

    verify(clearContentJobService).truncateDatabase();
  }

  @Test
  public void onStartJob_invokesForgetToothbrushes() {
    JobParameters params = mock(JobParameters.class);
    doNothing().when(clearContentJobService).truncateDatabase();
    doNothing().when(clearContentJobService).runUserLogoutHook();
    doNothing().when(clearContentJobService).forgetToothbrushes();
    doNothing().when(clearContentJobService).clearPreferences();
    doNothing().when(clearContentJobService).clearAvroFiles();

    mockFacadeConnector();

    clearContentJobService.onStartJob(params);

    verify(clearContentJobService).forgetToothbrushes();
  }

  @Test
  public void onStartJob_invokesClearPreferences() {
    JobParameters params = mock(JobParameters.class);
    doNothing().when(clearContentJobService).truncateDatabase();
    doNothing().when(clearContentJobService).runUserLogoutHook();
    doNothing().when(clearContentJobService).forgetToothbrushes();
    doNothing().when(clearContentJobService).clearPreferences();
    doNothing().when(clearContentJobService).clearAvroFiles();

    mockFacadeConnector();

    clearContentJobService.onStartJob(params);

    verify(clearContentJobService).clearPreferences();
  }

  @Test
  public void onStartJob_invokesClearAvroFiles() {
    JobParameters params = mock(JobParameters.class);
    doNothing().when(clearContentJobService).truncateDatabase();
    doNothing().when(clearContentJobService).runUserLogoutHook();
    doNothing().when(clearContentJobService).forgetToothbrushes();
    doNothing().when(clearContentJobService).clearPreferences();
    doNothing().when(clearContentJobService).clearAvroFiles();

    mockFacadeConnector();

    clearContentJobService.onStartJob(params);

    verify(clearContentJobService).clearAvroFiles();
  }

  @Test
  public void onStartJob_returnsTrue() {
    JobParameters params = mock(JobParameters.class);
    doNothing().when(clearContentJobService).truncateDatabase();
    doNothing().when(clearContentJobService).runUserLogoutHook();
    doNothing().when(clearContentJobService).forgetToothbrushes();
    doNothing().when(clearContentJobService).clearPreferences();
    doNothing().when(clearContentJobService).clearAvroFiles();

    mockFacadeConnector();

    assertTrue(clearContentJobService.onStartJob(params));
  }

  @Test
  public void onStartJob_storesParameters() {
    JobParameters params = mock(JobParameters.class);
    doNothing().when(clearContentJobService).truncateDatabase();
    doNothing().when(clearContentJobService).runUserLogoutHook();
    doNothing().when(clearContentJobService).forgetToothbrushes();
    doNothing().when(clearContentJobService).clearPreferences();
    doNothing().when(clearContentJobService).clearAvroFiles();

    mockFacadeConnector();

    assertNull(clearContentJobService.parameters);

    clearContentJobService.onStartJob(params);

    assertEquals(params, clearContentJobService.parameters);
  }

  @Test
  public void onStartJob_runUserLogoutHook() {
    JobParameters params = mock(JobParameters.class);
    doNothing().when(clearContentJobService).truncateDatabase();
    doNothing().when(clearContentJobService).runUserLogoutHook();
    doNothing().when(clearContentJobService).forgetToothbrushes();
    doNothing().when(clearContentJobService).clearPreferences();
    doNothing().when(clearContentJobService).clearAvroFiles();

    mockFacadeConnector();

    clearContentJobService.onStartJob(params);

    verify(clearContentJobService).runUserLogoutHook();
  }

  /*
  CLEAR AVRO FILES
   */
  @Test
  public void clearAvroFiles_invokesRunCompletableWithCompletableThatInvokesDeleteFiles() {
    AvroFileUploader avroFileUploader = mock(AvroFileUploader.class);
    clearContentJobService.avroFileUploader = avroFileUploader;

    doNothing().when(clearContentJobService).runCompletable(any(Completable.class));

    clearContentJobService.clearAvroFiles();

    ArgumentCaptor<Completable> completableCaptor = ArgumentCaptor.forClass(Completable.class);
    verify(clearContentJobService).runCompletable(completableCaptor.capture());

    Completable completable = completableCaptor.getValue();

    assertNotNull(completable);

    verify(avroFileUploader, never()).deletePendingFiles();

    completable.test().assertComplete();

    verify(avroFileUploader).deletePendingFiles();
  }

  /*
  TRUNCATE DATABASE
   */
  @Test
  public void truncateDatabase_runsAllCompletablesInTruncableSet() {
    final boolean[] completablesRun = new boolean[2];
    Completable completable1 = Completable.fromAction(() -> completablesRun[0] = true);
    Completable completable2 = Completable.fromAction(() -> completablesRun[1] = true);

    clearContentJobService.truncables = new HashSet<>();

    clearContentJobService.truncables.add(() -> completable1);
    clearContentJobService.truncables.add(() -> completable2);

    doNothing().when(clearContentJobService).runCompletable(any());

    clearContentJobService.truncateDatabase();

    ArgumentCaptor<Completable> captor = ArgumentCaptor.forClass(Completable.class);
    verify(clearContentJobService).runCompletable(captor.capture());

    assertFalse(completablesRun[0]);
    assertFalse(completablesRun[1]);

    captor.getValue().test();

    assertTrue(completablesRun[0]);
    assertTrue(completablesRun[1]);
  }

  /*
  USER LOGOUT HOOKS
  */
  @Test
  public void runUserLogoutHook_runsAllCompletablesInUserLogoutHooks() {
    final boolean[] completablesRun = new boolean[2];
    Completable completable1 = Completable.fromAction(() -> completablesRun[0] = true);
    Completable completable2 = Completable.fromAction(() -> completablesRun[1] = true);

    clearContentJobService.userLogoutHooks = new HashSet<>();

    clearContentJobService.userLogoutHooks.add(() -> completable1);
    clearContentJobService.userLogoutHooks.add(() -> completable2);

    doNothing().when(clearContentJobService).runCompletable(any());

    clearContentJobService.runUserLogoutHook();

    ArgumentCaptor<Completable> captor = ArgumentCaptor.forClass(Completable.class);
    verify(clearContentJobService).runCompletable(captor.capture());

    assertFalse(completablesRun[0]);
    assertFalse(completablesRun[1]);

    captor.getValue().test();

    assertTrue(completablesRun[0]);
    assertTrue(completablesRun[1]);
  }

  /*
  RUN COMPLETABLE
   */

  @Test
  public void runCompletable_invokesLifeCycleMethods() {
    final CompletableEmitter[] emitters = new CompletableEmitter[1];
    Completable completable = Completable.create(emitter -> emitters[0] = emitter);

    clearContentJobService.runCompletable(completable);

    verify(clearContentJobService).onObservableStarted();
    verify(clearContentJobService, never()).onObservableCompleted();

    doNothing().when(clearContentJobService).onObservableCompleted();

    emitters[0].onComplete();

    verify(clearContentJobService).onObservableCompleted();
  }

  @Test
  public void runCompletable_error_invokesLifeCycleMethods() {
    final CompletableEmitter[] emitters = new CompletableEmitter[1];
    Completable completable = Completable.create(emitter -> emitters[0] = emitter);

    InOrder inOrder = Mockito.inOrder(clearContentJobService);

    clearContentJobService.runCompletable(completable);

    verify(clearContentJobService).onObservableStarted();
    verify(clearContentJobService, never()).onObservableError(any(Throwable.class));
    verify(clearContentJobService, never()).onObservableCompleted();

    doNothing().when(clearContentJobService).onObservableCompleted();

    Throwable throwable = new Throwable("Test forced error");
    emitters[0].onError(throwable);

    inOrder.verify(clearContentJobService).onObservableError(throwable);
    inOrder.verify(clearContentJobService).onObservableCompleted();
  }

  /*
  ON OBSERVABLE LIFECYCLES
   */
  @Test
  public void onObservableStarted_incrementsObservableCounter() {
    assertEquals(0, clearContentJobService.observableCounter.get());

    clearContentJobService.onObservableStarted();

    assertEquals(1, clearContentJobService.observableCounter.get());
  }

  @Test
  public void onObservableError_flagsHadErrors() {
    assertFalse(clearContentJobService.observableHadError);

    clearContentJobService.onObservableError(mock(Throwable.class));

    assertTrue(clearContentJobService.observableHadError);
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void
      onObservableCompleted_counterWas1_invokesJobFinishedWithParametersAndHadErrorsValue() {
    clearContentJobService.observableCounter.incrementAndGet();

    JobParameters params = mock(JobParameters.class);
    clearContentJobService.parameters = params;

    boolean expectedNeedsReschedule = true;
    clearContentJobService.observableHadError = expectedNeedsReschedule;

    doNothing().when(clearContentJobService).jobFinished(any(JobParameters.class), anyBoolean());

    clearContentJobService.onObservableCompleted();

    verify(clearContentJobService).jobFinished(params, expectedNeedsReschedule);
  }

  @Test
  public void onObservableCompleted_counterWas2_neverInvokesJobFinished() {
    clearContentJobService.observableCounter.set(2);

    JobParameters params = mock(JobParameters.class);
    clearContentJobService.parameters = params;

    boolean ignoredNeedsReschedule = true;
    clearContentJobService.observableHadError = ignoredNeedsReschedule;

    clearContentJobService.onObservableCompleted();

    verify(clearContentJobService, never()).jobFinished(params, ignoredNeedsReschedule);
  }

  /*
  FORGET TOOTHBRUSH
   */
  @Test
  public void forgetToothbrushes_facadeReturnsService_invokesOnServiceConnected() {
    FailEarly.overrideDelegateWith(NoopTestDelegate.INSTANCE);

    ServiceProvider serviceProvider = mock(ServiceProvider.class);
    KolibreeService service = mock(KolibreeService.class);
    when(serviceProvider.connectOnce()).thenReturn(Single.just(service));
    clearContentJobService.serviceProvider = serviceProvider;

    doNothing().when(clearContentJobService).onServiceConnected(service);
    doNothing().when(clearContentJobService).onObservableCompleted();

    clearContentJobService.forgetToothbrushes();

    verify(clearContentJobService).onServiceConnected(service);
  }

  @Test
  public void forgetToothbrushes_invokesLifeCycleMethods() {
    final SingleEmitter[] emitters = new SingleEmitter[1];
    mockServiceProvider(Single.create(emitter -> emitters[0] = emitter));

    clearContentJobService.forgetToothbrushes();

    verify(clearContentJobService).onObservableStarted();
    verify(clearContentJobService, never()).onObservableCompleted();

    doNothing().when(clearContentJobService).onServiceConnected(any(KolibreeService.class));
    doNothing().when(clearContentJobService).onObservableCompleted();

    emitters[0].onSuccess(mock(KolibreeService.class));

    verify(clearContentJobService).onObservableCompleted();
  }

  @Test
  public void forgetToothbrushes_toothbrushes_error_invokesLifeCycleMethods() {
    final SingleEmitter[] emitters = new SingleEmitter[1];
    mockServiceProvider(Single.create(emitter -> emitters[0] = emitter));

    InOrder inOrder = Mockito.inOrder(clearContentJobService);

    clearContentJobService.forgetToothbrushes();

    verify(clearContentJobService).onObservableStarted();
    verify(clearContentJobService, never()).onObservableError(any(Throwable.class));
    verify(clearContentJobService, never()).onObservableCompleted();

    doNothing().when(clearContentJobService).onObservableCompleted();

    Throwable throwable = new Throwable("Test forced error");
    emitters[0].onError(throwable);

    inOrder.verify(clearContentJobService).onObservableError(throwable);
    inOrder.verify(clearContentJobService).onObservableCompleted();
  }

  /*
  ON SERVICE CONNECTED
   */
  @Test
  public void onServiceConnected_invokesForgetOnEveryConnection() {
    KolibreeService service = mock(KolibreeService.class);
    KLTBConnection connection1 = mock(KLTBConnection.class);
    KLTBConnection connection2 = mock(KLTBConnection.class);
    when(service.getKnownConnections()).thenReturn(Arrays.asList(connection1, connection2));

    clearContentJobService.onServiceConnected(service);

    verify(service).forget(connection1);
    verify(service).forget(connection2);
  }

  @After
  @Override
  public void tearDown() throws Exception {
    FailEarly.overrideDelegateWith(TestDelegate.INSTANCE);
    super.tearDown();
  }

  private void mockFacadeConnector() {
    KolibreeService mock = mock(KolibreeService.class);
    mockServiceProvider(Single.just(mock));
  }

  private void mockServiceProvider(Single<KolibreeService> single) {

    ServiceProvider serviceProvider = mock(ServiceProvider.class);
    when(serviceProvider.connectOnce()).thenReturn(single);
    clearContentJobService.serviceProvider = serviceProvider;
  }

  static class FakeClearUserContentJobService extends ClearUserContentJobService {}
}
