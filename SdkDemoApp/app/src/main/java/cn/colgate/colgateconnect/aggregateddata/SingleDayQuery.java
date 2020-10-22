package cn.colgate.colgateconnect.aggregateddata;

import cn.colgate.colgateconnect.demo.AccountInfo;
import com.kolibree.statsoffline.models.api.AggregatedStatsRepository;
import com.kolibree.statsoffline.persistence.models.StatsSession;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import java.util.List;
import javax.inject.Inject;
import org.threeten.bp.LocalDate;

public class SingleDayQuery implements QueryMode {
  private AggregatedStatsRepository repository;
  private AccountInfo account;
  private LocalDate date;

  @Inject
  public SingleDayQuery(AggregatedStatsRepository repository, AccountInfo account) {
    this.repository = repository;
    this.account = account;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    this.date = date;
  }

  @Override
  public Disposable execute(Consumer<List<StatsSession>> onSuccess, Consumer<Throwable> onError) {
    return repository
        .dayStats(account.getCurrentProfile().getId(), getDate())
        .map(dayAggregatedStatsWithSessions -> dayAggregatedStatsWithSessions.getSessions())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(onSuccess, onError);
  }
}
