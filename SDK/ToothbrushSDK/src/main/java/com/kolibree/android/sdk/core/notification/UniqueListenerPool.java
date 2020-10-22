package com.kolibree.android.sdk.core.notification;

import static com.kolibree.android.TimberTagKt.bluetoothTagFor;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import androidx.annotation.AnyThread;
import androidx.annotation.NonNull;
import com.kolibree.android.annotation.VisibleForApp;
import com.kolibree.android.collection.UniquePool;
import kotlin.Unit;
import timber.log.Timber;

/**
 * Created by aurelien on 09/08/17.
 *
 * <p>Generic Kolibree connections events listener pool
 *
 * <p>Handles listeners registration and ensures that all listeners will get the same value for
 * every event and thread safe data transmissions.
 *
 * <p>Listeners can be notified on the main thread or on a dedicatedThread one, this prevents events
 * bottlenecks in case of bad SDK usage (long operations in listeners callbacks)
 *
 * <p>Courtesy of miguelaragues
 *
 * @param <LT> Listener type
 */
@AnyThread
@VisibleForApp
public final class UniqueListenerPool<LT> implements ListenerPool<LT> {

  /** Log tag */
  private static final String TAG = bluetoothTagFor(UniqueListenerPool.class);

  /** Listener pool */
  private final UniquePool<LT> listeners;

  /** Pool notifying target thread (UI thread or dedicatedThread one) */
  private final boolean shouldInvokeOnUi;

  private final String name;

  /** Handler thread to use when */
  private HandlerThread dedicatedThread;

  /** Thread handler */
  private Handler handler;

  /**
   * ListenerPool constructor
   *
   * @param notifyOnMainThread true to send the notifications on the main thread
   */
  public UniqueListenerPool(@NonNull String name, boolean notifyOnMainThread) {
    this.name = name;
    shouldInvokeOnUi = notifyOnMainThread;
    listeners = new UniquePool<>();
  }

  /**
   * Add a listener to the pool
   *
   * @param listener non null listener
   * @return int listener count
   */
  @Override
  public int add(@NonNull LT listener) {
    synchronized (listeners) {
      int size = listeners.add(listener);

      // Create a handler if there was no one listening before
      if (handler == null) {
        createHandler();
      }

      return size;
    }
  }

  /**
   * Remove a listener from the pool
   *
   * @param listener non null listener
   * @return int listener count
   */
  @Override
  public int remove(@NonNull LT listener) {
    synchronized (listeners) {
      final int count = listeners.remove(listener);

      // No one left, release resources
      if (handler != null && count == 0) {
        deleteHandler();
      }

      return count;
    }
  }

  /**
   * Notify all listeners on event
   *
   * @param listenerNotifier non null ListenerNotifier implementation
   */
  @Override
  public void notifyListeners(@NonNull final ListenerNotifier<LT> listenerNotifier) {
    if (hasListeners()) {
      if (handler != null) {
        handler.post(
            new Runnable() {
              @Override
              public void run() {
                final UniquePool<LT> copyOfListenersAtInstant = new UniquePool<>();
                synchronized (listeners) {
                  /*
                  We were sometimes receiving ConcurrentModificationException in this method

                  My assumption is that listenerNotifier.notifyListener's recipient (the actual ref.get()),
                  was somehow getting back to this ListenerPool instance and modifying the listeners list.
                  Since we had already acquired the lock, the invoker is granted access into the
                  synchronized section and can add/remove from the list, which causes the exception
                  aforementioned

                  By copying the list, we make sure that the collection we are iterating over does not
                  get modified. Plus, any listener attempting to modify listeners list will have to
                  acquire the lock.

                  See https://fabric.io/antoine-piellards-projects/android/apps/com.kolibree.datapp/issues/59ca9a72be077a4dcc4a0617
                   */
                  copyOfListenersAtInstant.addAll(listeners);
                }

                copyOfListenersAtInstant.forEach(
                    listener -> {
                      listenerNotifier.notifyListener(listener);
                      return Unit.INSTANCE;
                    });

                /*
                In this block, we use the instance field listeners
                */
                boolean shouldDeleteHandler;
                synchronized (listeners) {
                  // If there was only leaking listeners we clean resources
                  // avoid invoking method inside synchronized block
                  shouldDeleteHandler = listeners.isEmpty();
                }

                if (shouldDeleteHandler) {
                  deleteHandler();
                }
              }
            });
      } else {
        Timber.tag(logTag()).w("Notified but no handler, ignoring data in %s", this);
      }
    }
  }

  private synchronized boolean hasListeners() {
    return !listeners.isEmpty();
  }

  /** Create a handler to notify listeners on another thread than the driver's one */
  private void createHandler() {
    if (shouldInvokeOnUi) {
      handler = new Handler(Looper.getMainLooper());
    } else {
      dedicatedThread = new HandlerThread(TAG + System.currentTimeMillis()); // Dummy name
      dedicatedThread.start();
      handler = new Handler(dedicatedThread.getLooper());
    }
  }

  /** Close the handler and quit the notifier thread if any */
  private void deleteHandler() {
    handler = null;

    if (dedicatedThread != null) {
      dedicatedThread.quit();
      dedicatedThread = null;
    }
  }

  @Override
  public final int size() {
    synchronized (listeners) {
      return listeners.size();
    }
  }

  private String logTag() {
    return getClass().getSimpleName() + "-" + name;
  }
}
