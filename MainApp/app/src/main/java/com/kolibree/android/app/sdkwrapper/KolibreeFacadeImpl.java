package com.kolibree.android.app.sdkwrapper;

import androidx.lifecycle.LifecycleObserver;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.sdkws.core.IKolibreeConnector;
import io.reactivex.Observable;
import io.reactivex.Single;
import javax.inject.Inject;

/**
 * Created by miguelaragues on 12/7/17.
 *
 * <p>Update 20/8/2018
 *
 * <p>This class remains for compatibility reasons, otherwise we'd have to update dozens of classes
 * and inject the dependencies on each one. By hiding the providers behind a facade, we simplify the
 * needed knowledge
 */
class KolibreeFacadeImpl implements KolibreeFacade, LifecycleObserver { // TODO AutoCloseable?

  private final ServiceProvider serviceProvider;
  private final IKolibreeConnector connector;

  @Inject
  KolibreeFacadeImpl(ServiceProvider serviceProvider, IKolibreeConnector connector) {
    this.serviceProvider = serviceProvider;
    this.connector = connector;
  }

  @Override
  public Single<KolibreeService> service() {
    return serviceProvider.connectOnce();
  }

  @Override
  public Observable<Boolean> connect() {
    return serviceProvider.connect();
  }

  @Override
  public IKolibreeConnector connector() {
    return connector;
  }
}
