/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.navigation;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import com.jakewharton.rxrelay2.PublishRelay;
import com.kolibree.android.app.dagger.scopes.ActivityScope;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.connection.KLTBConnection;
import io.reactivex.Observable;
import javax.inject.Inject;

/**
 * This class acts as navigation controller for MainActivity
 *
 * <p>In the future, we should also handle onActivityResult events so that we can test the behavior
 *
 * <p>Created by miguelaragues on 14/12/17.
 */
@SuppressLint("DeobfuscatedPublicSdkClass")
@ActivityScope
@Deprecated
public class MainActivityNavigationController {

  public static final int MY_TOOTHBRUSHES_SCREEN = 1;
  public static final int TOOTHBRUSH_SCREEN = 2;
  public static final int SETUP_TOOTHBRUSH_SCREEN = 3;
  public static final int DASHBOARD_DETAILS_SCREEN = 4;
  public static final int CHECKUP_SCREEN = 5;
  public static final int OTA_UPDATE_SCREEN = 6;
  public static final int WELCOME_SCREEN = 7;
  public static final int SAVE_DATA_BY_EMAIL_SCREEN = 8;
  public static final int CONNECTION_HELP_SCREEN = 9;
  public static final int COACH_SCREEN = 10;
  public static final int PIRATE_SCREEN = 12;
  public static final int COACH_PLUS_SCREEN = 13;
  public static final int COACH_SCREEN_MANUAL = 14;
  public static final int COACH_PLUS_SCREEN_MANUAL = 15;
  public static final int TEST_BRUSHING_SCREEN = 16;
  public static final int GRANT_LOCATION_SCREEN = 17;
  public static final int OFFLINE_BRUSHING_SCREEN = 18;
  public static final int TEST_ANGLES_SCREEN = 19;
  public static final int SPEED_CONTROL_SCREEN = 20;
  private final PublishRelay<NavigateAction> navigateActionRelay = PublishRelay.create();
  private final Observable<NavigateAction> navigateActionObservable =
      navigateActionRelay.publish().autoConnect();

  @Inject
  MainActivityNavigationController() {}

  public Observable<NavigateAction> navigateActionObservable() {
    return navigateActionObservable;
  }

  public void navigateToToothbrush(KLTBConnection connection) {
    navigateActionRelay.accept(NavigateAction.create(TOOTHBRUSH_SCREEN, connection));
  }

  public void navigateToMyToothbrushes() {
    navigateActionRelay.accept(NavigateAction.create(MY_TOOTHBRUSHES_SCREEN));
  }

  public void navigateToSetupToothbrush() {
    navigateActionRelay.accept(NavigateAction.create(SETUP_TOOTHBRUSH_SCREEN));
  }

  public void navigateToDashboardDetails(int detailScreen) {
    navigateActionRelay.accept(NavigateAction.create(DASHBOARD_DETAILS_SCREEN, detailScreen));
  }

  public void navigateToCheckupScreen() {
    navigateActionRelay.accept(NavigateAction.create(CHECKUP_SCREEN));
  }

  public void navigateToOtaUpdate(String mac, ToothbrushModel model) {
    Bundle bundle = new Bundle();
    bundle.putString(NavigateAction.EXTRA_MAC, mac);
    bundle.putSerializable(NavigateAction.EXTRA_MODEL, model);
    navigateActionRelay.accept(NavigateAction.create(OTA_UPDATE_SCREEN, bundle));
  }

  public void navigateToWelcomeScreen() {
    navigateActionRelay.accept(NavigateAction.create(WELCOME_SCREEN));
  }

  public void navigateToSaveDataByEmailScreen() {
    navigateActionRelay.accept(NavigateAction.create(SAVE_DATA_BY_EMAIL_SCREEN));
  }

  public void navigateToConnectionHelpScreen() {
    navigateActionRelay.accept(NavigateAction.create(CONNECTION_HELP_SCREEN));
  }

  public void navigateToConnectedCoachScreen(@Nullable String mac) {
    navigateActionRelay.accept(NavigateAction.create(COACH_SCREEN, mac));
  }

  public void navigateToManualCoachScreen() {
    navigateActionRelay.accept(NavigateAction.create(COACH_SCREEN_MANUAL, null));
  }

  public void navigateToConnectedCoachPlusScreen(
      @Nullable String mac, @Nullable ToothbrushModel model) {
    Bundle bundle = new Bundle();
    bundle.putString(NavigateAction.EXTRA_MAC, mac);
    bundle.putSerializable(NavigateAction.EXTRA_MODEL, model);
    navigateActionRelay.accept(NavigateAction.create(COACH_PLUS_SCREEN, bundle, false));
  }

  public void navigateToManualCoachPlusScreen() {
    navigateActionRelay.accept(NavigateAction.create(COACH_PLUS_SCREEN_MANUAL, null));
  }

  public void navigateToTestBrushingScreen(@Nullable String mac, boolean multiToothbrushes) {
    navigateActionRelay.accept(NavigateAction.create(TEST_BRUSHING_SCREEN, mac, multiToothbrushes));
  }

  public void navigateToPirateScreen(
      @Nullable String mac, @Nullable ToothbrushModel model, boolean multiToothbrushes) {
    Bundle bundle = new Bundle();
    bundle.putString(NavigateAction.EXTRA_MAC, mac);
    bundle.putSerializable(NavigateAction.EXTRA_MODEL, model);
    bundle.putBoolean(NavigateAction.EXTRA_MULTI_TB, multiToothbrushes);
    navigateActionRelay.accept(NavigateAction.create(PIRATE_SCREEN, bundle));
  }

  public void navigateToGrantLocation() {
    navigateActionRelay.accept(NavigateAction.create(GRANT_LOCATION_SCREEN));
  }

  public void navigateToOfflineBrushing() {
    navigateActionRelay.accept(NavigateAction.create(OFFLINE_BRUSHING_SCREEN));
  }

  public void navigateToTestAnglesScreen(
      @Nullable String mac, @Nullable ToothbrushModel model, boolean multiToothbrushes) {
    Bundle bundle = new Bundle();
    bundle.putString(NavigateAction.EXTRA_MAC, mac);
    bundle.putSerializable(NavigateAction.EXTRA_MODEL, model);
    bundle.putBoolean(NavigateAction.EXTRA_MULTI_TB, multiToothbrushes);
    navigateActionRelay.accept(NavigateAction.create(TEST_ANGLES_SCREEN, bundle));
  }

  public void navigateToSpeedControlScreen(
      @Nullable String mac, @Nullable ToothbrushModel model, boolean multiToothbrushes) {
    Bundle bundle = new Bundle();
    bundle.putString(NavigateAction.EXTRA_MAC, mac);
    bundle.putSerializable(NavigateAction.EXTRA_MODEL, model);
    bundle.putBoolean(NavigateAction.EXTRA_MULTI_TB, multiToothbrushes);
    navigateActionRelay.accept(NavigateAction.create(SPEED_CONTROL_SCREEN, bundle));
  }
}
