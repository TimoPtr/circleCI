/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.sdkws.core;

import static com.kolibree.sdkws.data.model.EditProfileData.FIELD_AGE;
import static com.kolibree.sdkws.data.model.EditProfileData.FIELD_BRUSHING_GOAL_TIME;
import static com.kolibree.sdkws.data.model.EditProfileData.FIELD_BRUSHING_NUMBER;
import static com.kolibree.sdkws.data.model.EditProfileData.FIELD_COUNTRY;
import static com.kolibree.sdkws.data.model.EditProfileData.FIELD_FIRST_NAME;
import static com.kolibree.sdkws.data.model.EditProfileData.FIELD_GENDER;
import static com.kolibree.sdkws.data.model.EditProfileData.FIELD_SURVEY_HANDEDNESS;
import static com.kolibree.sdkws.internal.OfflineUpdateInternal.TYPE_DELETE_PROFILE;
import static com.kolibree.sdkws.internal.OfflineUpdateInternal.TYPE_FIELDS;
import static com.kolibree.sdkws.internal.OfflineUpdateInternal.TYPE_PICTURE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.job.JobParameters;
import com.kolibree.android.accountinternal.internal.AccountInternal;
import com.kolibree.android.accountinternal.persistence.repo.AccountDatastore;
import com.kolibree.android.accountinternal.profile.persistence.ProfileDatastore;
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.synchronizator.Synchronizator;
import com.kolibree.sdkws.core.sync.IntegerSyncableField;
import com.kolibree.sdkws.core.sync.StringSyncableField;
import com.kolibree.sdkws.core.sync.SyncableField;
import com.kolibree.sdkws.data.model.EditProfileData;
import com.kolibree.sdkws.internal.OfflineUpdateDatastore;
import com.kolibree.sdkws.internal.OfflineUpdateInternal;
import com.kolibree.sdkws.profile.ProfileApi;
import com.kolibree.sdkws.profile.ProfileManager;
import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.disposables.Disposable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import retrofit2.Response;

/** Created by miguelaragues on 13/3/18. */
@SuppressWarnings("KotlinInternalInJava")
public class SynchronizerJobServiceTest extends BaseUnitTest {

  @Mock Synchronizator synchronizator;

  @Mock ProfileApi profileApi;

  @Mock ProfileManager profileManager;

  @Spy SynchronizerJobService jobService;

  private InternalKolibreeConnector connector;

  @Override
  public void setup() throws Exception {
    super.setup();
    jobService.profileApi = profileApi;
    jobService.synchronizator = synchronizator;
    jobService.profileManager = profileManager;
  }

  @Test
  public void onStartJob_userNotLoggedIn_returnsFalse() {
    mockConnector();
    when(connector.currentAccount()).thenReturn(null);

    doNothing().when(jobService).jobFinished(any(JobParameters.class), anyBoolean());

    assertFalse(jobService.onStartJob(mock(JobParameters.class)));
  }

  @Test
  public void onStartJob_userNotLoggedIn_invokesCompleteJobWithoutReschedule() {
    mockConnector();
    when(connector.currentAccount()).thenReturn(null);

    doNothing().when(jobService).completeJobWithoutReschedule();

    JobParameters jobParams = mock(JobParameters.class);
    jobService.onStartJob(jobParams);

    verify(jobService).completeJobWithoutReschedule();

    verify(jobService, never()).sync();
  }

  @Test
  public void onStartJob_userLoggedIn_returnsTrue() {
    mockConnector();
    when(connector.currentAccount()).thenReturn(mock(AccountInternal.class));

    doNothing().when(jobService).sync();

    assertTrue(jobService.onStartJob(mock(JobParameters.class)));
  }

  @Test
  public void onStartJob_userLoggedIn_storesAccount() {
    mockConnector();
    AccountInternal account = mock(AccountInternal.class);
    when(connector.currentAccount()).thenReturn(account);

    assertNull(jobService.currentAccount);

    doNothing().when(jobService).sync();

    jobService.onStartJob(mock(JobParameters.class));

    assertEquals(account, jobService.currentAccount);
  }

  @Test
  public void onStartJob_userLoggedIn_storesParams() {
    mockConnector();
    when(connector.currentAccount()).thenReturn(mock(AccountInternal.class));

    assertNull(jobService.jobParameters);

    doNothing().when(jobService).sync();

    JobParameters jobParams = mock(JobParameters.class);
    jobService.onStartJob(jobParams);

    assertEquals(jobParams, jobService.jobParameters);
  }

  @Test
  public void onStartJob_userLoggedIn_invokesSync() {
    mockConnector();
    when(connector.currentAccount()).thenReturn(mock(AccountInternal.class));

    doNothing().when(jobService).sync();

    jobService.onStartJob(mock(JobParameters.class));

    verify(jobService).sync();
  }

  /*
  ON STOP JOB
   */
  @Test
  public void onStopJob_nullDisposable_returnsFalse() {
    assertFalse(jobService.onStopJob(mock(JobParameters.class)));
  }

  @Test
  public void onStopJob_withDisposable_invokesDisposeAndReturnsFalse() {
    jobService.syncDisposable = mock(Disposable.class);

    assertFalse(jobService.onStopJob(mock(JobParameters.class)));

    verify(jobService.syncDisposable).dispose();
  }

  /*
  SYNC
   */

  @Test
  public void sync_storesSyncDisposable() {
    assertNull(jobService.syncDisposable);

    doNothing().when(jobService).synchronizePirate();
    doNothing().when(jobService).synchronizeBrushings();
    doNothing().when(jobService).synchronizeProfiles();
    doNothing().when(jobService).onSynchronizationCompleted();
    doNothing().when(jobService).synchronizeExternalAppData();

    jobService.sync();

    assertNotNull(jobService.syncDisposable);
  }

  @Test
  public void sync_invokesSynchronizeBrushings() {
    doNothing().when(jobService).synchronizePirate();
    doNothing().when(jobService).synchronizeBrushings();
    doNothing().when(jobService).synchronizeProfiles();
    doNothing().when(jobService).onSynchronizationCompleted();
    doNothing().when(jobService).synchronizeExternalAppData();

    jobService.sync();

    verify(jobService).synchronizeBrushings();
  }

  @Test
  public void sync_invokesSynchronizePirate() {
    doNothing().when(jobService).synchronizePirate();
    doNothing().when(jobService).synchronizeBrushings();
    doNothing().when(jobService).synchronizeProfiles();
    doNothing().when(jobService).onSynchronizationCompleted();
    doNothing().when(jobService).synchronizeExternalAppData();

    jobService.sync();

    verify(jobService).synchronizePirate();
  }

  @Test
  public void sync_invokesSynchronizeProfiles() {
    doNothing().when(jobService).synchronizePirate();
    doNothing().when(jobService).synchronizeBrushings();
    doNothing().when(jobService).synchronizeProfiles();
    doNothing().when(jobService).onSynchronizationCompleted();
    doNothing().when(jobService).synchronizeExternalAppData();

    jobService.sync();

    verify(jobService).synchronizeProfiles();
  }

  @Test
  public void sync_invokesSynchronizatorSynchronize() {
    doNothing().when(jobService).synchronizePirate();
    doNothing().when(jobService).synchronizeBrushings();
    doNothing().when(jobService).synchronizeProfiles();
    doNothing().when(jobService).onSynchronizationCompleted();
    doNothing().when(jobService).synchronizeExternalAppData();

    jobService.sync();

    verify(synchronizator).synchronize();
  }

  @Test
  public void sync_synchronizesExternalAppData() {
    doNothing().when(jobService).synchronizePirate();
    doNothing().when(jobService).synchronizeBrushings();
    doNothing().when(jobService).synchronizeProfiles();
    doNothing().when(jobService).onSynchronizationCompleted();
    doNothing().when(jobService).synchronizeExternalAppData();

    jobService.sync();

    verify(jobService).synchronizeExternalAppData();
  }

  @Test
  public void sync_noErrors_invokesOnSynchronizationCompleted() {
    doNothing().when(jobService).synchronizePirate();
    doNothing().when(jobService).synchronizeBrushings();
    doNothing().when(jobService).synchronizeProfiles();
    doNothing().when(jobService).onSynchronizationCompleted();

    jobService.sync();

    verify(jobService).onSynchronizationCompleted();
  }

  @Test
  public void sync_withErrors_invokesOnSynchronizationCompleted() {
    doNothing().when(jobService).synchronizePirate();
    doNothing().when(jobService).synchronizeBrushings();
    doNothing().when(jobService).completeJobWithoutReschedule();
    doNothing().when(jobService).onSynchronizationCompleted();

    doThrow(new IllegalStateException("Forced error from test"))
        .when(jobService)
        .synchronizeProfiles();

    jobService.sync();

    verify(jobService).onSynchronizationCompleted();
  }

  /*
  ON SYNCHRONIZATION COMPLETED
   */

  @Test
  public void sync_noErrors_invokesCompleteJobWithoutReschedule() {
    doNothing().when(jobService).completeJobWithoutReschedule();

    mockConnector();

    jobService.onSynchronizationCompleted();

    verify(jobService).completeJobWithoutReschedule();
  }

  @Test
  public void sync_noErrors_invokesSendRefreshBroadcast() {
    doNothing().when(jobService).completeJobWithoutReschedule();

    mockConnector();

    jobService.onSynchronizationCompleted();

    verify(connector).sendRefreshBroadcast();
  }

  /*
  SYNCHRONIZE BRUSHINGS
   */

  @Test
  public void synchronizeBrushings_invokesSynchronizeBrushingOnEachProfile() {
    jobService.currentAccount = mock(AccountInternal.class);

    mockConnector();

    ProfileInternal profile1 = mock(ProfileInternal.class);
    long id1 = 1L;
    when(profile1.getId()).thenReturn(id1);
    ProfileInternal profile2 = mock(ProfileInternal.class);
    long id2 = 2L;
    when(profile2.getId()).thenReturn(id2);

    when(jobService.currentAccount.getInternalProfiles())
        .thenReturn(Arrays.asList(profile1, profile2));

    when(connector.synchronizeBrushing(anyLong())).thenReturn(Single.just(true));

    jobService.synchronizeBrushings();

    verify(connector).synchronizeBrushing(id1);
    verify(connector).synchronizeBrushing(id2);
  }

  @Test
  public void synchronizeBrushings_synchronizeBrushingThrowsException_proceedsWithExecution() {
    jobService.currentAccount = mock(AccountInternal.class);

    mockConnector();

    ProfileInternal profile1 = mock(ProfileInternal.class);
    long id1 = 1L;
    when(profile1.getId()).thenReturn(id1);

    when(jobService.currentAccount.getInternalProfiles())
        .thenReturn(new ArrayList<>(Collections.singletonList(profile1)));

    when(connector.synchronizeBrushing(anyLong()))
        .thenReturn(Single.error(new Throwable("Test forced error")));

    // Asserting that synchronizeBrushings does not throw the test forced error
    jobService.synchronizeBrushings();
  }

  /*
  synchronizePirate
   */

  @Test
  public void synchronizePirate_invokesSynchronizePirateOnEachProfile() {
    jobService.currentAccount = mock(AccountInternal.class);

    mockConnector();

    ProfileInternal profile1 = mock(ProfileInternal.class);
    long id1 = 1L;
    when(profile1.getId()).thenReturn(id1);
    ProfileInternal profile2 = mock(ProfileInternal.class);
    long id2 = 2L;
    when(profile2.getId()).thenReturn(id2);

    when(jobService.currentAccount.getInternalProfiles())
        .thenReturn(Arrays.asList(profile1, profile2));

    when(connector.synchronizeBrushing(anyLong())).thenReturn(Single.just(true));

    jobService.synchronizePirate();

    verify(connector).synchronizeGoPirate(id1);
    verify(connector).synchronizeGoPirate(id2);
  }

  @Test
  public void remoteOrDefault_returns_remoteAccount_ifRemoteNotNull() {
    AccountInternal internal = mock(AccountInternal.class);
    AccountInternal remote = mock(AccountInternal.class);
    doReturn(remote).when(jobService).getRemoteAccount();

    Assert.assertEquals(remote, jobService.remoteOrDefault(internal));
  }

  @Test
  public void remoteOrDefault_returns_currentAccount_ifRemoteIsNull() {
    AccountInternal internal = mock(AccountInternal.class);
    doReturn(null).when(jobService).getRemoteAccount();

    Assert.assertEquals(internal, jobService.remoteOrDefault(internal));
  }

  /*
  SYNCHRONIZE PROFILES
   */
  @Test
  public void
      synchronizeProfiles_withLocalAccount_doesNotInvokeUpdateIfLocalProfileDoesNotNeedIt() {
    AccountInternal account = mock(AccountInternal.class);
    ProfileInternal profile = mock(ProfileInternal.class);
    when(profile.getNeedsUpdate()).thenReturn(false);
    when(account.getInternalProfiles()).thenReturn(Collections.singletonList(profile));

    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);
    doReturn(offlineUpdateDao).when(jobService).getOfflineUpdateDatastore();

    setupSynchronizeProfiles(null, account);
    jobService.currentAccount = null;

    jobService.synchronizeProfiles();

    verify(account, times(1)).getInternalProfiles();
    verify(profileApi, never()).updateProfile(anyLong(), anyLong(), any(ProfileInternal.class));
  }

  @Test
  public void
      synchronizeProfiles_withLocalAccount_firesProfileUpdateCallAndMasksTheProfileAsUpdated() {
    long accountId = 4L;
    long profileId = 10L;
    ProfileInternal profile = mock(ProfileInternal.class);
    when(profile.getId()).thenReturn(profileId);
    when(profile.getNeedsUpdate()).thenReturn(true);

    AccountInternal account = mock(AccountInternal.class);
    when(account.getId()).thenReturn(accountId);
    when(account.getInternalProfiles()).thenReturn(Collections.singletonList(profile));

    doReturn(Single.just(Response.success(profile)))
        .when(profileApi)
        .updateProfile(accountId, profileId, profile);
    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);
    doReturn(offlineUpdateDao).when(jobService).getOfflineUpdateDatastore();
    ProfileDatastore profileDatastore = mock(ProfileDatastore.class);
    doReturn(Completable.complete()).when(profileDatastore).markAsUpdated(profile);
    doReturn(profileDatastore).when(jobService).profileDatastore();

    setupSynchronizeProfiles(null, account);
    jobService.currentAccount = null;

    jobService.synchronizeProfiles();

    verify(account, times(1)).getInternalProfiles();
    verify(profileDatastore, times(1)).markAsUpdated(profile);
    verify(profileApi, times(1)).updateProfile(accountId, profileId, profile);
  }

  @Test
  public void synchronizeProfiles_nullRemoteAccount_neverInvokesCheckForNewOrDeletedProfiles() {
    jobService.currentAccount = mock(AccountInternal.class);

    setupSynchronizeProfiles(null, null);

    jobService.synchronizeProfiles();

    verify(jobService, never()).checkForNewOrDeletedProfiles(any());
  }

  @Test
  public void synchronizeProfiles_nullRemoteAccount_neverInvokesCheckUpdatableFields() {
    jobService.currentAccount = mock(AccountInternal.class);

    setupSynchronizeProfiles(null, null);

    jobService.synchronizeProfiles();

    verify(jobService, never()).checkUpdatableFields(any(), any());
  }

  @Test
  public void synchronizeProfiles_nullRemoteAccount_neverInvokesUpdateLocalFields() {
    jobService.currentAccount = mock(AccountInternal.class);

    setupSynchronizeProfiles(null, null);

    jobService.synchronizeProfiles();

    verify(jobService, never()).updateBrushingSettingsSoundsFromInternalProfiles(any());
  }

  @Test
  public void
      synchronizeProfiles_nullRemoteAccount_neverInvokesUpdateCurrentProfileIdFromLocalAccount() {
    jobService.currentAccount = mock(AccountInternal.class);

    setupSynchronizeProfiles(null, null);

    jobService.synchronizeProfiles();

    verify(jobService, never()).updateCurrentProfileIdFromLocalAccount(any());
  }

  @Test
  public void synchronizeProfiles_nonNullRemoteAccount_invokesCheckForNewOrDeletedProfiles() {
    jobService.currentAccount = mock(AccountInternal.class);

    AccountInternal expectedRemoteAccount = setupSynchronizeProfiles();

    jobService.synchronizeProfiles();

    verify(jobService).checkForNewOrDeletedProfiles(expectedRemoteAccount);
  }

  @Test
  public void synchronizeProfiles_nonNullRemoteAccount_invokesCheckProfileAvatars() {
    jobService.currentAccount = mock(AccountInternal.class);

    setupSynchronizeProfiles();

    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);
    doReturn(offlineUpdateDao).when(jobService).getOfflineUpdateDatastore();

    jobService.synchronizeProfiles();

    verify(jobService).uploadAvatarIfUpdatedWhileOffline(any(OfflineUpdateDatastore.class));
  }

  @Test
  public void synchronizeProfiles_nonNullRemoteAccount_invokesGetRemoteAccount() {
    jobService.currentAccount = mock(AccountInternal.class);

    AccountInternal remoteAccount = setupSynchronizeProfiles();

    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);
    doReturn(offlineUpdateDao).when(jobService).getOfflineUpdateDatastore();

    jobService.synchronizeProfiles();

    verify(jobService).remoteOrDefault(remoteAccount);
  }

  @Test
  public void synchronizeProfiles_nonNullRemoteAccount_invokesCheckUpdatableFields() {
    jobService.currentAccount = mock(AccountInternal.class);

    AccountInternal expectedRemoteAccount = setupSynchronizeProfiles();

    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);
    doReturn(offlineUpdateDao).when(jobService).getOfflineUpdateDatastore();

    jobService.synchronizeProfiles();

    verify(jobService)
        .checkUpdatableFields(eq(expectedRemoteAccount), any(OfflineUpdateDatastore.class));
  }

  @Test
  public void
      synchronizeProfiles_nonNullRemoteAccount_invokesUpdateCurrentProfileIdFromLocalAccount() {
    jobService.currentAccount = mock(AccountInternal.class);

    AccountInternal expectedRemoteAccount = setupSynchronizeProfiles();

    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);
    doReturn(offlineUpdateDao).when(jobService).getOfflineUpdateDatastore();

    jobService.synchronizeProfiles();

    verify(jobService).updateCurrentProfileIdFromLocalAccount(eq(expectedRemoteAccount));
  }

  @Test
  public void synchronizeProfiles_nonNullRemoteAccount_invokesKonnectorSaveAccount() {
    jobService.currentAccount = mock(AccountInternal.class);

    AccountInternal expectedRemoteAccount = setupSynchronizeProfiles();

    jobService.synchronizeProfiles();

    verify(connector).saveAccount(expectedRemoteAccount);
  }

  @Test
  public void synchronizeProfiles_nonNullRemoteAccount_invokesUpdateLocalFields() {
    jobService.currentAccount = mock(AccountInternal.class);

    doReturn(mock(AccountDatastore.class)).when(jobService).accountAdapter();

    AccountInternal expectedRemoteAccount = setupSynchronizeProfiles();

    jobService.synchronizeProfiles();

    verify(jobService).updateBrushingSettingsSoundsFromInternalProfiles(expectedRemoteAccount);
  }

  @Test
  public void
      synchronizeProfiles_nonNullRemoteAccount_invokesRefreshAvatarCachesAsLastInvocation() {
    jobService.currentAccount = mock(AccountInternal.class);

    setupSynchronizeProfiles();

    InOrder inOrder = Mockito.inOrder(jobService);

    jobService.synchronizeProfiles();

    inOrder.verify(jobService).refreshAvatarCache();

    inOrder.verifyNoMoreInteractions();
  }

  private AccountInternal setupSynchronizeProfiles() {
    return setupSynchronizeProfiles(mock(AccountInternal.class), null);
  }

  private AccountInternal setupSynchronizeProfiles(
      AccountInternal remoteAccount, AccountInternal localAccount) {
    mockConnector(localAccount);

    doReturn(remoteAccount).when(jobService).getRemoteAccount();

    doNothing().when(jobService).checkForNewOrDeletedProfiles(any(AccountInternal.class));
    doNothing()
        .when(jobService)
        .uploadAvatarIfUpdatedWhileOffline(any(OfflineUpdateDatastore.class));
    doNothing()
        .when(jobService)
        .checkUpdatableFields(any(AccountInternal.class), any(OfflineUpdateDatastore.class));
    doNothing().when(jobService).refreshAvatarCache();

    return remoteAccount;
  }

  /*
  GET REMOTE ACCOUNT
   */
  @Test
  public void getRemoteAccount_invokesConnectorGetRemoteAccount() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    long expectedId = 99L;
    when(currentAccount.getId()).thenReturn(expectedId);

    mockConnector();

    jobService.getRemoteAccount();

    verify(connector).getRemoteAccount(expectedId);
  }

  /*
  CHECK FOR NEW OR DELETED PROFILES
   */
  @Test
  public void checkForNewOrDeletedProfiles_withLocalProfile_doesNothing() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    AccountInternal remoteAccount = mock(AccountInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    long remoteId = 1L;
    when(remoteProfile.getId()).thenReturn(remoteId);
    when(remoteAccount.getInternalProfiles()).thenReturn(Arrays.asList(remoteProfile));

    ProfileInternal localProfile = mock(ProfileInternal.class);
    when(currentAccount.getProfileInternalWithId(remoteId)).thenReturn(localProfile);

    jobService.checkForNewOrDeletedProfiles(remoteAccount);

    verify(jobService, never()).getOfflineUpdateDatastore();
  }

  @Test
  public void checkForNewOrDeletedProfiles_withoutLocalProfile_noOfflineUpdate_addsRemoteProfile() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    AccountInternal remoteAccount = mock(AccountInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    long remoteId = 1L;
    when(remoteProfile.getId()).thenReturn(remoteId);
    when(remoteAccount.getInternalProfiles()).thenReturn(Collections.singletonList(remoteProfile));

    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);
    doReturn(offlineUpdateDao).when(jobService).getOfflineUpdateDatastore();

    when(offlineUpdateDao.getOfflineUpdateForProfileId(remoteId, TYPE_DELETE_PROFILE))
        .thenReturn(null);

    doNothing().when(jobService).onProfileAddedRemotely(any(ProfileInternal.class));

    jobService.checkForNewOrDeletedProfiles(remoteAccount);

    verify(jobService).onProfileAddedRemotely(remoteProfile);
  }

  @Test
  public void
      checkForNewOrDeletedProfiles_withoutLocalProfile_withOfflineUpdate_deleteProfileError_invokesofflineUpdateDaoInsertOrUpdate() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    AccountInternal remoteAccount = mock(AccountInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    long remoteId = 1L;
    when(remoteProfile.getId()).thenReturn(remoteId);
    when(remoteAccount.getInternalProfiles()).thenReturn(Arrays.asList(remoteProfile));

    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);
    doReturn(offlineUpdateDao).when(jobService).getOfflineUpdateDatastore();

    OfflineUpdateInternal offlineUpdateInternal = mock(OfflineUpdateInternal.class);
    when(offlineUpdateDao.getOfflineUpdateForProfileId(remoteId, TYPE_DELETE_PROFILE))
        .thenReturn(offlineUpdateInternal);

    mockConnector();

    when(profileManager.deleteProfile(remoteProfile)).thenReturn(Single.just(false));

    jobService.checkForNewOrDeletedProfiles(remoteAccount);

    verify(offlineUpdateDao).insertOrUpdate(offlineUpdateInternal);
  }

  @Test
  public void
      checkForNewOrDeletedProfiles_withoutLocalProfile_withOfflineUpdate_deleteProfileSuccess_invokesOnProfileDeletedRemotely() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    AccountInternal remoteAccount = mock(AccountInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    long remoteId = 1L;
    when(remoteProfile.getId()).thenReturn(remoteId);
    when(remoteAccount.getInternalProfiles()).thenReturn(Arrays.asList(remoteProfile));

    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);
    doReturn(offlineUpdateDao).when(jobService).getOfflineUpdateDatastore();

    OfflineUpdateInternal offlineUpdateInternal = mock(OfflineUpdateInternal.class);
    when(offlineUpdateDao.getOfflineUpdateForProfileId(remoteId, TYPE_DELETE_PROFILE))
        .thenReturn(offlineUpdateInternal);

    mockConnector();

    when(profileManager.deleteProfile(remoteProfile)).thenReturn(Single.just(true));

    doNothing().when(jobService).resetActiveProfile();

    jobService.checkForNewOrDeletedProfiles(remoteAccount);

    verify(jobService).resetActiveProfile();
  }

  @Test
  public void
      checkForNewOrDeletedProfiles_profileRemovedRemotelyStillPresentInDatabase_invokesOnProfileRemovedRemotely() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    ProfileInternal localProfileRemovedRemotely = mock(ProfileInternal.class);
    ProfileInternal profile = mock(ProfileInternal.class);
    long remoteIdPresent = 99L;
    long remoteIdRemoved = 1L;
    when(profile.getId()).thenReturn(remoteIdPresent);
    when(localProfileRemovedRemotely.getId()).thenReturn(remoteIdRemoved);
    when(currentAccount.getInternalProfiles())
        .thenReturn(Arrays.asList(profile, localProfileRemovedRemotely));
    when(currentAccount.getProfileInternalWithId(remoteIdPresent)).thenReturn(profile);

    AccountInternal remoteAccount = mock(AccountInternal.class);
    when(remoteAccount.getInternalProfiles()).thenReturn(Collections.singletonList(profile));

    doNothing().when(jobService).onProfileRemovedRemotely(any(ProfileInternal.class));

    jobService.checkForNewOrDeletedProfiles(remoteAccount);

    verify(jobService).onProfileRemovedRemotely(localProfileRemovedRemotely);
    verify(jobService, never()).onProfileRemovedRemotely(profile);
  }

  /*
  ON PROFILE REMOVED REMOTELY
   */
  @Test
  public void onProfileRemovedRemotely_invokesDeleteLocally() {
    ProfileInternal localProfileRemovedRemotely = mock(ProfileInternal.class);
    long remoteId = 1L;
    when(localProfileRemovedRemotely.getId()).thenReturn(remoteId);

    ProfileDatastore profileDatastore = mock(ProfileDatastore.class);
    doReturn(profileDatastore).when(jobService).profileDatastore();

    doNothing().when(jobService).resetActiveProfile();

    when(profileDatastore.deleteProfile(remoteId)).thenReturn(Completable.complete());

    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;
    when(currentAccount.getInternalProfiles()).thenReturn(Collections.emptyList());

    jobService.onProfileRemovedRemotely(localProfileRemovedRemotely);

    verify(profileDatastore).deleteProfile(remoteId);
  }

  @Test
  public void onProfileRemovedRemotely_invokesResetActiveProfileBeforeDeletingLocally() {
    ProfileInternal localProfileRemovedRemotely = mock(ProfileInternal.class);
    long remoteId = 1L;
    when(localProfileRemovedRemotely.getId()).thenReturn(remoteId);

    ProfileDatastore profileDatastore = mock(ProfileDatastore.class);
    doReturn(profileDatastore).when(jobService).profileDatastore();

    doNothing().when(jobService).resetActiveProfile();

    when(profileDatastore.deleteProfile(remoteId)).thenReturn(Completable.complete());

    InOrder inOrder = inOrder(jobService, profileDatastore);

    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;
    when(currentAccount.getInternalProfiles()).thenReturn(Collections.emptyList());

    jobService.onProfileRemovedRemotely(localProfileRemovedRemotely);

    inOrder.verify(jobService).resetActiveProfile();
    inOrder.verify(profileDatastore).deleteProfile(remoteId);
  }

  @Test
  public void onProfileRemovedRemotely_removesProfileFromLocalCurrentAccount() {
    ProfileInternal localProfileRemovedRemotely = mock(ProfileInternal.class);
    long remoteId = 1L;
    when(localProfileRemovedRemotely.getId()).thenReturn(remoteId);

    ProfileDatastore profileDatastore = mock(ProfileDatastore.class);
    doReturn(profileDatastore).when(jobService).profileDatastore();

    doNothing().when(jobService).resetActiveProfile();

    when(profileDatastore.deleteProfile(remoteId)).thenReturn(Completable.complete());

    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    List<ProfileInternal> mockedList = mock(List.class);
    when(currentAccount.getInternalProfiles()).thenReturn(mockedList);

    jobService.onProfileRemovedRemotely(localProfileRemovedRemotely);

    verify(mockedList).remove(localProfileRemovedRemotely);
  }

  /*
  RESET ACTIVE PROFILE
   */
  @Test
  public void resetActiveProfile_invokesSetOwnerAsCurrentProfile() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    AccountDatastore accountDatastore = mock(AccountDatastore.class);
    doReturn(accountDatastore).when(jobService).accountAdapter();

    jobService.resetActiveProfile();

    verify(currentAccount).setOwnerProfileAsCurrent();
  }

  @Test
  public void resetActiveProfile_invokesAccountAdapterUpdateCurrentProfileId() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    AccountDatastore accountDatastore = mock(AccountDatastore.class);
    doReturn(accountDatastore).when(jobService).accountAdapter();

    jobService.resetActiveProfile();

    verify(accountDatastore).updateCurrentProfileId(currentAccount);
  }

  /*
  ON PROFILE ADDED REMOTELY
   */

  @Test
  public void onProfileAddedRemotely_addsToCurrentAccount() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    ArrayList<ProfileInternal> list = mock(ArrayList.class);
    when(currentAccount.getInternalProfiles()).thenReturn(list);

    ProfileInternal remoteProfile = mock(ProfileInternal.class);

    ProfileDatastore ProfileDatastore = mock(ProfileDatastore.class);
    doReturn(ProfileDatastore).when(jobService).profileDatastore();

    jobService.onProfileAddedRemotely(remoteProfile);

    verify(list).add(remoteProfile);

    verify(ProfileDatastore).addProfile(remoteProfile);
  }

  @Test
  public void onProfileAddedRemotely_invokesProfileDatastoreAddProfile() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    ArrayList<ProfileInternal> list = mock(ArrayList.class);
    when(currentAccount.getInternalProfiles()).thenReturn(list);

    ProfileInternal remoteProfile = mock(ProfileInternal.class);

    ProfileDatastore ProfileDatastore = mock(ProfileDatastore.class);
    doReturn(ProfileDatastore).when(jobService).profileDatastore();

    jobService.onProfileAddedRemotely(remoteProfile);

    verify(ProfileDatastore).addProfile(remoteProfile);
  }

  /*
  UPLOAD AVATAR IF UPDATED WHILE OFFLINE
   */
  @Test
  public void uploadAvatarIfUpdatedWhileOffline_emptyProfileList_doesNothing() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    when(currentAccount.getInternalProfiles()).thenReturn(new ArrayList<>());

    mockConnector();

    OfflineUpdateDatastore offlineUpdater = mock(OfflineUpdateDatastore.class);
    jobService.uploadAvatarIfUpdatedWhileOffline(offlineUpdater);

    verify(connector, never()).changeProfilePictureSingle(any(ProfileInternal.class), anyString());
  }

  @Test
  public void uploadAvatarIfUpdatedWhileOffline_nullPictureUpdate_doesNothing() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    ProfileInternal profile = mock(ProfileInternal.class);
    Long profileId = 9L;
    when(profile.getId()).thenReturn(profileId);
    when(currentAccount.getInternalProfiles()).thenReturn(Collections.singletonList(profile));

    mockConnector();

    OfflineUpdateDatastore offlineUpdater = mock(OfflineUpdateDatastore.class);
    jobService.uploadAvatarIfUpdatedWhileOffline(offlineUpdater);

    verify(connector, never()).changeProfilePictureSingle(any(ProfileInternal.class), anyString());
  }

  @Test
  public void
      uploadAvatarIfUpdatedWhileOffline_withPictureUpdate_invokesChangeProfilePictureWithNewPath() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    ProfileInternal profile = mock(ProfileInternal.class);
    Long profileId = 9L;
    when(profile.getId()).thenReturn(profileId);
    when(currentAccount.getInternalProfiles()).thenReturn(Collections.singletonList(profile));

    mockConnector();

    OfflineUpdateDatastore offlineUpdater = mock(OfflineUpdateDatastore.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);
    String expectedPath = "dsfsd";
    when(offlineUpdate.getPicturePath()).thenReturn(expectedPath);
    when(offlineUpdater.getOfflineUpdateForProfileId(profileId, TYPE_PICTURE))
        .thenReturn(offlineUpdate);

    jobService.uploadAvatarIfUpdatedWhileOffline(offlineUpdater);

    verify(connector).changeProfilePictureSingle(profile, expectedPath);
  }

  /*
  CHECK UPDATABLE FIELDS
   */
  @Test
  public void checkUpdatableFields_nullRemoteProfile_invokesOnProfileDeletedRemotely() {
    AccountInternal remoteAccount = mock(AccountInternal.class);
    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);

    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    ProfileInternal localProfile = mock(ProfileInternal.class);
    Long profileId = 9L;
    when(localProfile.getId()).thenReturn(profileId);
    when(currentAccount.getInternalProfiles()).thenReturn(Arrays.asList(localProfile));

    ProfileDatastore profileDatastore = mock(ProfileDatastore.class);
    doReturn(profileDatastore).when(jobService).profileDatastore();

    doNothing().when(jobService).resetActiveProfile();

    jobService.checkUpdatableFields(remoteAccount, offlineUpdateDao);

    verify(profileDatastore).deleteProfile(profileId);
    verify(jobService).resetActiveProfile();
  }

  @Test
  public void
      checkUpdatableFields_withRemoteProfile_withOfflineUpdateNull_invokesUpdateLocalProfile() {
    AccountInternal remoteAccount = mock(AccountInternal.class);
    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);

    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    ProfileInternal localProfile = mock(ProfileInternal.class);
    Long profileId = 9L;
    when(localProfile.getId()).thenReturn(profileId);
    when(currentAccount.getInternalProfiles()).thenReturn(Arrays.asList(localProfile));

    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    when(remoteAccount.getProfileInternalWithId(profileId)).thenReturn(remoteProfile);

    ProfileDatastore ProfileDatastore = mock(ProfileDatastore.class);
    doReturn(ProfileDatastore).when(jobService).profileDatastore();

    doNothing()
        .when(jobService)
        .updateLocalProfile(any(ProfileInternal.class), any(ProfileInternal.class));

    jobService.checkUpdatableFields(remoteAccount, offlineUpdateDao);

    verify(jobService).updateLocalProfile(localProfile, remoteProfile);
  }

  @Test
  public void
      checkUpdatableFields_withRemoteProfile_withOfflineUpdate_invokesOnProfileUpdatedLocally() {
    AccountInternal remoteAccount = mock(AccountInternal.class);
    OfflineUpdateDatastore offlineUpdateDao = mock(OfflineUpdateDatastore.class);

    AccountInternal currentAccount = mock(AccountInternal.class);
    jobService.currentAccount = currentAccount;

    ProfileInternal localProfile = mock(ProfileInternal.class);
    Long profileId = 9L;
    when(localProfile.getId()).thenReturn(profileId);
    when(currentAccount.getInternalProfiles()).thenReturn(Arrays.asList(localProfile));

    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    when(remoteAccount.getProfileInternalWithId(profileId)).thenReturn(remoteProfile);

    ProfileDatastore profileDatastore = mock(ProfileDatastore.class);
    doReturn(profileDatastore).when(jobService).profileDatastore();

    doNothing()
        .when(jobService)
        .onProfileUpdatedLocally(
            any(ProfileInternal.class),
            any(ProfileInternal.class),
            any(OfflineUpdateInternal.class));

    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);
    when(offlineUpdateDao.getOfflineUpdateForProfileId(profileId, TYPE_FIELDS))
        .thenReturn(offlineUpdate);

    jobService.checkUpdatableFields(remoteAccount, offlineUpdateDao);

    verify(jobService).onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);
  }

  /*
  ON PROFILE UPDATED LOCALLY
   */
  @Test
  public void onProfileUpdatedLocally_updatesLocalProfileStatsAndKolibreeIndexAndPersists() {
    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    int point = 42;
    when(remoteProfile.getPoints()).thenReturn(point);

    when(offlineUpdate.getSyncableFields()).thenReturn(new ArrayList<>());

    ProfileDatastore profileDatastore = mock(ProfileDatastore.class);
    doReturn(profileDatastore).when(jobService).profileDatastore();

    final boolean[] subscribed = new boolean[1];
    Completable completable =
        Completable.create(
            emitter -> {
              subscribed[0] = true;

              emitter.onComplete();
            });
    when(profileDatastore.updateProfile(localProfile)).thenReturn(completable);

    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(localProfile).setPoints(point);

    verify(profileDatastore).updateProfile(localProfile);

    assertTrue(subscribed[0]);
  }

  @Test
  public void onProfileUpdatedLocally_withDataUpdate_invokesEditProfile() {
    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    int point = 42;
    when(remoteProfile.getPoints()).thenReturn(point);

    when(offlineUpdate.getSyncableFields()).thenReturn(new ArrayList<>());

    ProfileDatastore profileDatastore = mock(ProfileDatastore.class);
    when(profileDatastore.updateProfile(any())).thenReturn(Completable.complete());
    doReturn(profileDatastore).when(jobService).profileDatastore();

    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    when(editProfileData.hasUpdate()).thenReturn(true);

    mockConnector();

    Single<Boolean> editProfileSingle = mock(Single.class);
    when(connector.editProfile(editProfileData, localProfile)).thenReturn(editProfileSingle);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(connector).editProfile(editProfileData, localProfile);

    verify(editProfileSingle).blockingGet();
  }

  /*
  FIELD_COUNTRY
   */

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_COUNTRY_remoteUpdatedValue_setsValueToLocalProfile() {
    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    String remoteValue = "remote value";
    when(remoteProfile.getAddressCountry()).thenReturn(remoteValue);
    String newValue = "new value";

    prepareStringSyncableField(offlineUpdate, FIELD_COUNTRY, newValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(localProfile).setAddressCountry(remoteValue);
  }

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_COUNTRY_remoteUntouched_addsNewValueToEditProfileData() {
    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    String remoteValue = "remote value";
    when(remoteProfile.getAddressCountry()).thenReturn(remoteValue);
    String newValue = "new value";

    prepareStringSyncableField(offlineUpdate, FIELD_COUNTRY, remoteValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(editProfileData).setCountryCode(newValue);
  }

  /*
  FIELD_GENDER
   */

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_GENDER_remoteUpdatedValue_setsValueToLocalProfile() {
    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    String remoteValue = "remote value";
    when(remoteProfile.getGender()).thenReturn(remoteValue);
    String newValue = "new value";

    prepareStringSyncableField(offlineUpdate, FIELD_GENDER, newValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(localProfile).setGender(remoteValue);
  }

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_GENDER_remoteUntouched_addsNewValueToEditProfileData() {
    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    String remoteValue = "remote value";
    when(remoteProfile.getGender()).thenReturn(remoteValue);
    String newValue = "new value";

    prepareStringSyncableField(offlineUpdate, FIELD_GENDER, remoteValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(editProfileData).setGender(newValue);
  }

  /*
  FIELD_SURVEY_HANDEDNESS
   */

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_SURVEY_HANDEDNESS_remoteUpdatedValue_setsValueToLocalProfile() {
    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    String remoteValue = "remote value";
    when(remoteProfile.getHandedness()).thenReturn(remoteValue);
    String newValue = "new value";

    prepareStringSyncableField(offlineUpdate, FIELD_SURVEY_HANDEDNESS, newValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(localProfile).setHandedness(remoteValue);
  }

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_SURVEY_HANDEDNESS_remoteUntouched_addsNewValueToEditProfileData() {
    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    String remoteValue = "remote value";
    when(remoteProfile.getHandedness()).thenReturn(remoteValue);
    String newValue = "new value";

    prepareStringSyncableField(offlineUpdate, FIELD_SURVEY_HANDEDNESS, remoteValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(editProfileData).setHandedness(newValue);
  }

  /*
  FIELD_FIRST_NAME
   */

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_FIRST_NAME_remoteUpdatedValue_setsValueToLocalProfile() {
    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    String remoteValue = "remote value";
    when(remoteProfile.getFirstName()).thenReturn(remoteValue);
    String newValue = "new value";

    prepareStringSyncableField(offlineUpdate, FIELD_FIRST_NAME, newValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(localProfile).setFirstName(remoteValue);
  }

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_FIRST_NAME_remoteUntouched_addsNewValueToEditProfileData() {
    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    String remoteValue = "remote value";
    when(remoteProfile.getFirstName()).thenReturn(remoteValue);
    String newValue = "new value";

    prepareStringSyncableField(offlineUpdate, FIELD_FIRST_NAME, remoteValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(editProfileData).setFirstName(newValue);
  }

  /*
  FIELD_AGE
   */

  @Test
  public void onProfileUpdatedLocally_withFIELD_AGE_remoteUpdatedValue_setsValueToLocalProfile() {
    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    int remoteValue = 1;
    when(remoteProfile.getAge()).thenReturn(remoteValue);
    int newValue = 2;

    prepareIntegerSyncableField(offlineUpdate, FIELD_AGE, newValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(localProfile).setAge(remoteValue);
  }

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_AGE_remoteUntouched_addsNewValueToEditProfileData() {
    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    int remoteValue = 1;
    when(remoteProfile.getAge()).thenReturn(remoteValue);
    int newValue = 2;

    prepareIntegerSyncableField(offlineUpdate, FIELD_AGE, remoteValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(editProfileData).setAge(newValue);
  }

  /*
  FIELD_BRUSHING_NUMBER
   */

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_BRUSHING_NUMBER_remoteUpdatedValue_setsValueToLocalProfile() {
    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    int remoteValue = 3;
    when(remoteProfile.getBrushingNumber()).thenReturn(remoteValue);
    int newValue = 2;

    prepareIntegerSyncableField(offlineUpdate, FIELD_BRUSHING_NUMBER, newValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(localProfile).setBrushingNumber(remoteValue);
  }

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_BRUSHING_NUMBER_remoteUntouched_addsNewValueToEditProfileData() {
    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    int remoteValue = 3;
    when(remoteProfile.getBrushingNumber()).thenReturn(remoteValue);
    int newValue = 2;

    prepareIntegerSyncableField(offlineUpdate, FIELD_BRUSHING_NUMBER, remoteValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(editProfileData).setBrushingNumber(newValue);
  }

  /*
  FIELD_BRUSHING_GOAL_TIME
   */

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_BRUSHING_GOAL_TIME_remoteUpdatedValue_setsValueToLocalProfile() {
    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    int remoteValue = 1;
    when(remoteProfile.getBrushingTime()).thenReturn(remoteValue);
    int newValue = 2;

    prepareIntegerSyncableField(offlineUpdate, FIELD_BRUSHING_GOAL_TIME, newValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(localProfile).setBrushingTime(remoteValue);
  }

  @Test
  public void
      onProfileUpdatedLocally_withFIELD_BRUSHING_GOAL_TIME_remoteUntouched_addsNewValueToEditProfileData() {
    EditProfileData editProfileData = mock(EditProfileData.class);
    doReturn(editProfileData).when(jobService).emptyEditProfileData();

    ProfileInternal localProfile = mock(ProfileInternal.class);
    ProfileInternal remoteProfile = mock(ProfileInternal.class);
    OfflineUpdateInternal offlineUpdate = mock(OfflineUpdateInternal.class);

    prepareForSyncableFields(remoteProfile, offlineUpdate);

    int remoteValue = 1;
    when(remoteProfile.getBrushingTime()).thenReturn(remoteValue);
    int newValue = 2;

    prepareIntegerSyncableField(offlineUpdate, FIELD_BRUSHING_GOAL_TIME, remoteValue, newValue);

    jobService.onProfileUpdatedLocally(localProfile, remoteProfile, offlineUpdate);

    verify(editProfileData).setBrushingTime(newValue);
  }

  private void prepareForSyncableFields(
      ProfileInternal remoteProfile, OfflineUpdateInternal offlineUpdate) {
    when(remoteProfile.getPoints()).thenReturn(42);

    ArrayList<SyncableField> list = new ArrayList<>();
    when(offlineUpdate.getSyncableFields()).thenReturn(list);

    ProfileDatastore profileDatastore = mock(ProfileDatastore.class);
    when(profileDatastore.updateProfile(any())).thenReturn(Completable.complete());
    doReturn(profileDatastore).when(jobService).profileDatastore();
  }

  private void prepareStringSyncableField(
      OfflineUpdateInternal offlineUpdate,
      String fieldName,
      String snapshotValue,
      String newValue) {
    StringSyncableField syncableField = new StringSyncableField(fieldName, snapshotValue, newValue);
    when(offlineUpdate.getUpdateForField(fieldName)).thenReturn(syncableField);
  }

  private void prepareIntegerSyncableField(
      OfflineUpdateInternal offlineUpdate, String fieldName, int snapshotValue, int newValue) {
    IntegerSyncableField syncableField =
        new IntegerSyncableField(fieldName, snapshotValue, newValue);
    when(offlineUpdate.getUpdateForField(fieldName)).thenReturn(syncableField);
  }

  /*
  REFRESH AVATAR CACHE
   */
  @Test
  public void refreshAvatarCache_emptyProfiles_doesNothing() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    mockConnector(currentAccount);

    when(currentAccount.getInternalProfiles()).thenReturn(new ArrayList<>());

    AvatarCache avatarCache = mock(AvatarCache.class);
    jobService.avatarCache = avatarCache;

    jobService.refreshAvatarCache();

    verify(avatarCache, never()).cache(anyLong(), anyString(), anyString());
  }

  @SuppressWarnings("ConstantConditions")
  @Test
  public void refreshAvatarCache_withProfiles_invokesLoadAsyncForEachProfileWithPicture() {
    AccountInternal currentAccount = mock(AccountInternal.class);
    mockConnector(currentAccount);

    Long profileId1 = 123L;
    Long profileId2 = 456L;
    Long profileId3 = 789L;

    String expectedPictureUrl1 = null;
    String expectedPictureUrl2 = "";
    String expectedPictureUrl3 = "myUrl";

    String pictureLastModifier1 = "modifier 1";
    String pictureLastModifier2 = "modifier 2";
    String pictureLastModifier3 = "modifier 3";

    ProfileInternal profile1 = mock(ProfileInternal.class);
    ProfileInternal profile2 = mock(ProfileInternal.class);
    ProfileInternal profile3 = mock(ProfileInternal.class);
    when(currentAccount.getInternalProfiles())
        .thenReturn(Arrays.asList(profile1, profile2, profile3));

    when(profile1.getId()).thenReturn(profileId1);
    when(profile2.getId()).thenReturn(profileId2);
    when(profile3.getId()).thenReturn(profileId3);

    when(profile1.getPictureUrl()).thenReturn(expectedPictureUrl1);
    when(profile2.getPictureUrl()).thenReturn(expectedPictureUrl2);
    when(profile3.getPictureUrl()).thenReturn(expectedPictureUrl3);

    when(profile1.getPictureLastModifier()).thenReturn(pictureLastModifier1);
    when(profile2.getPictureLastModifier()).thenReturn(pictureLastModifier2);
    when(profile3.getPictureLastModifier()).thenReturn(pictureLastModifier3);

    AvatarCache avatarCache = mock(AvatarCache.class);
    jobService.avatarCache = avatarCache;

    jobService.refreshAvatarCache();

    verify(avatarCache).cache(profileId1, expectedPictureUrl1, pictureLastModifier1);
    verify(avatarCache).cache(profileId2, expectedPictureUrl2, pictureLastModifier2);
    verify(avatarCache).cache(profileId3, expectedPictureUrl3, pictureLastModifier3);
  }

  /*
  updateCurrentProfileIdFromLocalAccount
   */

  @Test
  public void updateCurrentProfileIdFromLocalAccount_setsTheCurrentProfileIdFromTheCurrentOne() {
    final long currentProfileId = 1986L;
    final AccountInternal currentAccount = mock(AccountInternal.class);
    when(currentAccount.getCurrentProfileId()).thenReturn(currentProfileId);
    jobService.currentAccount = currentAccount;

    final AccountInternal remoteAccount = mock(AccountInternal.class);

    jobService.updateCurrentProfileIdFromLocalAccount(remoteAccount);

    verify(remoteAccount).setCurrentProfileId(currentProfileId);
  }

  @Test
  public void synchronizeProfiles_setCurrentProfileIdToCorrectAccountInstance() {
    Long currentProfileId = 1L;

    AccountInternal remoteAccount = new AccountInternal();
    AccountInternal updatedRemoteAccount = new AccountInternal();

    jobService.currentAccount = new AccountInternal();
    jobService.currentAccount.setCurrentProfileId(currentProfileId);

    doReturn(remoteAccount).when(jobService).getRemoteAccount();
    doReturn(updatedRemoteAccount).when(jobService).remoteOrDefault(remoteAccount);

    doNothing().when(jobService).checkForNewOrDeletedProfiles(null);
    doNothing().when(jobService).uploadAvatarIfUpdatedWhileOffline(null);

    mockConnector();
    jobService.synchronizeProfiles();
    assertEquals(currentProfileId, updatedRemoteAccount.getCurrentProfileId());
  }

  private void mockConnector() {
    mockConnector(null);
  }

  private void mockConnector(AccountInternal localAccount) {
    connector = mock(InternalKolibreeConnector.class);
    jobService.kolibreeConnector = connector;
    if (localAccount != null) {
      doReturn(localAccount).when(connector).currentAccount();
    }
  }
}
