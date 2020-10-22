package com.kolibree.android.app.ui.navigation;

import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.COACH_PLUS_SCREEN;
import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.COACH_PLUS_SCREEN_MANUAL;
import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.COACH_SCREEN;
import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.COACH_SCREEN_MANUAL;
import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.OFFLINE_BRUSHING_SCREEN;
import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.OTA_UPDATE_SCREEN;
import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.PIRATE_SCREEN;
import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.TEST_ANGLES_SCREEN;
import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.TEST_BRUSHING_SCREEN;
import static com.kolibree.android.app.ui.navigation.MainActivityNavigationController.TOOTHBRUSH_SCREEN;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.commons.ToothbrushModel;
import com.kolibree.android.sdk.connection.KLTBConnection;
import io.reactivex.observers.TestObserver;
import org.junit.Test;

/** Created by miguelaragues on 16/10/17. */
@SuppressWarnings("KotlinInternalInJava")
public class MainActivityNavigationControllerTest extends BaseUnitTest {

  MainActivityNavigationController navigationController;

  @Override
  public void setup() throws Exception {
    super.setup();

    navigationController = spy(new MainActivityNavigationController());
  }

  @Test
  public void navigateToToothbrush_emitsTOOTHBRUSH_SCREEN_withConnection() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();

    observer.assertEmpty();

    KLTBConnection connection = mock(KLTBConnection.class);
    navigationController.navigateToToothbrush(connection);

    observer.assertValue(NavigateAction.create(TOOTHBRUSH_SCREEN, connection));
  }

  @Test
  public void navigateToMyToothbrushes_emitsMY_TOOTHBRUSHES_SCREEN() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();

    observer.assertEmpty();

    navigationController.navigateToMyToothbrushes();

    observer.assertValue(
        NavigateAction.create(MainActivityNavigationController.MY_TOOTHBRUSHES_SCREEN));
  }

  @Test
  public void navigateToSetupToothbrush_emitsSETUP_TOOTHBRUSH_SCREEN() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();

    observer.assertEmpty();

    navigationController.navigateToSetupToothbrush();

    observer.assertValue(
        NavigateAction.create(MainActivityNavigationController.SETUP_TOOTHBRUSH_SCREEN));
  }

  @Test
  public void navigateToConnectionHelpScreen_emitsCONNECTION_HELP_SCREEN() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();

    observer.assertEmpty();

    navigationController.navigateToConnectionHelpScreen();

    observer.assertValue(
        NavigateAction.create(MainActivityNavigationController.CONNECTION_HELP_SCREEN));
  }

  @Test
  public void futureSubscribersDontReceiveNavigateActions() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();

    observer.assertEmpty();

    navigationController.navigateToSetupToothbrush();

    observer.assertValue(
        NavigateAction.create(MainActivityNavigationController.SETUP_TOOTHBRUSH_SCREEN));

    TestObserver<NavigateAction> observer2 = navigationController.navigateActionObservable().test();

    observer2.assertEmpty();
  }

  @Test
  public void navigateToCoachScreen_emitsCOACH_SCREEN_withMac() {
    final String mac = "mac";
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToConnectedCoachScreen(mac);
    observer.assertValue(NavigateAction.create(COACH_SCREEN, mac));
  }

  @Test
  public void navigateToCoachScreen_emitsCOACH_SCREEN_withoutMac() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToConnectedCoachScreen(null);
    observer.assertValue(NavigateAction.create(COACH_SCREEN));
  }

  @Test
  public void navigateToManualCoachScreen_emitsCOACH_PLUS_SCREEN_MANUAL() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToManualCoachScreen();
    observer.assertValue(NavigateAction.create(COACH_SCREEN_MANUAL));
  }

  @Test
  public void navigateToManualCoachPlusScreen_emitsCOACH_PLUS_SCREEN_MANUAL() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToManualCoachPlusScreen();
    observer.assertValue(NavigateAction.create(COACH_PLUS_SCREEN_MANUAL));
  }

  @Test
  public void navigateToConnectedCoachPlusScreen_emitsCOACH_PLUS_SCREEN_withMac_withModel() {
    final String mac = "mac";
    final ToothbrushModel model = ToothbrushModel.ARA;
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToConnectedCoachPlusScreen(mac, model);
    observer.awaitCount(1);
    observer.assertValue(navigateAction -> navigateAction.screenId() == COACH_PLUS_SCREEN);
  }

  @Test
  public void navigateToCoachPlusScreen_emitsCOACH_PLUS_SCREEN_withoutMac_withoutModel() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToConnectedCoachPlusScreen(null, null);
    observer.assertValue(navigateAction -> navigateAction.screenId() == COACH_PLUS_SCREEN);
  }

  @Test
  public void navigateToSbaScreen_emitsTEST_BRUSHING_SCREEN() {
    final String mac = "mac";
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToTestBrushingScreen(mac, false);
    observer.assertValue(NavigateAction.create(TEST_BRUSHING_SCREEN, mac, false));
  }

  @Test
  public void navigateToPirateScreen_emitsPIRATE_SCREEN() {
    final String mac = "mac";
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToPirateScreen(mac, ToothbrushModel.ARA, false);
    observer.assertValue(
        value -> {
          return value.screenId() == PIRATE_SCREEN;
        });
  }

  @Test
  public void navigateToOfflineBrushing_emitsOFFLINE_BRUSHING_SCREEN() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToOfflineBrushing();
    observer.assertValue(NavigateAction.create(OFFLINE_BRUSHING_SCREEN));
  }

  @Test
  public void navigateToTestAngles_emitsTEST_ANGLES_SCREEN() {
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToTestAnglesScreen(null, null, true);
    observer.assertValue(
        value -> {
          return value.screenId() == TEST_ANGLES_SCREEN;
        });
  }

  @Test
  public void navigateToOtaUpdate_emitOTA_UPDATE_SCREEN() {
    final String mac = "mac";
    TestObserver<NavigateAction> observer = navigationController.navigateActionObservable().test();
    observer.assertEmpty();
    navigationController.navigateToOtaUpdate(mac, ToothbrushModel.CONNECT_E2);
    observer.assertValue(navigateAction -> navigateAction.screenId() == OTA_UPDATE_SCREEN);
  }
}
