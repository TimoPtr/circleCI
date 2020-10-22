package cn.colgate.colgateconnect.aggregateddata;

import com.kolibree.statsoffline.persistence.models.StatsSession;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import java.util.List;

public interface QueryMode {
  Disposable execute(Consumer<List<StatsSession>> onSuccess, Consumer<Throwable> onError);
}
