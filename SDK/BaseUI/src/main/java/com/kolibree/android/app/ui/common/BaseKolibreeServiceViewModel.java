package com.kolibree.android.app.ui.common;

import static com.kolibree.android.extensions.DisposableUtils.addSafely;

import android.annotation.SuppressLint;
import androidx.annotation.CallSuper;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModel;
import com.kolibree.android.extensions.DisposableUtils;
import com.kolibree.android.sdk.core.KolibreeService;
import com.kolibree.android.sdk.core.ServiceConnected;
import com.kolibree.android.sdk.core.ServiceProvider;
import com.kolibree.android.sdk.core.ServiceProvisionResult;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * ViewModel that knows how to establish a link with KolibreeService
 *
 * <p>Lifecycle containers of any ViewModel that extends this abstract class must register it as
 * lifecycle observer
 *
 * <p>Created by miguelaragues on 17/11/17.
 */
@Keep
public abstract class BaseKolibreeServiceViewModel extends ViewModel
    implements DefaultLifecycleObserver {

  private final ServiceProvider serviceProvider;

  @SuppressLint("StaticFieldLeak")
  @VisibleForTesting
  protected KolibreeService kolibreeService;

  @VisibleForTesting final CompositeDisposable serviceStateDisposables = new CompositeDisposable();

  @VisibleForTesting Disposable serviceConnectDisposable;

  @VisibleForTesting final CompositeDisposable disposables = new CompositeDisposable();

  protected final void addToDisposables(Disposable disposable) {
    addSafely(disposables, disposable);
  }

  protected BaseKolibreeServiceViewModel(@NonNull ServiceProvider serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  @Override
  public void onStart(@NonNull LifecycleOwner owner) {
    if (serviceConnectDisposable == null || serviceConnectDisposable.isDisposed()) {
      serviceConnectDisposable =
          serviceProvider
              .connectStream()
              .subscribeOn(Schedulers.io())
              .distinctUntilChanged()
              .observeOn(AndroidSchedulers.mainThread())
              .doOnDispose(this::onKolibreeServiceDisconnected)
              .subscribe(this::onServiceConnectionChanged, this::onErrorReadingService);

      DisposableUtils.addSafely(serviceStateDisposables, serviceConnectDisposable);
    }
  }

  @VisibleForTesting
  void onServiceConnectionChanged(ServiceProvisionResult serviceProvisionResult) {
    if (serviceProvisionResult instanceof ServiceConnected) {
      this.kolibreeService = ((ServiceConnected) serviceProvisionResult).getService();

      onKolibreeServiceConnected(((ServiceConnected) serviceProvisionResult).getService());
    } else {
      onKolibreeServiceDisconnected();
    }
  }

  @Override
  public void onStop(@NonNull LifecycleOwner owner) {
    DisposableUtils.addSafely(
        serviceStateDisposables,
        canDisconnectFromService()
            .takeUntil(value -> value)
            .subscribe(
                canDisconnect -> {
                  if (canDisconnect) disconnectFromService();
                },
                Timber::e));
  }

  protected Observable<Boolean> canDisconnectFromService() {
    return Observable.just(true);
  }

  @VisibleForTesting
  void disconnectFromService() {
    serviceStateDisposables.clear();
  }

  @Override
  protected void onCleared() {
    disposables.dispose();

    serviceStateDisposables.dispose();
  }

  /**
   * Called when the Kolibree service becomes available
   *
   * @param service non null {@link KolibreeService}
   */
  @CallSuper
  protected void onKolibreeServiceConnected(@NonNull KolibreeService service) {}

  /** Called when the Kolibree service is no longer available */
  @CallSuper
  protected void onKolibreeServiceDisconnected() {
    kolibreeService = null;
  }

  @CallSuper
  protected void onErrorReadingService(Throwable throwable) {
    Timber.e(throwable);
  }

  /**
   * Get Kolibree service if it is started
   *
   * @return non null {@link KolibreeService} if started, null otherwise
   */
  @Nullable
  @VisibleForTesting(otherwise = VisibleForTesting.PROTECTED)
  public final KolibreeService kolibreeService() {
    return kolibreeService;
  }

  protected final ServiceProvider serviceProvider() {
    return serviceProvider;
  }
}
