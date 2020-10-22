/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.sdk.scan;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.sdk.error.BluetoothNotEnabledException;
import com.kolibree.android.sdk.util.IBluetoothUtils;
import io.reactivex.disposables.Disposable;
import io.reactivex.subjects.PublishSubject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.stubbing.Answer;

/** Created by miguelaragues on 29/12/17. */
@SuppressWarnings("KotlinInternalInJava")
public class NordicBleScannerWrapperTest extends BaseUnitTest {

  @Mock IBluetoothUtils bluetoothUtils;

  @Mock BluetoothWindowedScanner windowedScanner;

  @Mock BluetoothLeScannerCompat scanner;

  @Mock ScanCallbackProvider scanCallbackProvider;

  private NordicBleScannerWrapper bleScannerWrapper;

  @Override
  public void setup() throws Exception {
    super.setup();

    bleScannerWrapper =
        spy(
            new NordicBleScannerWrapper(
                bluetoothUtils, scanner, windowedScanner, scanCallbackProvider));
  }

  /*
  stopScan PendingIntent
   */
  @Test
  public void stopScanPendingIntent_withBluetoothOff_doesNothing() {
    Context context = mock(Context.class);
    PendingIntent pendingIntent = mock(PendingIntent.class);

    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(false);

    bleScannerWrapper.stopScan(context, pendingIntent);

    verifyNoMoreInteractions(scanner);
  }

  @Test
  public void stopScanPendingIntent_withBluetoothOn_invokesStopScan() {
    Context context = mock(Context.class);
    PendingIntent pendingIntent = mock(PendingIntent.class);

    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    bleScannerWrapper.stopScan(context, pendingIntent);

    verify(scanner).stopScan(context, pendingIntent);
  }

  /*
  startScan PendingIntent
   */
  @Test
  public void startScanPendingIntent_withBluetoothOff_returns_false() {
    Context context = mock(Context.class);
    PendingIntent pendingIntent = mock(PendingIntent.class);

    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(false);

    assertFalse(bleScannerWrapper.startScan(context, Collections.emptyList(), pendingIntent));

    verifyNoMoreInteractions(scanner);
  }

  @Test
  public void startScanPendingIntent_withEmptyMacs_neverInvokesStartScan() {
    Context context = mock(Context.class);
    PendingIntent pendingIntent = mock(PendingIntent.class);

    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    bleScannerWrapper.startScan(context, Collections.emptyList(), pendingIntent);

    verify(scanner, never())
        .startScan(
            anyList(), any(ScanSettings.class), any(Context.class), any(PendingIntent.class));
  }

  @Test
  public void startScanPendingIntent_withEmptyMacs_returnsTrue() {
    Context context = mock(Context.class);
    PendingIntent pendingIntent = mock(PendingIntent.class);

    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    assertTrue(bleScannerWrapper.startScan(context, Collections.emptyList(), pendingIntent));
  }

  @Test
  public void startScanPendingIntent_withMacs_invokesStartScan() {
    Context context = mock(Context.class);
    List<String> macs = Arrays.asList("mac1", "mac2");
    PendingIntent pendingIntent = mock(PendingIntent.class);

    List<ScanFilter> expectedFilters = Collections.emptyList();
    ScanSettings expectedSettings = new ScanSettings.Builder().build();

    doReturn(expectedFilters).when(bleScannerWrapper).createScanFilterForMac(macs);
    doReturn(expectedSettings).when(bleScannerWrapper).pendingIntentScanSettings();

    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    bleScannerWrapper.startScan(context, macs, pendingIntent);

    verify(scanner).startScan(expectedFilters, expectedSettings, context, pendingIntent);
  }

  @Test
  public void startScanPendingIntent_withMacs_returnsTrue() {
    Context context = mock(Context.class);
    List<String> macs = Arrays.asList("mac1", "mac2");
    PendingIntent pendingIntent = mock(PendingIntent.class);

    List<ScanFilter> expectedFilters = Collections.emptyList();
    ScanSettings expectedSettings = new ScanSettings.Builder().build();

    doReturn(expectedFilters).when(bleScannerWrapper).createScanFilterForMac(macs);
    doReturn(expectedSettings).when(bleScannerWrapper).pendingIntentScanSettings();

    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    assertTrue(bleScannerWrapper.startScan(context, macs, pendingIntent));
  }

  /*
  pendingIntentScanSettings
   */
  @Test
  public void pendingIntentScanSettings_usesLowPowerMode() {
    assertTrue(bleScannerWrapper.pendingIntentScanSettings().hasPowerSaveMode());
  }

  /*
  START SCAN
   */
  @Test
  public void startScan_invokesStartScanWithFiltersWithEmptyList()
      throws BluetoothNotEnabledException {
    doNothing()
        .when(bleScannerWrapper)
        .startScanWithFilters(anyList(), any(AnyToothbrushScanCallback.class));

    AnyToothbrushScanCallback expectedCallback = createAnyToothbrushCallback();
    bleScannerWrapper.startScan(expectedCallback, false);

    verify(bleScannerWrapper).startScanWithFilters(Collections.emptyList(), expectedCallback);
  }

  /*
  START SCAN WITH FILTERS
   */

  @Test(expected = BluetoothNotEnabledException.class)
  public void startScanWithFilters_noBluetooth_throwsBluetoothNotEnabledException()
      throws BluetoothNotEnabledException {
    mockBluetoothState(false);

    bleScannerWrapper.startScanWithFilters(Collections.emptyList(), createAnyToothbrushCallback());
  }

  @Test
  public void startScanWithFilters_withBluetooth_isScanningForDeviceTrue_neverInvokesStartScan()
      throws BluetoothNotEnabledException {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    BluetoothDevice device = mock(BluetoothDevice.class);
    SpecificToothbrushScanCallback callback = createSpecificCallback(device);
    doReturn(new AtomicBoolean(true)).when(bleScannerWrapper).isScanning(device);

    doNothing().when(bleScannerWrapper).stopScanningOnBluetoothOff(callback);

    bleScannerWrapper.startScanWithFilters(Collections.emptyList(), callback);

    verify(windowedScanner, never())
        .startScan(anyList(), any(ScanSettings.class), any(KLScanCallback.class));
  }

  @Test
  public void startScanWithFilters_withBluetooth_isScanningForDeviceFalse_invokesStartScan()
      throws BluetoothNotEnabledException {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    BluetoothDevice device = mock(BluetoothDevice.class);
    SpecificToothbrushScanCallback callback = createSpecificCallback(device);

    doReturn(new AtomicBoolean(false)).when(bleScannerWrapper).isScanning(device);

    KLScanCallback expectedScanCallback = mock(KLScanCallback.class);
    when(scanCallbackProvider.getOrCreate(callback)).thenReturn(expectedScanCallback);

    doNothing().when(bleScannerWrapper).stopScanningOnBluetoothOff(callback);

    List<ScanFilter> expectedList = Collections.emptyList();
    bleScannerWrapper.startScanWithFilters(expectedList, callback);

    verify(windowedScanner)
        .startScan(expectedList, bleScannerWrapper.scanSettings, expectedScanCallback);
  }

  @Test
  public void startScanWithFilters_withBluetooth_isScanningForDeviceFalse_setsIsScanningToTrue()
      throws BluetoothNotEnabledException {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    BluetoothDevice device = mock(BluetoothDevice.class);
    SpecificToothbrushScanCallback callback = createSpecificCallback(device);

    AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    doReturn(atomicBoolean).when(bleScannerWrapper).isScanning(device);

    when(scanCallbackProvider.getOrCreate(callback)).thenReturn(mock(KLScanCallback.class));

    doNothing().when(bleScannerWrapper).stopScanningOnBluetoothOff(callback);

    List<ScanFilter> expectedList = Collections.emptyList();
    bleScannerWrapper.startScanWithFilters(expectedList, callback);

    assertTrue(atomicBoolean.get());
  }

  @Test
  public void startScanWithFilters_withBluetooth_invokesStopScanningOnBluetoothOff()
      throws BluetoothNotEnabledException {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    BluetoothDevice device = mock(BluetoothDevice.class);
    SpecificToothbrushScanCallback callback = createSpecificCallback(device);

    when(scanCallbackProvider.getOrCreate(callback)).thenReturn(mock(KLScanCallback.class));

    doNothing().when(bleScannerWrapper).stopScanningOnBluetoothOff(callback);

    bleScannerWrapper.startScanWithFilters(Collections.emptyList(), callback);

    verify(bleScannerWrapper).stopScanningOnBluetoothOff(callback);
  }

  /*
  stopScanningOnBluetoothOff
   */

  @Test
  public void stopScanningOnBluetoothOff_subscribesToBluetoothStateObservable() {
    PublishSubject<Boolean> subject = PublishSubject.create();
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(subject);

    bleScannerWrapper.stopScanningOnBluetoothOff(createAnyToothbrushCallback());

    assertTrue(subject.hasObservers());
  }

  @Test
  public void stopScanningOnBluetoothOff_bluetoothEmitsOn_neverInvokesOnBluetoothOff() {
    PublishSubject<Boolean> subject = PublishSubject.create();
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(subject);

    ToothbrushScanCallback callback = createAnyToothbrushCallback();
    bleScannerWrapper.stopScanningOnBluetoothOff(callback);

    subject.onNext(true);

    verify(bleScannerWrapper, never()).onBluetoothOff(any());
  }

  @Test
  public void stopScanningOnBluetoothOff_bluetoothEmitsOff_invokesOnBluetoothOff() {
    PublishSubject<Boolean> subject = PublishSubject.create();
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(subject);

    ToothbrushScanCallback callback = createAnyToothbrushCallback();
    bleScannerWrapper.stopScanningOnBluetoothOff(callback);

    doNothing().when(bleScannerWrapper).onBluetoothOff(callback);

    subject.onNext(false);

    verify(bleScannerWrapper).onBluetoothOff(callback);
  }

  @Test
  public void
      stopScanningOnBluetoothOff_bluetoothEmitsOff_doesNotUnsubscribesFromBluetoothStateObservable() {
    PublishSubject<Boolean> subject = PublishSubject.create();
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(subject);

    ToothbrushScanCallback callback = createAnyToothbrushCallback();
    bleScannerWrapper.stopScanningOnBluetoothOff(callback);

    subject.onNext(false);

    assertTrue(subject.hasObservers());
  }

  @Test
  public void
      stopScanningOnBluetoothOff_bluetoothEmitsOffMultipleTimes_invokesOnBluetoothOffOnlyOnce() {
    PublishSubject<Boolean> subject = PublishSubject.create();
    when(bluetoothUtils.bluetoothStateObservable()).thenReturn(subject);

    ToothbrushScanCallback callback = createAnyToothbrushCallback();

    doNothing().when(bleScannerWrapper).onBluetoothOff(callback);

    bleScannerWrapper.stopScanningOnBluetoothOff(callback);

    subject.onNext(false);
    subject.onNext(false);
    subject.onNext(false);
    subject.onNext(false);

    verify(bleScannerWrapper).onBluetoothOff(callback);
  }

  /*
  onBluetoothOff
   */
  @Test
  public void onBluetoothOff_invokesStopScan() {
    ToothbrushScanCallback callback = createAnyToothbrushCallback();

    doNothing().when(bleScannerWrapper).stopScan(callback);
    bleScannerWrapper.onBluetoothOff(callback);

    verify(bleScannerWrapper).stopScan(callback);
  }

  @Test
  public void onBluetoothOff_invokesScannerOnBluetoothOff() {
    ToothbrushScanCallback callback = createAnyToothbrushCallback();

    doNothing().when(bleScannerWrapper).stopScan(callback);

    verify(windowedScanner, never()).onBluetoothOff();

    bleScannerWrapper.onBluetoothOff(callback);

    verify(windowedScanner).onBluetoothOff();
  }

  @Test
  public void onBluetoothOff_clearsIsScanningCache() {
    ToothbrushScanCallback callback = createAnyToothbrushCallback();

    doNothing().when(bleScannerWrapper).stopScan(callback);
    BluetoothDevice device = storeIsScanningTrueForDevice();

    assertTrue(bleScannerWrapper.isScanning(device).get());

    bleScannerWrapper.onBluetoothOff(callback);

    assertFalse(bleScannerWrapper.isScanning(device).get());
  }

  /*
  isScanning
   */
  @Test
  public void isScanning_returnsAtomicBooleanWithValueFalse_whenFirstTimeDeviceAsParameter() {
    BluetoothDevice device = mock(BluetoothDevice.class);

    assertFalse(bleScannerWrapper.isScanning(device).get());
  }

  @Test
  public void isScanning_returnsExistingAtomicBoolean_onFutureInvocationsWithDeviceAsParameter() {
    BluetoothDevice device = storeIsScanningTrueForDevice();

    assertTrue(bleScannerWrapper.isScanning(device).get());
  }

  /*
  SCAN FOR
   */

  @Test
  public void scanFor_invokesStartScanWithFiltersWithExpectedFilters()
      throws BluetoothNotEnabledException {
    BluetoothDevice device = mock(BluetoothDevice.class);

    List<ScanFilter> expectedFilters = Collections.emptyList();
    doReturn(expectedFilters).when(bleScannerWrapper).createScanFilterForDevice(device);

    doNothing()
        .when(bleScannerWrapper)
        .startScanWithFilters(anyList(), any(SpecificToothbrushScanCallback.class));

    SpecificToothbrushScanCallback expectedCallback = createSpecificCallback(device);

    bleScannerWrapper.scanFor(expectedCallback);

    verify(bleScannerWrapper).startScanWithFilters(expectedFilters, expectedCallback);
  }

  /*
  CREATE SCAN FILTER FOR DEVICE
   */
  @Test
  public void createScanFilterForDevice_returnsEmptyFilterListIfDeviceIsNull() {
    assertTrue(bleScannerWrapper.createScanFilterForDevice(null).isEmpty());
  }

  @Test
  public void createScanFilterForDevice_returnsFilterWith2FiltersIfDeviceIsNotNull() {
    BluetoothDevice device = mock(BluetoothDevice.class);
    String nonDFUMac = "C0:4B:01:9D:18:78";
    when(device.getAddress()).thenReturn(nonDFUMac);

    String expectedExtraMac = "C0:4B:01:9D:18:79";

    doAnswer(
            (Answer<List<ScanFilter>>)
                invocation -> {
                  List<ScanFilter> filters = new ArrayList<>();
                  for (Object object : invocation.getArguments()) {
                    ScanFilter filter = mock(ScanFilter.class);
                    when(filter.getDeviceAddress()).thenReturn((String) object);

                    filters.add(filter);
                  }

                  return filters;
                })
        .when(bleScannerWrapper)
        .createScanFilterForMac(anyString(), anyString());

    List<ScanFilter> filters = bleScannerWrapper.createScanFilterForDevice(device);

    assertEquals(2, filters.size());

    assertEquals(nonDFUMac, filters.get(0).getDeviceAddress());
    assertEquals(expectedExtraMac, filters.get(1).getDeviceAddress());
  }

  /*
  shouldStopScanWithDelay
   */
  @Test
  public void shouldStopScanWithDelay_bluetoothOff_returnsFalse() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(false);

    SpecificToothbrushScanCallback specificCallback =
        createSpecificCallback(mock(BluetoothDevice.class));
    assertFalse(bleScannerWrapper.shouldStopScanWithDelay(specificCallback));
    assertFalse(bleScannerWrapper.shouldStopScanWithDelay(createAnyToothbrushCallback()));
  }

  @Test
  public void shouldStopScanWithDelay_bluetoothOn_specificCallback_returnsFalse() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    SpecificToothbrushScanCallback specificCallback =
        createSpecificCallback(mock(BluetoothDevice.class));
    assertFalse(bleScannerWrapper.shouldStopScanWithDelay(specificCallback));
  }

  @Test
  public void shouldStopScanWithDelay_bluetoothOn_anyToothbrushCallback_returnsTrue() {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(true);

    assertTrue(bleScannerWrapper.shouldStopScanWithDelay(createAnyToothbrushCallback()));
  }

  /*
  SHOULD STOP SCAN
   */
  @Test
  public void shouldStopScanning_bluetoothOff_returnsTrue() {
    mockBluetoothState(false);

    assertTrue(bleScannerWrapper.shouldStopScanning(klScanCallback(true)));
    assertTrue(bleScannerWrapper.shouldStopScanning(klScanCallback(false)));
    assertTrue(
        bleScannerWrapper.shouldStopScanning(klScanCallback(true, mock(BluetoothDevice.class))));
    assertTrue(
        bleScannerWrapper.shouldStopScanning(klScanCallback(false, mock(BluetoothDevice.class))));
  }

  @Test
  public void shouldStopScanning_callbackHasListeners_returnsFalse() {
    mockBluetoothState(true);

    KLScanCallback klScanCallback = klScanCallback(true);

    assertFalse(bleScannerWrapper.shouldStopScanning(klScanCallback));
  }

  @Test
  public void shouldStopScanning_callbackDoesNotHaveListeners_isScanningFalse_returnsFalse() {
    mockBluetoothState(true);

    BluetoothDevice device = mock(BluetoothDevice.class);
    KLScanCallback klScanCallback = klScanCallback(false, device);

    doReturn(new AtomicBoolean(false)).when(bleScannerWrapper).isScanning(device);

    assertFalse(bleScannerWrapper.shouldStopScanning(klScanCallback));
  }

  @Test
  public void shouldStopScanning_callbackDoesNotHaveListeners_isScanningTrue_returnsTrue() {
    mockBluetoothState(true);

    BluetoothDevice device = mock(BluetoothDevice.class);
    KLScanCallback klScanCallback = klScanCallback(false, device);

    doReturn(new AtomicBoolean(true)).when(bleScannerWrapper).isScanning(device);

    assertTrue(bleScannerWrapper.shouldStopScanning(klScanCallback));
  }

  @Test
  public void
      shouldStopScanning_callbackDoesNotHaveListeners_isScanningTrue_setsAtomicBooleanToFalse() {
    mockBluetoothState(true);

    BluetoothDevice device = mock(BluetoothDevice.class);
    KLScanCallback klScanCallback = klScanCallback(false, device);

    AtomicBoolean atomicBoolean = new AtomicBoolean(true);
    doReturn(atomicBoolean).when(bleScannerWrapper).isScanning(device);

    bleScannerWrapper.shouldStopScanning(klScanCallback);

    assertFalse(atomicBoolean.get());
  }

  /*
  STOP SCAN
   */
  @Test
  public void stopScan_klScanCallbackIsNull_doesNothing() {
    AnyToothbrushScanCallback callback = createAnyToothbrushCallback();

    bleScannerWrapper.stopScan(callback);

    verifyNoMoreInteractions(windowedScanner);
  }

  @Test
  public void stopScan_klScanCallbackNotNull_removesListenerBeforeShouldStopScanning() {
    AnyToothbrushScanCallback callback = createAnyToothbrushCallback();

    KLScanCallback klScanCallback = klScanCallback(true);
    doReturn(klScanCallback).when(scanCallbackProvider).get(callback);

    doReturn(false).when(bleScannerWrapper).shouldStopScanning(klScanCallback);

    bleScannerWrapper.stopScan(callback);

    InOrder inorder = inOrder(klScanCallback, bleScannerWrapper);

    inorder.verify(klScanCallback).removeListener(callback);
    inorder.verify(bleScannerWrapper).shouldStopScanning(klScanCallback);
  }

  @Test
  public void stopScan_klScanCallbackNotNull_shouldStopScanningReturnsFalse_neverInvokesStopScan() {
    AnyToothbrushScanCallback callback = createAnyToothbrushCallback();

    KLScanCallback klScanCallback = klScanCallback(true);
    doReturn(klScanCallback).when(scanCallbackProvider).get(callback);

    doReturn(false).when(bleScannerWrapper).shouldStopScanning(klScanCallback);

    bleScannerWrapper.stopScan(callback);

    verifyNoMoreInteractions(windowedScanner);
  }

  @Test
  public void
      stopScan_klScanCallbackNotNull_shouldStopScanningReturnsFalse_neverDisposesBluetoothStateDisposable() {
    AnyToothbrushScanCallback callback = createAnyToothbrushCallback();

    KLScanCallback klScanCallback = klScanCallback(true);
    doReturn(klScanCallback).when(scanCallbackProvider).get(callback);

    doReturn(false).when(bleScannerWrapper).shouldStopScanning(klScanCallback);

    bleScannerWrapper.bluetoothStateDisposable = mock(Disposable.class);

    bleScannerWrapper.stopScan(callback);

    verify(bleScannerWrapper.bluetoothStateDisposable, never()).dispose();
  }

  @Test
  public void
      stopScan_klScanCallbackNotNull_shouldStopScanningReturnsTrue_neverDisposesBluetoothStateDisposable() {
    AnyToothbrushScanCallback callback = createAnyToothbrushCallback();

    KLScanCallback klScanCallback = klScanCallback(true);
    doReturn(klScanCallback).when(scanCallbackProvider).get(callback);

    doReturn(true).when(bleScannerWrapper).shouldStopScanning(klScanCallback);

    bleScannerWrapper.bluetoothStateDisposable = mock(Disposable.class);

    bleScannerWrapper.stopScan(callback);

    verify(bleScannerWrapper.bluetoothStateDisposable, never()).dispose();
  }

  @Test
  public void
      stopScan_klScanCallbackIsForAnyToothbrush_shouldStopScanningReturnsTrue_shouldStopScanWithDelayReturnsTrue_invokesStopScanWithDelay() {
    AnyToothbrushScanCallback callback = createAnyToothbrushCallback();

    KLScanCallback klScanCallback = klScanCallback(true);
    doReturn(klScanCallback).when(scanCallbackProvider).get(callback);

    doReturn(true).when(bleScannerWrapper).shouldStopScanning(klScanCallback);
    doReturn(true).when(bleScannerWrapper).shouldStopScanWithDelay(callback);

    bleScannerWrapper.stopScan(callback);

    verify(windowedScanner).stopScanWithDelay(klScanCallback);
    verify(windowedScanner, never()).stopScan(klScanCallback);
  }

  @Test
  public void
      stopScan_klScanCallbackIsForSpecificToothbrush_shouldStopScanningReturnsTrue_shouldStopScanWithDelayReturnsFalse_invokesStopScan() {
    SpecificToothbrushScanCallback callback = createSpecificCallback(mock(BluetoothDevice.class));

    KLScanCallback klScanCallback = klScanCallback(true);
    doReturn(klScanCallback).when(scanCallbackProvider).get(callback);

    doReturn(true).when(bleScannerWrapper).shouldStopScanning(klScanCallback);
    doReturn(false).when(bleScannerWrapper).shouldStopScanWithDelay(callback);

    bleScannerWrapper.stopScan(callback);

    verify(windowedScanner).stopScan(klScanCallback);
    verify(windowedScanner, never()).stopScanWithDelay(klScanCallback);
  }

  /*
  UTILS
   */

  static AnyToothbrushScanCallback createAnyToothbrushCallback() {
    return new AnyToothbrushScanCallback() {
      @Nullable
      @Override
      public BluetoothDevice bluetoothDevice() {
        return null;
      }

      @Override
      public void onToothbrushFound(@NotNull ToothbrushScanResult result) {}

      @Override
      public void onError(int errorCode) {}
    };
  }

  static SpecificToothbrushScanCallback createSpecificCallback(@Nullable BluetoothDevice device) {
    return new SpecificToothbrushScanCallback() {
      @Nullable
      @Override
      public BluetoothDevice bluetoothDevice() {
        return device;
      }

      @Override
      public void onToothbrushFound(@NotNull ToothbrushScanResult result) {}

      @Override
      public void onError(int errorCode) {}
    };
  }

  private KLScanCallback klScanCallback(boolean hasListeners) {
    return klScanCallback(hasListeners, null);
  }

  private KLScanCallback klScanCallback(boolean hasListeners, BluetoothDevice device) {
    KLScanCallback klScanCallback = mock(KLScanCallback.class);

    when(klScanCallback.hasListeners()).thenReturn(hasListeners);
    when(klScanCallback.getBluetoothDevice()).thenReturn(device);

    return klScanCallback;
  }

  private void mockBluetoothState(boolean isEnabled) {
    when(bluetoothUtils.isBluetoothEnabled()).thenReturn(isEnabled);
  }

  private BluetoothDevice storeIsScanningTrueForDevice() {
    BluetoothDevice device = mock(BluetoothDevice.class);

    AtomicBoolean atomicBoolean = bleScannerWrapper.isScanning(device);
    atomicBoolean.set(true);
    return device;
  }
}
