package com.kolibree.android.sdk.core;

import static org.mockito.Mockito.*;

import androidx.annotation.NonNull;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.ConnectionState;
import com.kolibree.android.sdk.connection.state.ConnectionStateListener;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.test.mocks.KLTBConnectionBuilder;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;
import org.mockito.Mock;

@SuppressWarnings("KotlinInternalInJava")
public class KLTBConnectionProviderImplTest extends BaseUnitTest {

  @Mock ServiceProvider serviceProvider;

  private KLTBConnectionProviderImpl connectionProvider;

  @Override
  public void setup() throws Exception {
    super.setup();

    connectionProvider = spy(new KLTBConnectionProviderImpl(serviceProvider));
  }

  /*
  GET KLTB CONNECTION MAYBE
   */

  @Test
  public void
      existingConnectionWithStates_expectingACTIVE_getKLTBConnectionSingleReturnsError_returnsSingleError() {
    String mac = "das";
    doReturn(Single.error(new Throwable("Test Forced error")))
        .when(connectionProvider)
        .getKLTBConnectionSingle(mac);

    connectionProvider
        .existingConnectionWithStates(mac, Collections.singletonList(KLTBConnectionState.ACTIVE))
        .test()
        .assertError(Throwable.class);
  }

  @Test
  public void existingConnectionWithStates_registersAsStateListener() {
    String mac = "das";
    KLTBConnection connection = mockKLTBConnection(KLTBConnectionState.ACTIVE);

    doReturn(Single.just(connection)).when(connectionProvider).getKLTBConnectionSingle(mac);

    TestObserver<KLTBConnection> observer =
        connectionProvider
            .existingConnectionWithStates(
                mac, Collections.singletonList(KLTBConnectionState.ACTIVE))
            .test();

    verify(connection.state()).register(any());
  }

  @Test
  public void
      existingConnectionWithStates_expectingACTIVE_nonActiveConnection_waitsToReturnUntilConnectionActive() {
    String mac = "das";
    PublishSubject<KLTBConnectionState> stateSubject = PublishSubject.create();
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.NEW)
            .withStateListener(stateSubject)
            .build();

    doReturn(Single.just(connection)).when(connectionProvider).getKLTBConnectionSingle(mac);

    TestObserver<KLTBConnection> observer =
        connectionProvider
            .existingConnectionWithStates(
                mac, Collections.singletonList(KLTBConnectionState.ACTIVE))
            .test();

    observer.assertEmpty();
    observer.assertNoErrors();

    stateSubject.onNext(KLTBConnectionState.ESTABLISHING);

    observer.assertEmpty();
    observer.assertNoErrors();

    stateSubject.onNext(KLTBConnectionState.ACTIVE);

    observer.assertValue(connection);
    observer.assertComplete();
  }

  @Test
  public void
      existingConnectionWithStates_expectingTERMINATING_nonActiveConnection_transitionsToTERMINATING_emitsConnection() {
    String mac = "das";
    PublishSubject<KLTBConnectionState> stateSubject = PublishSubject.create();
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.NEW)
            .withStateListener(stateSubject)
            .build();

    doReturn(Single.just(connection)).when(connectionProvider).getKLTBConnectionSingle(mac);

    TestObserver<KLTBConnection> observer =
        connectionProvider
            .existingConnectionWithStates(
                mac, Collections.singletonList(KLTBConnectionState.TERMINATING))
            .test();

    observer.assertEmpty();
    observer.assertNoErrors();

    stateSubject.onNext(KLTBConnectionState.TERMINATING);

    observer.assertValue(connection);
    observer.assertComplete();
  }

  @Test
  public void
      existingConnectionWithStates_expectingTERMINATINGorNEW_nonActiveConnection_transitionsToTERMINATING_emitsConnection() {
    String mac = "das";
    PublishSubject<KLTBConnectionState> stateSubject = PublishSubject.create();
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .withStateListener(stateSubject)
            .build();

    doReturn(Single.just(connection)).when(connectionProvider).getKLTBConnectionSingle(mac);

    TestObserver<KLTBConnection> observer =
        connectionProvider
            .existingConnectionWithStates(
                mac, Arrays.asList(KLTBConnectionState.TERMINATING, KLTBConnectionState.NEW))
            .test();

    observer.assertEmpty();
    observer.assertNoErrors();

    stateSubject.onNext(KLTBConnectionState.TERMINATING);

    observer.assertValue(connection);
    observer.assertComplete();
  }

  @Test
  public void
      existingConnectionWithStates_expectingTERMINATINGorNEW_nonActiveConnection_transitionsToNEW_emitsConnection() {
    String mac = "das";
    PublishSubject<KLTBConnectionState> stateSubject = PublishSubject.create();
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.ESTABLISHING)
            .withStateListener(stateSubject)
            .build();

    doReturn(Single.just(connection)).when(connectionProvider).getKLTBConnectionSingle(mac);

    TestObserver<KLTBConnection> observer =
        connectionProvider
            .existingConnectionWithStates(
                mac, Arrays.asList(KLTBConnectionState.TERMINATING, KLTBConnectionState.NEW))
            .test();

    observer.assertEmpty();
    observer.assertNoErrors();

    stateSubject.onNext(KLTBConnectionState.NEW);

    observer.assertValue(connection);
    observer.assertComplete();
  }

  @Test
  public void
      existingConnectionWithStates_expectingACTIVE_nonActiveConnection_transitionsToTERMINATING_emitsError() {
    String mac = "das";
    PublishSubject<KLTBConnectionState> stateSubject = PublishSubject.create();
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.NEW)
            .withStateListener(stateSubject)
            .build();

    doReturn(Single.just(connection)).when(connectionProvider).getKLTBConnectionSingle(mac);

    TestObserver<KLTBConnection> observer =
        connectionProvider
            .existingConnectionWithStates(
                mac, Collections.singletonList(KLTBConnectionState.ACTIVE))
            .test();

    observer.assertEmpty();
    observer.assertNoErrors();

    stateSubject.onNext(KLTBConnectionState.TERMINATING);

    observer.assertError(Throwable.class);
  }

  @Test
  public void
      existingConnectionWithStates_expectingACTIVE_nonActiveConnection_transitionsToTERMINATED_emitsError() {
    String mac = "das";
    PublishSubject<KLTBConnectionState> stateSubject = PublishSubject.create();
    KLTBConnection connection =
        KLTBConnectionBuilder.createAndroidLess()
            .withState(KLTBConnectionState.NEW)
            .withStateListener(stateSubject)
            .build();

    doReturn(Single.just(connection)).when(connectionProvider).getKLTBConnectionSingle(mac);

    TestObserver<KLTBConnection> observer =
        connectionProvider
            .existingConnectionWithStates(
                mac, Collections.singletonList(KLTBConnectionState.ACTIVE))
            .test();

    observer.assertEmpty();
    observer.assertNoErrors();

    stateSubject.onNext(KLTBConnectionState.TERMINATED);

    observer.assertError(Throwable.class);
  }

  @Test
  public void
      existingConnectionWithStates_expectingACTIVE_dispose_invokesOnUserDisposedConnection() {
    String mac = "das";
    KLTBConnection kltbConnection = mockKLTBConnection(KLTBConnectionState.ACTIVE);

    doReturn(Single.just(kltbConnection)).when(connectionProvider).getKLTBConnectionSingle(mac);

    TestObserver<KLTBConnection> observer =
        connectionProvider
            .existingConnectionWithStates(
                mac, Collections.singletonList(KLTBConnectionState.ACTIVE))
            .test();

    verify(connectionProvider, never()).onUserDisposedConnection(any());

    observer.dispose();

    verify(connectionProvider).onUserDisposedConnection(kltbConnection);
  }

  /*
  ON USER DISPOSED CONNECTION
   */
  @Test
  public void onUserDisposedConnection_stateListenerNull_doesNothing() {
    KLTBConnection connection = mockKLTBConnection(KLTBConnectionState.ACTIVE);

    connectionProvider.onUserDisposedConnection(connection);
  }

  @Test
  public void onUserDisposedConnection_stateListenerNotNull_invokesUnregisterAndThenDisconnect() {
    KLTBConnection connection = mockKLTBConnection(KLTBConnectionState.ACTIVE);

    connectionProvider.stateListener = mock(ConnectionStateListener.class);

    connectionProvider.onUserDisposedConnection(connection);

    verify(connection.state()).unregister(connectionProvider.stateListener);
  }

  /*
  GET KLTB CONNECTION SINGLE
   */
  @Test
  public void getKLTBConnectionSingle_serviceReturnsNullConnection_returnsError() {
    String mac = "das";
    KolibreeService service = setupServiceProvider();
    when(service.getConnection(mac)).thenReturn(null);

    connectionProvider.getKLTBConnectionSingle(mac).test().assertError(Throwable.class);
  }

  @Test
  public void getKLTBConnectionSingle_serviceReturnsConnection_returnsSuccess() {
    String mac = "das";
    KolibreeService service = setupServiceProvider();
    KLTBConnection expectedConnection = mock(KLTBConnection.class);
    when(service.getConnection(mac)).thenReturn(expectedConnection);

    connectionProvider
        .getKLTBConnectionSingle(mac)
        .test()
        .assertValue(expectedConnection)
        .assertComplete();
  }

  /*
  UTILS
   */

  @NonNull
  private KolibreeService setupServiceProvider() {
    KolibreeService service = mock(KolibreeService.class);
    when(serviceProvider.connectOnce()).thenReturn(Single.just(service));
    return service;
  }

  @NonNull
  private KLTBConnection mockKLTBConnection(KLTBConnectionState active) {
    KLTBConnection kltbConnection = mock(KLTBConnection.class);
    ConnectionState connectionState = mock(ConnectionState.class);
    when(connectionState.getCurrent()).thenReturn(active);
    when(kltbConnection.state()).thenReturn(connectionState);
    return kltbConnection;
  }
}
