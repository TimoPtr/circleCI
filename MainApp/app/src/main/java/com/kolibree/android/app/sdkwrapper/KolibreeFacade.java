package com.kolibree.android.app.sdkwrapper;

import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.sdkws.core.IKolibreeConnector;
import io.reactivex.Observable;
import io.reactivex.Single;

/** Created by miguelaragues on 12/7/17. */
@Deprecated
public interface KolibreeFacade {

  Single<KolibreeService> service();

  IKolibreeConnector connector();

  Observable<Boolean> connect();
}
