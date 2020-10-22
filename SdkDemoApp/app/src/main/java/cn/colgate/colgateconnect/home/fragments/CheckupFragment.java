package cn.colgate.colgateconnect.home.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.OnClick;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.base.BaseViewPagerFragment;
import cn.colgate.colgateconnect.demo.AccountInfo;
import cn.colgate.colgateconnect.orphanbrushings.OrphanBrushingsActivity;
import com.kolibree.android.app.ui.widget.RingChartView;
import com.kolibree.android.extensions.DateExtensionsKt;
import com.kolibree.android.jaws.color.ColorJawsView;
import com.kolibree.android.jaws.color.ColorMouthZones;
import com.kolibree.android.offlinebrushings.persistence.SDKOrphanBrushingRepository;
import com.kolibree.android.processedbrushings.CheckupCalculator;
import com.kolibree.android.processedbrushings.CheckupData;
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade;
import com.kolibree.sdkws.brushing.wrapper.IBrushing;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import java.text.NumberFormat;
import java.util.Locale;
import javax.inject.Inject;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.format.DateTimeFormatter;

/** Third tab with the jaws 3D */
public class CheckupFragment extends BaseViewPagerFragment {

  private static final String DURATION_SEPARATOR = "′";

  // brushing module
  @Inject BrushingFacade brushingFacade;

  // your profile data container, where you will store the data of the user for this session
  @Inject AccountInfo accountInfo;

  @Inject SDKOrphanBrushingRepository orphanBrushingRepository;

  @Inject CheckupCalculator checkupCalculator;

  // jaws 3D module
  @BindView(R.id.fragment_mouth_datetime)
  TextView formattedTimeView;

  @BindView(R.id.fragment_mouth_mouthView)
  ColorJawsView jawsView;

  @BindView(R.id.fragment_mouth_duration_caption)
  TextView durationView;

  @BindView(R.id.fragment_mouth_surface_caption)
  TextView surfaceView;

  @BindView(R.id.fragment_mouth_duration_ring)
  RingChartView durationRingChart;

  @BindView(R.id.fragment_mouth_surface_ring)
  RingChartView surfaceRingChart;

  @BindView(R.id.deleteBrushing)
  ImageView deleteBrushing;

  @BindView(R.id.checkup_navigate_to_orphan_new_content)
  ImageView pendingOrphanBrushingIndicator;

  @BindView(R.id.kpi_speed)
  TextView speedView;

  @BindView(R.id.kpi_angle)
  TextView angleView;

  @BindView(R.id.kpi_movement)
  TextView movementView;

  private IBrushing lastBrushing = null;
  private boolean jawsOpened = true;

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflateView(inflater, container, R.layout.fragment_checkup);
    initView();
    return v;
  }

  @Override
  public void onResume() {
    super.onResume();

    getLastBrushing(accountInfo.getCurrentProfile().getId());
    countOrphanBrushings();
  }

  private void countOrphanBrushings() {
    disposables.add(
        orphanBrushingRepository
            .count()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onOrphanBrushingCount, Throwable::printStackTrace));
  }

  private void onOrphanBrushingCount(Integer count) {
    Log.d(getClass().getSimpleName(), "Received orphan brushing count " + count);
    if (count > 0) {
      pendingOrphanBrushingIndicator.setVisibility(View.VISIBLE);
    } else {
      pendingOrphanBrushingIndicator.setVisibility(View.GONE);
    }
  }

  // LOAD JAWS AND the last brushing session

  private void initView() {
    if (accountInfo.getCurrentProfile() != null) {
      getLastBrushing(accountInfo.getCurrentProfile().getId());
    }

    deleteBrushing.setOnClickListener(
        v -> {
          if (lastBrushing != null) {
            deleteLastBrushing();
          }
        });
  }

  private void deleteLastBrushing() {
    long profileId = accountInfo.getCurrentProfile().getId();
    disposables.add(
        brushingFacade
            .deleteBrushing(lastBrushing)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                () -> {
                  Toast.makeText(
                          CheckupFragment.this.getContext(),
                          "last Brushing has been delete",
                          Toast.LENGTH_LONG)
                      .show();
                  CheckupFragment.this.getLastBrushing(profileId);
                },
                Throwable::printStackTrace));
  }

  /**
   * Get the latest brushing session for a profile using the Brushing module If the latest brushing
   * is not found, it will hide the jaws3D container, Otherwise it will call the jaws 3D and display
   * the latest session
   */
  private void getLastBrushing(long profileId) {
    disposables.add(
        brushingFacade
            .getLastBrushingSession(profileId)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                brushing -> {
                  // there might not be any last brushing
                  if (brushing != null) {
                    displayLastBrushingSession(brushing);
                  } else {
                    noLastBrushing();
                  }
                },
                t -> {
                  t.printStackTrace();
                  noLastBrushing();
                }));
  }

  private void noLastBrushing() {
    jawsView.setVisibility(View.VISIBLE);
    jawsView.setColorMouthZones(ColorMouthZones.gray());
    durationRingChart.setVisibility(View.GONE);
    surfaceRingChart.setVisibility(View.GONE);
  }

  /**
   * Display the data of the latest brushing on the view
   *
   * @param brushing latest brushing
   */
  private void displayLastBrushingSession(IBrushing brushing) {
    this.lastBrushing = brushing;
    jawsView.setVisibility(View.VISIBLE);
    durationRingChart.setVisibility(View.VISIBLE);
    surfaceRingChart.setVisibility(View.VISIBLE);
    jawsView.setColorMouthZones(checkupCalculator.calculateCheckup(brushing).getZoneSurfaceMap());
    jawsView.setOnClickListener(
        v -> {
          if (jawsOpened) {
            jawsView.closeJaws();
          } else {
            jawsView.openJaws();
          }

          jawsOpened = !jawsOpened;
        });

    // Kotlin code sample: val lastSessionDate =
    // getFormattedElapsedTimeSince(brushing.dateTime.toCurrentTimeZone())
    String lastSessionDate =
        getFormattedElapsedTimeSince(DateExtensionsKt.toCurrentTimeZone(brushing.getDateTime()));

    formattedTimeView.setText(lastSessionDate);

    float durationPercentage = (float) brushing.getDuration() / (float) brushing.getGoalDuration();

    durationRingChart.setRingCoverage((int) durationPercentage);
    surfaceRingChart.setRingCoverage(brushingFacade.getQualityBrushing(brushing));

    adjustRingChartColor(durationRingChart, (int) durationPercentage);
    adjustRingChartColor(surfaceRingChart, brushing.getGoalDuration());
    fillSurface(displayableSurface(brushingFacade.getQualityBrushing(brushing)));
    durationView.setText(getFormattedBrushingDuration(brushing.getDuration(), DURATION_SEPARATOR));

    CheckupData checkupData = checkupCalculator.calculateCheckup(brushing);
    speedView.setText("Avg Speed: " + checkupData.speedAverage());
    angleView.setText("Avg Angle: " + checkupData.angleAverage());
    movementView.setText("Avg Movement: " + checkupData.movementAverage());
  }

  /*
  Addresses https://jira.kolibree.com/browse/KLTB002-4718

  Manual brushing sessions do not have brushing data, nor surface data
  A surface of -1 (NO_ZONE_SURFACE) is returned in this case, so we can adapt the UI
  accordingly.
   */
  private int displayableSurface(int qualityBrushing) {
    if (qualityBrushing == CheckupData.NO_ZONE_SURFACE) {
      return 0; // Manual brushing session, no data
    }

    return qualityBrushing;
  }

  @NonNull
  private String getFormattedElapsedTimeSince(@NonNull OffsetDateTime dateTime) {
    return DateTimeFormatter.ofPattern("yyyy MMM dd, hh:mm a").format(dateTime);
  }

  /** Utility method */
  private void adjustRingChartColor(RingChartView ringChart, int percentage) {
    final int PERFECT_PERCENTAGE = 100;
    Context context = getContext();
    if (context != null) {
      final boolean isPerfect = percentage >= PERFECT_PERCENTAGE;
      int colorRes = isPerfect ? R.color.metric_perfect_smooth : R.color.metric_average;
      ringChart.setRingColor(ContextCompat.getColor(context, colorRes));
    }
  }

  private void fillSurface(int surfacePercentage) {
    final NumberFormat numberFormat = NumberFormat.getPercentInstance();
    surfaceView.setText(numberFormat.format(surfacePercentage / 100f));
  }

  /**
   * Get a formatted brushing duration with M\<separator\>ss pattern
   *
   * @param durationSeconds brushing duration in seconds
   * @return non null formatted brushing duration
   */
  @NonNull
  private String getFormattedBrushingDuration(long durationSeconds, String separator) {
    if (durationSeconds > 59L) {
      if (durationSeconds % 60L == 0) {
        return String.format(Locale.getDefault(), "%d" + separator + "00", durationSeconds / 60L);
      } else {
        return String.format(
            Locale.getDefault(),
            "%d" + separator + "%02d",
            durationSeconds / 60L,
            durationSeconds % 60L);
      }
    } else {
      return String.format(Locale.getDefault(), "0" + separator + "%02d", durationSeconds);
    }
  }

  @OnClick(R.id.checkup_navigate_to_orphan_container)
  void onOrphanClicked() {
    OrphanBrushingsActivity.start(getContext());
  }
}
