package com.kolibree.sdkws.data.database;

import static org.mockito.Mockito.*;

import androidx.annotation.NonNull;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.sdkws.brushing.persistence.repo.BrushingsRepository;
import com.kolibree.sdkws.data.model.gopirate.GoPirateDatastore;
import com.kolibree.sdkws.internal.OfflineUpdateDatastore;
import io.reactivex.Completable;
import org.junit.Test;
import org.mockito.Mock;

public class ApiSDKDatabaseInteractorImplTest extends BaseUnitTest {

  @Mock BrushingsRepository brushingsRepository;

  @SuppressWarnings("KotlinInternalInJava")
  @Mock
  AccountDatastore accountDatastore;

  @Mock ProfileDatastore profileDatastore;

  @SuppressWarnings("KotlinInternalInJava")
  @Mock
  OfflineUpdateDatastore offlineUpdateDatastore;

  @SuppressWarnings("KotlinInternalInJava")
  @Mock
  GoPirateDatastore goPirateDatastore;

  ApiSDKDatabaseInteractorImpl apiSDKDatabaseInteractor;

  @Override
  public void setup() throws Exception {
    super.setup();

    apiSDKDatabaseInteractor =
        spy(
            new ApiSDKDatabaseInteractorImpl(
                brushingsRepository,
                accountDatastore,
                profileDatastore,
                offlineUpdateDatastore,
                goPirateDatastore));
  }

  /*
  TRUNCATE
   */

  @Test
  public void truncate_brushingsRepoDeleteAllError_execue() {
    doReturn(Completable.complete()).when(apiSDKDatabaseInteractor).deleteBrushingsCompletable();
    doReturn(Completable.complete()).when(apiSDKDatabaseInteractor).deleteProfileCompletable();
    doReturn(Completable.complete()).when(apiSDKDatabaseInteractor).truncateAdaptersCompletable();

    apiSDKDatabaseInteractor.truncate().test().assertNoErrors().assertComplete();

    verify(apiSDKDatabaseInteractor).deleteBrushingsCompletable();
    verify(apiSDKDatabaseInteractor).truncateAdaptersCompletable();
  }

  /*
  DELETE BRUSHINGS COMPLETABLE
   */
  @Test
  public void deleteBrushingsCompletable_errorCompletes() {
    when(brushingsRepository.deleteAll()).thenReturn(Completable.error(createException()));

    apiSDKDatabaseInteractor.deleteBrushingsCompletable().test().assertNoErrors().assertComplete();
  }

  /*
  DELETE BRUSHINGS COMPLETABLE
   */
  @Test
  public void deleteProfileCompletable_errorCompletes() {
    when(profileDatastore.deleteAll()).thenReturn(Completable.error(createException()));

    apiSDKDatabaseInteractor.deleteProfileCompletable().test().assertNoErrors().assertComplete();
  }

  @Test
  public void deleteProfileCompletable_completes() {
    when(profileDatastore.deleteAll()).thenReturn(Completable.complete());

    apiSDKDatabaseInteractor.deleteProfileCompletable().test().assertComplete();
  }

  @Test
  public void deleteBrushingsCompletable_completes() {
    when(brushingsRepository.deleteAll()).thenReturn(Completable.complete());

    apiSDKDatabaseInteractor.deleteBrushingsCompletable().test().assertComplete();
  }

  /*
  TRUNCATE ADAPTERS COMPLETABLE
   */
  @Test
  public void truncateAdaptersCompletable_accountAdapterException_executesRestOfTruncates() {
    doAnswer(
            ignore -> {
              throw createException();
            })
        .when(accountDatastore)
        .truncate();

    apiSDKDatabaseInteractor.truncateAdaptersCompletable().test().assertNoErrors().assertComplete();

    verify(offlineUpdateDatastore).truncate();
    verify(goPirateDatastore).truncate();
  }

  @Test
  public void truncateAdaptersCompletable_profileAdapterException_executesRestOfTruncates() {
    when(profileDatastore.deleteAll())
        .thenAnswer(
            ignore -> {
              throw createException();
            });

    apiSDKDatabaseInteractor.truncateAdaptersCompletable().test().assertNoErrors().assertComplete();

    verify(accountDatastore).truncate();
    verify(offlineUpdateDatastore).truncate();
    verify(goPirateDatastore).truncate();
  }

  @Test
  public void truncateAdaptersCompletable_offlineUpdateAdapterException_executesRestOfTruncates() {
    doAnswer(
            ignore -> {
              throw createException();
            })
        .when(offlineUpdateDatastore)
        .truncate();

    apiSDKDatabaseInteractor.truncateAdaptersCompletable().test().assertNoErrors().assertComplete();

    verify(accountDatastore).truncate();
    verify(goPirateDatastore).truncate();
  }

  @Test
  public void truncateAdaptersCompletable_goPirateAdapterException_executesRestOfTruncates() {
    doAnswer(
            ignore -> {
              throw createException();
            })
        .when(goPirateDatastore)
        .truncate();

    apiSDKDatabaseInteractor.truncateAdaptersCompletable().test().assertNoErrors().assertComplete();

    verify(offlineUpdateDatastore).truncate();
    verify(accountDatastore).truncate();
  }

  @Test
  public void truncateAdaptersCompletable_noErrors_executesAllTruncates() {
    apiSDKDatabaseInteractor.truncateAdaptersCompletable().test().assertNoErrors().assertComplete();

    verify(accountDatastore).truncate();
    verify(offlineUpdateDatastore).truncate();
    verify(goPirateDatastore).truncate();
  }

  @NonNull
  private Exception createException() {
    return new Exception("Test forced error");
  }
}
