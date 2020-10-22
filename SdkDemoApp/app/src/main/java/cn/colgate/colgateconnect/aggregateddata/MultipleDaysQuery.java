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

public class MultipleDaysQuery implements QueryMode {
  private AggregatedStatsRepository repository;
  private AccountInfo account;
  private Set<LocalDate> dates;

  @Inject
  public MultipleDaysQuery(AggregatedStatsRepository repository, AccountInfo account) {
    this.repository = repository;
    this.account = account;
  }

  public Set<LocalDate> getDates() {
    return dates;
  }

  public void setDates(Set<LocalDate> dates) {
    this.dates = dates;
  }

  @Override
  public Disposable execute(Consumer<List<StatsSession>> onSuccess, Consumer<Throwable> onError) {
    return repository
        .dayStats(account.getCurrentProfile().getId(), getDates())
        .map(data -> daysDataToList(data))
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
