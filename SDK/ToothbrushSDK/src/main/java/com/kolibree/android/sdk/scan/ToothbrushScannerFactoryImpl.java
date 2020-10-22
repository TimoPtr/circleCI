package com.kolibree.android.sdk.scan;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.util.IBluetoothUtils;
import javax.inject.Inject;

/** Created by miguelaragues on 21/9/17. */
class ToothbrushScannerFactoryImpl implements ToothbrushScannerFactory {

  private final IBluetoothUtils bluetoothUtils;

  @Inject
  ToothbrushScannerFactoryImpl(IBluetoothUtils bluetoothUtils) {
    this.bluetoothUtils = bluetoothUtils;
  }

  /**
   * Get a compatible Bluetooth toothbrush scanner
   *
   * @return a compatible BLE scanner if device is BLE compatible, null otherwise
   */
  @Nullable
  @Override
  public ToothbrushScanner getCompatibleBleScanner() {
    if (bluetoothUtils.deviceSupportsBle()) {
      return NordicBleScannerWrapper.singleton(bluetoothUtils);
    }

    return null;
  }

  @Nullable
  @Override
  public ToothbrushScanner getScanner(@NonNull Context context, @NonNull ToothbrushModel model) {
    return getCompatibleBleScanner();
  }
}
