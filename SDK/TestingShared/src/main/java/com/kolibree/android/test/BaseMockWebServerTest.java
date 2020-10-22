package com.kolibree.android.test;

import androidx.annotation.CallSuper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.commons.gson.LocalDateTypeAdapter;
import com.kolibree.android.commons.gson.OffsetDateTimeTypeAdapter;
import com.kolibree.android.commons.gson.ZonedDateTimeTypeAdapter;
import com.kolibree.android.test.extensions.TrustedClockExtensionsKt;
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZonedDateTime;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

public abstract class BaseMockWebServerTest<T> extends BaseInstrumentationTest {

  private static final Timber.Tree TEST_TREE =
      new Timber.DebugTree() {
        @Override
        protected void log(int priority, String tag, String message, Throwable t) {
          System.out.println(message);
          // else do nothing
        }
      };

  @Rule
  public final UnitTestImmediateRxSchedulersOverrideRule mOverrideSchedulersRule =
      new UnitTestImmediateRxSchedulersOverrideRule();

  protected final MockWebServer mockWebServer = new MockWebServer();
  protected final OkHttpClient okHttpClient = new OkHttpClient();

  protected final Gson gson =
      new GsonBuilder()
          .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
          .registerTypeAdapter(LocalDate.class, new LocalDateTypeAdapter())
          .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
          .registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeTypeAdapter())
          .create();

  private final Retrofit retrofit =
      new Retrofit.Builder()
          .baseUrl(mockWebServer.url("/").toString())
          .client(okHttpClient)
          .addConverterFactory(GsonConverterFactory.create(gson))
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .build();

  protected BaseMockWebServerTest() {
    if (Timber.treeCount() == 0) {
      System.out.println("planting tree");
      Timber.plant(TEST_TREE);
    }
  }

  @Before
  @CallSuper
  public void setUp() throws Exception {
    super.setUp();
  }

  @After
  @CallSuper
  public void tearDown() {
    super.tearDown();
    TrustedClockExtensionsKt.reset(TrustedClock.INSTANCE);

    try {
      mockWebServer.shutdown();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected T retrofitService() {
    return retrofit.create(retrofitServiceClass());
  }

  protected abstract Class<T> retrofitServiceClass();
}
