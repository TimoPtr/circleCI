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
import javax.inject.Inject;
import org.threeten.bp.YearMonth;

public class SingleMonthQuery implements QueryMode {
  private AggregatedStatsRepository repository;
  private AccountInfo account;

  private YearMonth month;

  @Inject
  public SingleMonthQuery(AggregatedStatsRepository repository, AccountInfo account) {
    this.repository = repository;
    this.account = account;
  }

  public YearMonth getMonth() {
    return month;
  }

  public void setMonth(YearMonth month) {
    this.month = month;
  }

  @Override
  public Disposable execute(Consumer<List<StatsSession>> onSuccess, Consumer<Throwable> onError) {
    return repository
        .monthStats(account.getCurrentProfile().getId(), getMonth())
        .map(sessions -> monthDataToList(sessions))
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onSuccess, onError);
  }

  private List<StatsSession> monthDataToList(MonthAggregatedStatsWithSessions sessions) {
    List<StatsSession> list = new ArrayList<>();
    for (DayAggregatedStatsWithSessions session : sessions.getSessionsMap().values()) {
      list.addAll(session.getSessions());
    }
    return list;
  }
}
