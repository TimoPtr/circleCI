/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.sdk.BluetoothStateReceiver;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/** Created by miguelaragues on 13/12/17. */
public class BluetoothUtilsImplTest extends BaseUnitTest {

  @Mock private Context context;

  BluetoothUtilsImpl bluetoothUtils;

  @Before
  public void setup() throws Exception {
    super.setup();

    when(context.getApplicationContext()).thenReturn(context);

    bluetoothUtils = spy(new BluetoothUtilsImpl(context));
  }

  @Test
  public void bluetoothStateObservable_multipleInvocationsReceiveSameInstance() {
    assertEquals(
        bluetoothUtils.bluetoothStateObservable(), bluetoothUtils.bluetoothStateObservable());
  }

  @Test
  public void bluetoothStateObservable_subscribeInvokesMaybeRegisterBluetoothReceiver() {
    Observable<Boolean> observable = bluetoothUtils.bluetoothStateObservable();

    doNothing().when(bluetoothUtils).maybeRegisterBluetoothStateReceiver();

    verify(bluetoothUtils, never()).maybeRegisterBluetoothStateReceiver();

    observable.test();

    verify(bluetoothUtils).maybeRegisterBluetoothStateReceiver();
  }

  @Test
  public void
      bluetoothStateObservable_disposeObserver_invokesonAllBluetoothStateObserversUnsubscribed() {
    Observable<Boolean> observable = bluetoothUtils.bluetoothStateObservable();

    doNothing().when(bluetoothUtils).maybeRegisterBluetoothStateReceiver();
    doNothing().when(bluetoothUtils).onAllBluetoothStateObserversUnsubscribed();

    Disposable disposable = observable.subscribe();

    verify(bluetoothUtils, never()).onAllBluetoothStateObserversUnsubscribed();

    disposable.dispose();

    verify(bluetoothUtils).onAllBluetoothStateObserversUnsubscribed();
  }

  @Test
  public void
      bluetoothStateObservable_multipleObserver_onlyInvokesonAllBluetoothStateObserversUnsubscribedWhenAllDisposed() {
    Observable<Boolean> observable = bluetoothUtils.bluetoothStateObservable();

    doNothing().when(bluetoothUtils).maybeRegisterBluetoothStateReceiver();
    doNothing().when(bluetoothUtils).onAllBluetoothStateObserversUnsubscribed();

    Disposable disposable = observable.subscribe();
    Disposable disposable2 = observable.subscribe();

    verify(bluetoothUtils, never()).onAllBluetoothStateObserversUnsubscribed();

    disposable2.dispose();

    verify(bluetoothUtils, never()).onAllBluetoothStateObserversUnsubscribed();

    disposable.dispose();

    verify(bluetoothUtils).onAllBluetoothStateObserversUnsubscribed();
  }

  /*
  ON ALL BLUETOOTH STATE OBSERVERS UNSUBSCRIBED
   */
  @Test
  public void onAllBluetoothStateObserversUnsubscribed_nullifiesSharedObservable() {
    doNothing().when(bluetoothUtils).maybeUnregisterBluetoothStateReceiver();

    bluetoothUtils.setBluetoothStateObservable(PublishSubject.create());

    bluetoothUtils.onAllBluetoothStateObserversUnsubscribed();

    assertNull(bluetoothUtils.getBluetoothStateObservable());
  }

  @Test
  public void
      onAllBluetoothStateObserversUnsubscribed_invokesMaybeUnregisterBluetoothStateReceiver() {
    doNothing().when(bluetoothUtils).maybeUnregisterBluetoothStateReceiver();

    verify(bluetoothUtils, never()).maybeUnregisterBluetoothStateReceiver();

    bluetoothUtils.onAllBluetoothStateObserversUnsubscribed();

    verify(bluetoothUtils).maybeUnregisterBluetoothStateReceiver();
  }

  /*
  MAYBE REGISTER BLUETOOTHSTATE RECEIVER
   */
  @Test
  public void maybeRegisterBluetoothStateReceiver_receiverNull_registersNewReceiver() {
    doReturn(true).when(bluetoothUtils).shouldRegisterExplicitBluetoothReceiver();

    assertNull(bluetoothUtils.getReceiver());

    bluetoothUtils.maybeRegisterBluetoothStateReceiver();

    verify(context).registerReceiver(any(BluetoothStateReceiver.class), any(IntentFilter.class));
  }

  @Test
  public void maybeRegisterBluetoothStateReceiver_receiverNull_storesReceiver() {
    doReturn(true).when(bluetoothUtils).shouldRegisterExplicitBluetoothReceiver();

    assertNull(bluetoothUtils.getReceiver());

    bluetoothUtils.maybeRegisterBluetoothStateReceiver();

    assertNotNull(bluetoothUtils.getReceiver());
  }

  /*
  MAYBE UNREGISTER BLUETOOTHSTATE RECEIVER
   */
  @Test
  public void maybeUnregisterBluetoothStateReceiver_receiverNull_doesNothing() {
    assertNull(bluetoothUtils.getReceiver());

    bluetoothUtils.maybeUnregisterBluetoothStateReceiver();

    verify(context, never()).unregisterReceiver(any());
    verify(context, never()).unregisterReceiver(isNull());
  }

  @Test
  public void maybeUnregisterBluetoothStateReceiver_receiverNotNull_unregistersReceiver() {
    BroadcastReceiver expectedReceiver = mock(BluetoothStateReceiver.class);
    bluetoothUtils.setReceiver(expectedReceiver);

    bluetoothUtils.maybeUnregisterBluetoothStateReceiver();

    verify(context).unregisterReceiver(expectedReceiver);
  }
}
