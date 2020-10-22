package cn.colgate.colgateconnect.aggregateddata;

import cn.colgate.colgateconnect.demo.AccountInfo;
import com.kolibree.statsoffline.models.DayAggregatedStatsWithSessions;
import com.kolibree.statsoffline.models.MonthAggregatedStatsWithSessions;
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
import org.threeten.bp.YearMonth;

public class MultipleMonthsQuery implements QueryMode {
  private AggregatedStatsRepository repository;
  private AccountInfo account;
  private Set<YearMonth> months;

  @Inject
  public MultipleMonthsQuery(AggregatedStatsRepository repository, AccountInfo account) {
    this.repository = repository;
    this.account = account;
  }

  public Set<YearMonth> getMonths() {
    return months;
  }

  public void setMonths(Set<YearMonth> months) {
    this.months = months;
  }

  @Override
  public Disposable execute(Consumer<List<StatsSession>> onSuccess, Consumer<Throwable> onError) {
    return repository
        .monthStats(account.getCurrentProfile().getId(), getMonths())
        .map(data -> monthsDataToList(data))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onSuccess, onError);
  }

  private List<StatsSession> monthsDataToList(Set<MonthAggregatedStatsWithSessions> sessionSet) {
    List<StatsSession> list = new ArrayList<>();
    for (MonthAggregatedStatsWithSessions sessions : sessionSet) {
      for (DayAggregatedStatsWithSessions session : sessions.getSessionsMap().values()) {
        list.addAll(session.getSessions());
      }
    }
    return list;
  }
}
