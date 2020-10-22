package com.kolibree.android.sdk.core.ota.kltb002.updater;

import static com.kolibree.android.sdk.core.ota.kltb002.updater.FastGruWriter.COMMAND_ID_FAST_GRU_UPDATE;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.sdk.core.driver.ble.BleDriver;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.junit.Test;
import org.mockito.Mock;
import timber.log.Timber;

@SuppressWarnings("KotlinInternalInJava")
public class FastGruWriterTest extends BaseUnitTest {

  @Mock BleDriver driver;

  private FastGruWriter updater;

  @Override
  public void setup() throws Exception {
    super.setup();

    updater = spy(new FastGruWriter(driver, Schedulers.io(), 0));
  }

  @Test
  public void test() {
    Observable.empty()
        .concatWith(Observable.just(1))
        .subscribe(value -> Timber.d("Emitting " + value), Throwable::printStackTrace);
  }

  /*
  VALIDATE UPDATE PRECONDITIONS OBSERVABLE
   */

  @Test
  public void validateUpdatePreconditionsObservable_returnsEmptyObservable() {
    updater.validateUpdatePreconditionsObservable().test().assertComplete();
  }

  /*
  PREPARE TO START UPDATE
   */

  @Test
  public void prepareToStartUpdate_doesNothing() throws Exception {
    verifyNoMoreInteractions(updater);
  }

  /*
  GET START OTA COMMAND ID
   */
  @Test
  public void getStartOTACommandId_otaTypeFirmware_returnsCOMMAND_ID_FAST_FW_UPDATE() {
    assertEquals(COMMAND_ID_FAST_GRU_UPDATE, updater.getStartOTACommandId());
  }
}
