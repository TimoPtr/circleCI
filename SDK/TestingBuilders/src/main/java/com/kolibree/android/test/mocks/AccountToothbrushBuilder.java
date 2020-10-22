/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.mocks;

import static com.kolibree.android.KolibreeConstKt.SHARED_MODE_PROFILE_ID;
import static com.kolibree.android.test.mocks.AccountToothbrushCreatorKt.createAccountToothbrush;
import static com.kolibree.android.test.mocks.KLTBConnectionBuilder.DEFAULT_BOOTLOADER_VERSION;
import static com.kolibree.android.test.mocks.KLTBConnectionBuilder.DEFAULT_SERIAL;
import static com.kolibree.android.test.mocks.TestConstantsKt.DEFAULT_TEST_ACCOUNT_ID;

import com.kolibree.android.annotation.VisibleForApp;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.persistence.model.AccountToothbrush;
import com.kolibree.android.sdk.version.DspVersion;
import com.kolibree.android.sdk.version.HardwareVersion;
import com.kolibree.android.sdk.version.SoftwareVersion;

/**
 * Created by miguelaragues on 21/2/18.
 *
 * @deprecated Use AccountToothbrushCreator.createAccountToothbrush
 */
@VisibleForApp
@Deprecated
public class AccountToothbrushBuilder {

  private String mac;
  private String name;
  private ToothbrushModel model;
  private long accountId;
  private long profileId;
  private String serial;
  private HardwareVersion hardwareVersion;
  private SoftwareVersion softwareVersion;
  private SoftwareVersion bootloaderVersion;
  private DspVersion dspVersion;

  private AccountToothbrushBuilder() {}

  public static AccountToothbrushBuilder builder() {
    return new AccountToothbrushBuilder();
  }

  public AccountToothbrushBuilder withDefaultState() {
    name = KLTBConnectionBuilder.DEFAULT_NAME;
    model = KLTBConnectionBuilder.DEFAULT_MODEL;
    mac = KLTBConnectionBuilder.DEFAULT_MAC;
    profileId = KLTBConnectionBuilder.DEFAULT_OWNER_ID;
    accountId = DEFAULT_TEST_ACCOUNT_ID;
    serial = DEFAULT_SERIAL;
    hardwareVersion = KLTBConnectionBuilder.DEFAULT_HW_VERSION;
    softwareVersion = KLTBConnectionBuilder.DEFAULT_FW_VERSION;
    bootloaderVersion = DEFAULT_BOOTLOADER_VERSION;
    dspVersion = DspVersion.NULL;

    return this;
  }

  public AccountToothbrushBuilder withProfileId(long profileId) {
    this.profileId = profileId;

    return this;
  }

  public AccountToothbrushBuilder withModel(ToothbrushModel model) {
    this.model = model;

    return this;
  }

  public AccountToothbrushBuilder withMac(String mac) {
    this.mac = mac;

    return this;
  }

  public AccountToothbrushBuilder withIsShared(boolean isShared) {
    if (isShared) {
      return withProfileId(SHARED_MODE_PROFILE_ID);
    }

    return this;
  }

  public AccountToothbrushBuilder withName(String name) {
    this.name = name;

    return this;
  }

  public AccountToothbrushBuilder withDspVersion(DspVersion version) {
    this.dspVersion = version;

    return this;
  }

  public AccountToothbrush build() {
    return createAccountToothbrush(
        mac,
        name,
        serial,
        model,
        softwareVersion,
        bootloaderVersion,
        hardwareVersion,
        dspVersion,
        accountId,
        profileId,
        false);
  }

  public static AccountToothbrush fromConnection(KLTBConnection connection) {
    return createAccountToothbrush(
        connection.toothbrush().getMac(),
        connection.toothbrush().getName(),
        DEFAULT_SERIAL,
        connection.toothbrush().getModel(),
        connection.toothbrush().getFirmwareVersion(),
        connection.toothbrush().getBootloaderVersion(),
        connection.toothbrush().getHardwareVersion(),
        connection.toothbrush().getDspVersion(),
        DEFAULT_TEST_ACCOUNT_ID,
        connection.userMode().profileOrSharedModeId().blockingGet(),
        false);
  }
}
