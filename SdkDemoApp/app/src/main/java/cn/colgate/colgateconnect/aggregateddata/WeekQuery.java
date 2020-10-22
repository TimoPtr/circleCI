package cn.colgate.colgateconnect.aggregateddata;

import cn.colgate.colgateconnect.demo.AccountInfo;
import com.kolibree.statsoffline.models.DayAggregatedStatsWithSessions;
import com.kolibree.statsoffline.models.WeekAggregatedStatsWithSessions;
import com.kolibree.statsoffline.models.YearWeek;
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

public class WeekQuery implements QueryMode {
  private AggregatedStatsRepository repository;
  private AccountInfo account;
  private Set<YearWeek> weeks;

  @Inject
  public WeekQuery(AggregatedStatsRepository repository, AccountInfo account) {
    this.repository = repository;
    this.account = account;
  }

  public Set<YearWeek> getWeeks() {
    return weeks;
  }

  public void setWeeks(Set<YearWeek> weeks) {
    this.weeks = weeks;
  }

  @Override
  public Disposable execute(Consumer<List<StatsSession>> onSuccess, Consumer<Throwable> onError) {
    return repository
        .weekStats(account.getCurrentProfile().getId(), getWeeks())
        .map(sessions -> weeksDataToList(sessions))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onSuccess, onError);
  }

  private List<StatsSession> weeksDataToList(Set<WeekAggregatedStatsWithSessions> sessionSet) {
    List<StatsSession> list = new ArrayList<>();
    for (WeekAggregatedStatsWithSessions sessions : sessionSet) {
      for (DayAggregatedStatsWithSessions session : sessions.getSessionsMap().values()) {
        list.addAll(session.getSessions());
      }
    }
    return list;
  }
}
