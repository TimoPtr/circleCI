package cn.colgate.colgateconnect.aggregateddata;

import cn.colgate.colgateconnect.demo.AccountInfo;
import com.kolibree.statsoffline.models.DayAggregatedStatsWithSessions;
import com.kolibree.statsoffline.models.api.AggregatedStatsRepository;
import com.kolibree.statsoffline.persistence.models.StatsSession;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import org.threeten.bp.LocalDate;

public class PeriodQuery implements QueryMode {
  private AggregatedStatsRepository repository;
  private AccountInfo account;
  private LocalDate startDate;
  private LocalDate endDate;

  @Inject
  public PeriodQuery(AggregatedStatsRepository repository, AccountInfo account) {
    this.repository = repository;
    this.account = account;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    this.startDate = startDate;
  }

  public LocalDate getEndDate() {
    return endDate;
  }

  public void setEndDate(LocalDate endDate) {
    this.endDate = endDate;
  }

  @Override
  public Disposable execute(Consumer<List<StatsSession>> onSuccess, Consumer<Throwable> onError) {
    return repository
        .periodStats(account.getCurrentProfile().getId(), getStartDate(), getEndDate())
        .map(data -> daysDataToList(data.getDayAggregatedStats()))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onSuccess, onError);
  }

  private List<StatsSession> daysDataToList(Set<DayAggregatedStatsWithSessions> sessions) {
    List<StatsSession> list = new ArrayList<>();
    for (DayAggregatedStatsWithSessions session : sessions) {
      list.addAll(session.getSessions());
    }
    return list;
  }
}
