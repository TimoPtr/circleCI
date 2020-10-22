package com.kolibree.android.sdk.scan;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import com.kolibree.android.commons.ToothbrushModel;

/**
 * Created by aurelien on 20/10/16.
 *
 * <p>Model for toothbrush scan result
 */
final class ToothbrushScanResultImpl implements ToothbrushScanResult {

  private final String mac;
  private final String name;
  private final ToothbrushModel model;
  private final long ownerDevice;
  private final boolean seamlessConnectionAvailable;
  private final ToothbrushApp app;

  public ToothbrushScanResultImpl(
      @NonNull String mac,
      @NonNull String name,
      @NonNull ToothbrushModel model,
      long ownerDevice,
      boolean seamlessConnectionAvailable,
      @NonNull ToothbrushApp app) {
    this.mac = mac;
    this.name = name;
    this.model = model;
    this.ownerDevice = ownerDevice;
    this.seamlessConnectionAvailable = seamlessConnectionAvailable;
    this.app = app;
  }

  /**
   * Parcelable constructor
   *
   * @param in non null Parcel
   */
  private ToothbrushScanResultImpl(@NonNull Parcel in) {
    mac = in.readString();
    name = in.readString();
    model = ToothbrushModel.values()[in.readInt()];
    ownerDevice = in.readLong();
    seamlessConnectionAvailable = in.readInt() == 1;
    app = ToothbrushApp.values()[in.readInt()];
  }

  /**
   * Get toothbrush mac address
   *
   * @return non null String-encapsulated mac address
   */
  @Override
  @NonNull
  public String getMac() {
    return mac;
  }

  /**
   * Get toothbrush name
   *
   * @return non null name
   */
  @Override
  @NonNull
  public String getName() {
    return name;
  }

  /**
   * Get toothbrush model
   *
   * @return non null toothbrush model
   */
  @Override
  @NonNull
  public ToothbrushModel getModel() {
    return model;
  }

  /**
   * Get toothbrush owner device
   *
   * <p>This method only applies to V2 toothbrushes
   *
   * @return the owner device of a V2 toothbrush, 0 if V1 one
   */
  @Override
  public long getOwnerDevice() {
    return ownerDevice;
  }

  /**
   * Check if M1 devices are running bootloader
   *
   * @return true if running bootloader, false otherwise
   */
  @Override
  public boolean isRunningBootloader() {
    return app == ToothbrushApp.DFU_BOOTLOADER;
  }

  /**
   * Check if the toothbrush can be seamlessly connected
   *
   * <p>This method applies only to V2 toothbrushes
   *
   * @return true if the toothbrush can be seamless connected, false if not or V1
   */
  @Override
  public boolean isSeamlessConnectionAvailable() {
    return seamlessConnectionAvailable;
  }

  @NonNull
  @Override
  public ToothbrushApp getToothbrushApp() {
    return app;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ToothbrushScanResultImpl) {
      final ToothbrushScanResultImpl another = (ToothbrushScanResultImpl) obj;
      return mac.equals(another.getMac()) && app == another.app;
    }

    return false;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString(mac);
    parcel.writeString(name);
    parcel.writeInt(model.ordinal());
    parcel.writeLong(ownerDevice);
    parcel.writeInt(seamlessConnectionAvailable ? 1 : 0);
    parcel.writeInt(app.ordinal());
  }

  /** Parcelable creator */
  public static final Parcelable.Creator<ToothbrushScanResultImpl> CREATOR =
      new Parcelable.Creator<ToothbrushScanResultImpl>() {
        @Override
        public ToothbrushScanResultImpl createFromParcel(Parcel source) {
          return new ToothbrushScanResultImpl(source);
        }

        @Override
        public ToothbrushScanResultImpl[] newArray(int size) {
          return new ToothbrushScanResultImpl[size];
        }
      };
}
