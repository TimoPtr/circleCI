package cn.colgate.colgateconnect.home.fragments;

import static cn.colgate.colgateconnect.home.MainActivity.REQUEST_CODE_GAME;
import static com.kolibree.android.pirate.PirateCompatActivityKt.createPirateIntent;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.base.BaseViewPagerFragment;
import cn.colgate.colgateconnect.demo.AccountInfo;
import cn.colgate.colgateconnect.home.MainActivity;
import com.kolibree.android.angleandspeed.speedcontrol.mvi.SpeedControlActivityKt;
import com.kolibree.android.angleandspeed.testangles.mvi.TestAnglesActivityKt;
import com.kolibree.android.brushingquiz.presentation.BrushingProgramActivityKt;
import com.kolibree.android.coachplus.CoachPlusFactory;
import com.kolibree.android.guidedbrushing.GuidedBrushingFactory;
import com.kolibree.android.sba.testbrushing.TestBrushingActivity;
import com.kolibree.android.sdk.connection.brushingmode.BrushingMode;
import com.kolibree.android.sdk.connection.brushingmode.BrushingModeSetting;
import com.kolibree.android.sdk.wrapper.ToothbrushFacade;
import com.kolibree.pairing.session.PairingSession;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import javax.inject.Inject;
import org.greenrobot.eventbus.EventBus;

/** Brushing fragment, the 2nd tab on the viewpager */
public class BrushingFragment extends BaseViewPagerFragment {

  private final CompositeDisposable disposables = new CompositeDisposable();

  @Inject AccountInfo accountInfo;
  @Inject CoachPlusFactory coachPlusFactory;
  @Inject GuidedBrushingFactory guidedBrushingFactory;
  @Inject BrushingModeSetting brushingModeSetting;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflateView(inflater, container, R.layout.fragment_activities);
    v.findViewById(R.id.coach_plus).setOnClickListener(v1 -> displayCoachGame());
    v.findViewById(R.id.guided_brushing).setOnClickListener(v1 -> displayGuidedBrushing());
    v.findViewById(R.id.pirate).setOnClickListener(v1 -> displayPirateGameCompat());
    v.findViewById(R.id.launch_test_brushing).setOnClickListener(v1 -> displayTestBrushing());
    v.findViewById(R.id.quiz).setOnClickListener(v1 -> displayQuiz());
    v.findViewById(R.id.brushing_mode).setOnClickListener(v1 -> showBrushingMode());
    v.findViewById(R.id.test_your_angle).setOnClickListener(v1 -> displayTestYourAngle());
    v.findViewById(R.id.speed_control).setOnClickListener(v1 -> displaySpeedControl());

    return v;
  }

  @Override
  public void onDestroyView() {
    disposables.clear();
    super.onDestroyView();
  }

  /**
   * action when clicking the coach + button. Will launch the coach+ if there is a connected
   * toothbrush, otherwise start manual Coach+
   */
  private void displayCoachGame() {
    if (getContext() != null) {
      PairingSession session = accountInfo.getPairingSession();
      Intent intent;
      if (session != null && getContext() != null) {
        ToothbrushFacade toothbrush = session.toothbrush();
        intent =
            coachPlusFactory.createConnectedCoach(
                getContext(), toothbrush.getMac(), toothbrush.getModel(), null);
      } else {
        intent = coachPlusFactory.createManualCoach(getContext(), null);
      }
      /*
      you should use startActivityWithResult if you wish to know if the session has been recorded
       */
      startActivityForResult(intent, REQUEST_CODE_GAME);
    }
  }

  /**
   * action when clicking the guided brushing button. Will launch the guided brushing if there is a
   * connected toothbrush, otherwise start manual guided brushing
   */
  private void displayGuidedBrushing() {
    if (getContext() != null) {
      PairingSession session = accountInfo.getPairingSession();
      Intent intent;
      if (session != null && getContext() != null) {
        ToothbrushFacade toothbrush = session.toothbrush();
        intent =
            guidedBrushingFactory.createConnectedGuidedBrushing(
                getContext(), toothbrush.getMac(), toothbrush.getModel(), null);
      } else {
        intent = guidedBrushingFactory.createManualGuidedBrushing(getContext(), null);
      }
      /*
      you should use startActivityWithResult if you wish to know if the session has been recorded
       */
      startActivityForResult(intent, REQUEST_CODE_GAME);
    }
  }

  /**
   * action when clicking the Test brushing button. Will launch the Test brushing if there is a
   * connected toothbrush, otherwise display a message.
   */
  private void displayTestBrushing() {
    if (getContext() != null) {
      PairingSession session = accountInfo.getPairingSession();
      Intent intent;
      if (session != null && getContext() != null) {
        intent =
            TestBrushingActivity.createIntent(
                getContext(), session.toothbrush().getMac(), session.toothbrush().getModel());

        startActivity(intent);
      } else {
        Toast.makeText(
                getContext(), "Test brushing needs a connected toothbrush", Toast.LENGTH_LONG)
            .show();
      }
    }
  }

  private void displayPirateGameCompat() {
    PairingSession session = accountInfo.getPairingSession();
    if (session != null && getContext() != null) {
      Boolean assetBundleActivityLaunched =
          ((MainActivity) getContext()).assetBundleActivityLaunched;
      if (assetBundleActivityLaunched != null && assetBundleActivityLaunched) {
        Toast.makeText(
                getContext(),
                "You already launched an asset bundle version of Unity integration in this app instance. "
                    + "This will cause issues with Unity. "
                    + "Please kill the app and open Pirate Compat again.",
                Toast.LENGTH_LONG)
            .show();
      } else {
        ((MainActivity) getContext()).assetBundleActivityLaunched = false;
        Intent intent =
            createPirateIntent(
                getContext(),
                session.toothbrush().getModel(),
                session.toothbrush().getMac(),
                MainActivity.class);
        startActivity(intent);
      }
    } else {
      Toast.makeText(getContext(), R.string.no_tb_connected, Toast.LENGTH_LONG).show();
    }
  }

  private void displayQuiz() {
    /*
    To us, Quiz is only visible if user has a toothbrush that supports changing vibration speed.

    Here we always display it but only allow to enter it if there's an E2 or B1 paired

    You can use connection.toothbrush().model().supportsVibrationSpeedUpdate()
     */
    if (getContext() != null) {
      PairingSession session = accountInfo.getPairingSession();
      if (session != null
          && session.connection() != null
          && session.connection().toothbrush().getModel().supportsVibrationSpeedUpdate()) {
        startActivity(BrushingProgramActivityKt.createBrushingProgramIntent(getContext()));
      } else {
        Toast.makeText(
                getContext(),
                "Brushing Quiz needs a connected toothbrush that supports vibration speed update",
                Toast.LENGTH_LONG)
            .show();
      }
    }
  }

  private void showBrushingMode() {
    if (getContext() != null) {
      PairingSession session = accountInfo.getPairingSession();
      if (session != null
          && session.connection() != null
          && session.connection().toothbrush().getModel().supportsVibrationSpeedUpdate()) {
        disposables.add(
            session
                .connection()
                .userMode()
                .profileId()
                .map(id -> brushingModeSetting.getBrushingMode(id))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    mode ->
                        showBrushingModeDialog(
                            mode.getProfileId(), mode.getBrushingMode().name())));
      } else {
        Toast.makeText(
                getContext(),
                "BrushingMode needs a connected toothbrush that supports multi brushing mode",
                Toast.LENGTH_LONG)
            .show();
      }
    }
  }

  private final String[] brushingModeTitles = {
    BrushingMode.Regular.name(), BrushingMode.Slow.name(), BrushingMode.Strong.name()
  };

  private void showBrushingModeDialog(long id, String title) {
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Current mode is " + title);
    builder.setItems(
        brushingModeTitles, (dialog, which) -> setBrushingMode(id, BrushingMode.values()[which]));
    builder.show();
  }

  private void setBrushingMode(long id, BrushingMode mode) {
    disposables.add(
        brushingModeSetting.setBrushingMode(id, mode).subscribeOn(Schedulers.io()).subscribe());
  }

  private void displayTestYourAngle() {
    if (getContext() != null) {
      PairingSession session = accountInfo.getPairingSession();
      if (session != null && session.connection() != null) {
        startActivity(
            TestAnglesActivityKt.createTestAnglesIntent(
                getContext(), session.toothbrush().getMac(), session.toothbrush().getModel()));
      } else {
        Toast.makeText(getContext(), "Test angles needs a connected toothbrush", Toast.LENGTH_LONG)
            .show();
      }
    }
  }

  private void displaySpeedControl() {
    if (getContext() != null) {
      PairingSession session = accountInfo.getPairingSession();
      if (session != null && session.connection() != null) {
        startActivity(
            SpeedControlActivityKt.createSpeedControlIntent(
                getContext(), session.toothbrush().getMac(), session.toothbrush().getModel()));
      } else {
        Toast.makeText(getContext(), "Test angles needs a connected toothbrush", Toast.LENGTH_LONG)
            .show();
      }
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (EventBus.getDefault().isRegistered(this)) {
      EventBus.getDefault().unregister(this);
    }
  }
}
