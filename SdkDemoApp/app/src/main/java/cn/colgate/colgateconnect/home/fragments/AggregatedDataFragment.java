package cn.colgate.colgateconnect.home.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.OnClick;
import cn.colgate.colgateconnect.R;
import cn.colgate.colgateconnect.aggregateddata.AggregatedDataListActivity;
import cn.colgate.colgateconnect.aggregateddata.MultipleDaysQuery;
import cn.colgate.colgateconnect.aggregateddata.MultipleMonthsQuery;
import cn.colgate.colgateconnect.aggregateddata.PeriodQuery;
import cn.colgate.colgateconnect.aggregateddata.QueryMode;
import cn.colgate.colgateconnect.aggregateddata.SingleDayQuery;
import cn.colgate.colgateconnect.aggregateddata.SingleMonthQuery;
import cn.colgate.colgateconnect.aggregateddata.WeekQuery;
import cn.colgate.colgateconnect.base.BaseViewPagerFragment;
import com.kolibree.android.accountinternal.CurrentProfileProvider;
import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.sdkws.brushing.wrapper.BrushingFacade;
import com.kolibree.sdkws.brushing.wrapper.IBrushing;
import com.kolibree.statsoffline.models.YearWeek;
import com.kolibree.statsoffline.persistence.models.StatsSession;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Inject;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.YearMonth;
import org.threeten.bp.format.DateTimeFormatter;
import timber.log.Timber;

/** AggregatedDataFragment, the 4th tab on the viewpager */
public class AggregatedDataFragment extends BaseViewPagerFragment {

  @Inject SingleDayQuery singleDayQuery;
  @Inject MultipleDaysQuery multipleDaysQuery;
  @Inject SingleMonthQuery singleMonthQuery;
  @Inject MultipleMonthsQuery multipleMonthsQuery;
  @Inject WeekQuery weekQuery;
  @Inject PeriodQuery periodQuery;
  @Inject BrushingFacade brushingFacade;
  @Inject CurrentProfileProvider currentProfileProvider;

  @BindView(R.id.day_button)
  TextView dayButton;

  @BindView(R.id.add_date_button)
  Button addDateButton;

  @BindView(R.id.selected_days)
  TextView selectedDays;

  @BindView(R.id.date_confirm_button)
  Button dateConfirmButton;

  @BindView(R.id.month_button)
  Button monthButton;

  @BindView(R.id.add_month_button)
  Button addMonthButton;

  @BindView(R.id.selected_months)
  TextView selectedMonths;

  @BindView(R.id.months_confirm_button)
  Button monthsConfirmButton;

  @BindView(R.id.period_start_button)
  Button periodStartButton;

  @BindView(R.id.period_end_button)
  Button periodEndButton;

  @BindView(R.id.period_confirm_button)
  Button periodConfirmButton;

  @BindView(R.id.add_week_button)
  Button addWeekButton;

  @BindView(R.id.selected_weeks)
  TextView selectedWeeks;

  @BindView(R.id.weeks_confirm_button)
  Button weeksConfirmButton;

  @BindView(R.id.data_brushing_between_result)
  TextView beginDateView;

  @BindView(R.id.data_brushing_between_begin)
  TextView endDateView;

  @BindView(R.id.data_brushing_between_end)
  TextView resultView;

  private Set<LocalDate> days = new HashSet<>();
  private Set<YearWeek> weeks = new HashSet<>();
  private Set<YearMonth> months = new HashSet<>();
  private LocalDate startDateAggregated;
  private LocalDate endDateAggregated;
  private final AtomicReference<OffsetDateTime> beginDate =
      new AtomicReference<>(OffsetDateTime.now().minusMonths(1));

  private final AtomicReference<OffsetDateTime> endDate =
      new AtomicReference<>(OffsetDateTime.now());

  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflateView(inflater, container, R.layout.fragment_aggregated_data);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    beginDateView.setOnClickListener(v -> setBeginDate());
    endDateView.setOnClickListener(v -> setEndDate());
    view.findViewById(R.id.data_brushing_between_button)
        .setOnClickListener(v -> getAndDisplayBrushingCount());
    refreshDates();
  }

  @OnClick(R.id.day_button)
  void showDatePickDialog() {
    showDatePicker(
        (view, year, month, dayOfMonth) -> {
          dayButton.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
          singleDayQuery.setDate(LocalDate.of(year, month + 1, dayOfMonth));
          queryData(singleDayQuery);
        });
  }

  @OnClick(R.id.add_date_button)
  void onAddDateButtonClicked() {
    showDatePicker(
        (view, year, month, dayOfMonth) -> {
          selectedDays.append("\n" + year + "-" + (month + 1) + "-" + dayOfMonth);
          days.add(LocalDate.of(year, month + 1, dayOfMonth));
        });
  }

  @OnClick(R.id.date_confirm_button)
  void onDateConfirmButtonClicked() {
    multipleDaysQuery.setDates(new HashSet<>(days));
    queryData(multipleDaysQuery);
    cleanSelectedDays();
  }

  private void cleanSelectedDays() {
    days.clear();
    selectedDays.setText(R.string.selected_days);
  }

  @OnClick(R.id.add_week_button)
  void onWeekButtonClicked() {
    showDatePicker(
        (view, year, month, dayOfMonth) -> {
          YearWeek yearWeek = YearWeek.from(LocalDate.of(year, month + 1, dayOfMonth));
          weeks.add(yearWeek);
          selectedWeeks.append("\n" + year + "-" + yearWeek.getWeek() + " week");
        });
  }

  @OnClick(R.id.weeks_confirm_button)
  void onWeeksConfirmButtonClicked() {
    weekQuery.setWeeks(new HashSet<>(weeks));
    queryData(weekQuery);
    cleanSelectedWeeks();
  }

  private void cleanSelectedWeeks() {
    weeks.clear();
    selectedWeeks.setText(R.string.selected_weeks);
  }

  @OnClick(R.id.month_button)
  void onMonthButtonClicked() {
    showDatePicker(
        (view, year, month, dayOfMonth) -> {
          monthButton.setText(year + "-" + (month + 1));
          singleMonthQuery.setMonth(YearMonth.of(year, month + 1));
          queryData(singleMonthQuery);
        });
  }

  @OnClick(R.id.add_month_button)
  void onAddMonthButtonClicked() {
    showDatePicker(
        (view, year, month, dayOfMonth) -> {
          selectedMonths.append("\n" + year + "-" + (month + 1));
          months.add(YearMonth.of(year, month + 1));
        });
  }

  @OnClick(R.id.months_confirm_button)
  void onMonthsConfirmButtonClicked() {
    multipleMonthsQuery.setMonths(new HashSet<>(months));
    queryData(multipleMonthsQuery);
    cleanSelectedMonths();
  }

  private void cleanSelectedMonths() {
    months.clear();
    selectedMonths.setText(R.string.selected_months);
  }

  @OnClick(R.id.period_start_button)
  void onPeriodStartButtonClicked() {
    showDatePicker(
        (view, year, month, dayOfMonth) -> {
          periodStartButton.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
          startDateAggregated = LocalDate.of(year, month + 1, dayOfMonth);
        });
  }

  @OnClick(R.id.period_end_button)
  void onPeriodEndButtonClicked() {
    showDatePicker(
        (view, year, month, dayOfMonth) -> {
          periodEndButton.setText(year + "-" + (month + 1) + "-" + dayOfMonth);
          endDateAggregated = LocalDate.of(year, month + 1, dayOfMonth);
        });
  }

  @OnClick(R.id.period_confirm_button)
  void onPeriodConfirmButtonClicked() {
    if (startDateAggregated != null && endDateAggregated != null) {
      periodQuery.setStartDate(startDateAggregated);
      periodQuery.setEndDate(endDateAggregated);
      queryData(periodQuery);
    } else {
      Toast.makeText(getContext(), "Missing StartDate or EndDate", Toast.LENGTH_LONG).show();
    }
  }

  private void showDatePicker(DatePickerDialog.OnDateSetListener listener) {
    LocalDate currentDate = LocalDate.now();
    DatePickerDialog datePickerDialog =
        new DatePickerDialog(
            getContext(),
            R.style.SDKDatePickerDialogStyle,
            listener,
            currentDate.getYear(),
            currentDate.getMonthValue() - 1,
            currentDate.getDayOfMonth());
    datePickerDialog.show();
  }

  private void showData(List<StatsSession> queryData) {
    if (queryData == null || queryData.isEmpty()) {
      Toast.makeText(getContext(), "No Data", Toast.LENGTH_SHORT).show();
    } else {
      AggregatedDataListActivity.start(getContext(), queryData);
    }
  }

  private void queryData(QueryMode queryMode) {
    if (queryMode == null) {
      return;
    }
    disposables.add(queryMode.execute(this::showData, Throwable::printStackTrace));
  }

  private void refreshDates() {
    beginDateView.setText(beginDate.get().format(DateTimeFormatter.ISO_LOCAL_DATE));
    endDateView.setText(endDate.get().format(DateTimeFormatter.ISO_LOCAL_DATE));
  }

  private void setBeginDate() {
    pickDate(beginDateView, beginDate);
  }

  private void setEndDate() {
    pickDate(endDateView, endDate);
  }

  private void pickDate(
      @NonNull TextView dateView, @NonNull AtomicReference<OffsetDateTime> zonedDateTime) {
    final Context context = getContext();

    if (context != null) {
      new DatePickerDialog(
              getContext(),
              (view, year, monthOfYear, dayOfMonth) -> {
                zonedDateTime.set(
                    zonedDateTime
                        .get()
                        .withYear(year)
                        .withMonth(monthOfYear + 1)
                        .withDayOfMonth(dayOfMonth));
                dateView.setText(zonedDateTime.get().format(DateTimeFormatter.ISO_LOCAL_DATE));
              },
              zonedDateTime.get().getYear(),
              zonedDateTime.get().getMonthValue(),
              zonedDateTime.get().getDayOfMonth())
          .show();
    }
  }

  private void onResultAvailable(@NonNull List<IBrushing> sessions) {
    resultView.setVisibility(View.VISIBLE);
    resultView.setText(
        getString(
            R.string.data_brushing_between_result,
            sessions.size(),
            beginDate.get().format(DateTimeFormatter.ISO_LOCAL_DATE),
            endDate.get().format(DateTimeFormatter.ISO_LOCAL_DATE)));
  }

  private void getAndDisplayBrushingCount() {
    disposables.add(
        currentProfileProvider
            .currentProfileSingle()
            .flatMapObservable(
                (Function<Profile, ObservableSource<List<IBrushing>>>)
                    profile ->
                        brushingFacade.getBrushingSessions(
                            beginDate.get(), endDate.get(), profile.getId()))
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(this::onResultAvailable, Timber::e));
  }
}
