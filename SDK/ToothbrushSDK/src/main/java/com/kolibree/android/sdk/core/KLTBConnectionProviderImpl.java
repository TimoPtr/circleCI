package com.kolibree.android.sdk.core;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.kolibree.android.sdk.connection.KLTBConnection;
import com.kolibree.android.sdk.connection.state.ConnectionStateListener;
import com.kolibree.android.sdk.connection.state.KLTBConnectionState;
import com.kolibree.android.sdk.error.InvalidConnectionStateException;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

class KLTBConnectionProviderImpl implements KLTBConnectionProvider {

  private final ServiceProvider serviceProvider;

  @Inject
  KLTBConnectionProviderImpl(ServiceProvider serviceProvider) {
    this.serviceProvider = serviceProvider;
  }

  @VisibleForTesting ConnectionStateListener stateListener;

  /**
   * This method is a best effort at returning an active KLTBConnection
   *
   * <p>1. If there's no service or the service doesn't return any KLTBConnection for the
   * macAddress, returns a Single.error
   *
   * <p>2. If there's an ACTIVE connection, the Single emits that connection
   *
   * <p>3. If there's a connection but it's not ACTIVE, we register as state listeners and the
   * Single emits the connection when it becomes ACTIVE, or errors if it's in TERMINATING or
   * TERMINATED
   *
   * <p>Timeout is not managed by this class, it should be handled by the client
   *
   * @param macAddress the mac address of the toothrbush we want to connect to
   * @return a Single that will emit on Main Thread an KLTBConnection with state ACTIVE, or an error
   */
  @NonNull
  @Override
  public Single<KLTBConnection> existingActiveConnection(@NonNull String macAddress) {
    return existingConnectionWithStates(
        macAddress, Collections.singletonList(KLTBConnectionState.ACTIVE));
  }

  @NotNull
  @Override
  public Single<KLTBConnection> existingConnectionWithStates(
      @NotNull String macAddress, @NotNull List<? extends KLTBConnectionState> acceptedStates) {

    return getKLTBConnectionSingle(macAddress)
        .flatMap(
            connection ->
                Single.<KLTBConnection>create(
                        emitter -> {
                          stateListener =
                              new ConnectionProviderStateListener(emitter, acceptedStates);

                          connection.state().register(stateListener);
                        })
                    .doOnDispose(() -> onUserDisposedConnection(connection))
                    .doFinally(() -> stateListener = null));
  }

  @VisibleForTesting
  void onUserDisposedConnection(@NonNull KLTBConnection connection) {
    if (stateListener != null) {
      connection.state().unregister(stateListener);
    }
  }

  @NonNull
  @Override
  public Single<KLTBConnection> getKLTBConnectionSingle(@NonNull final String macAddress) {
    return serviceProvider
        .connectOnce()
        .flatMap(
            service -> {
              KLTBConnection connection = service.getConnection(macAddress);

              if (connection == null) {
                return Single.error(
                    new Throwable("Unable to connect to the toothbrush with mac " + macAddress));
              }

              return Single.just(connection);
            });
  }

  @VisibleForTesting
  static class ConnectionProviderStateListener implements ConnectionStateListener {

    private final SingleEmitter<KLTBConnection> emitter;
    private final List<? extends KLTBConnectionState> acceptedStates;

    @VisibleForTesting
    ConnectionProviderStateListener(
        SingleEmitter<KLTBConnection> connectionSingleEmitter,
        @NotNull List<? extends KLTBConnectionState> acceptedStates) {
      emitter = connectionSingleEmitter;
      this.acceptedStates = acceptedStates;
    }

    @Override
    public void onConnectionStateChanged(
        @NotNull KLTBConnection connection, @NotNull KLTBConnectionState newState) {
      if (emitter.isDisposed()) {
        unregisterAsStateListener(connection);
        return;
      }

      if (acceptedStates.contains(newState)) {
        emitter.onSuccess(connection);

        unregisterAsStateListener(connection);
      } else if (isInvalidState(newState)) {
        emitter.tryOnError(
            new InvalidConnectionStateException(
                "Excepted connection with state "
                    + acceptedStates
                    + ", was "
                    + newState
                    + ". Disconnected from service"));

        unregisterAsStateListener(connection);
      }
    }

    private boolean isInvalidState(@NotNull KLTBConnectionState newState) {
      return !acceptedStates.contains(newState)
          && (newState == KLTBConnectionState.TERMINATED
              || newState == KLTBConnectionState.TERMINATING);
    }

    private void unregisterAsStateListener(@NotNull KLTBConnection connection) {
      connection.state().unregister(this);
    }
  }
}
