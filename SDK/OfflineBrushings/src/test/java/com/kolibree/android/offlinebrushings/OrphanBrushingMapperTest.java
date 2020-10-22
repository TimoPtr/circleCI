package com.kolibree.android.offlinebrushings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.android.app.test.BaseUnitTest;
import com.kolibree.android.clock.TrustedClock;
import com.kolibree.android.offlinebrushings.persistence.OrphanBrushingRepository;
import com.kolibree.android.processedbrushings.CheckupCalculator;
import com.kolibree.android.utils.KolibreeAppVersions;
import com.kolibree.sdkws.core.IKolibreeConnector;
import com.kolibree.sdkws.core.ProfileWrapper;
import com.kolibree.sdkws.data.model.CreateBrushingData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

/** Created by miguelaragues on 23/10/17. */
public class OrphanBrushingMapperTest extends BaseUnitTest {

  @Mock IKolibreeConnector connector;

  @Mock OrphanBrushingRepository orphanBrushingRepository;

  @Mock CheckupCalculator checkupCalculator;

  KolibreeAppVersions appVersions = new KolibreeAppVersions("1.0", "2");

  OrphanBrushingMapper mapper;

  @Override
  public void setup() throws Exception {
    super.setup();

    mapper =
        spy(
            new OrphanBrushingMapper(
                connector, orphanBrushingRepository, appVersions, checkupCalculator));
  }

  private OrphanBrushing brushing() {
    OrphanBrushing brushing = mock(OrphanBrushing.class);
    return brushing;
  }

  /*
  DELETE
   */
  @Test
  public void delete_doRemoteDeleteSuccess_invokesDoDBDelete() {
    OrphanBrushing synched1 = brushing();
    OrphanBrushing synched2 = brushing();
    OrphanBrushing notSynched1 = brushing();
    OrphanBrushing notSynched2 = brushing();

    List<OrphanBrushing> initialList = Arrays.asList(synched1, synched2, notSynched1, notSynched2);

    List<OrphanBrushing> unsynchedList = Arrays.asList(notSynched1, notSynched2);
    doReturn(unsynchedList).when(mapper).extractUnsynchedBrushings(eq(initialList));

    List<OrphanBrushing> synchedList = Arrays.asList(synched1, synched2);
    doReturn(synchedList).when(mapper).brushingsToUpdate(eq(initialList));

    doReturn(true).when(mapper).doRemoteDelete(eq(synchedList));

    mapper.delete(initialList).test();

    verify(mapper).doLocalDelete(eq(unsynchedList));

    verify(mapper).doRemoteDelete(eq(synchedList));

    verify(mapper).doLocalDelete(eq(synchedList));
  }

  @Test
  public void delete_doRemoteDeleteFailure_neverInvokesDoDBDelete() {
    OrphanBrushing synched1 = brushing();
    OrphanBrushing synched2 = brushing();
    OrphanBrushing notSynched1 = brushing();
    OrphanBrushing notSynched2 = brushing();

    List<OrphanBrushing> initialList = Arrays.asList(synched1, synched2, notSynched1, notSynched2);

    List<OrphanBrushing> unsynchedList = Arrays.asList(notSynched1, notSynched2);
    doReturn(unsynchedList).when(mapper).extractUnsynchedBrushings(eq(initialList));

    List<OrphanBrushing> synchedList = Arrays.asList(synched1, synched2);
    doReturn(synchedList).when(mapper).brushingsToUpdate(eq(initialList));

    doReturn(false).when(mapper).doRemoteDelete(eq(synchedList));

    mapper.delete(initialList).test();

    verify(mapper).doLocalDelete(eq(unsynchedList));

    verify(mapper).flagBrushingsAsDeleted(eq(synchedList));

    verify(mapper).doRemoteDelete(eq(synchedList));

    verify(mapper, never()).doLocalDelete(eq(synchedList));
  }

  /*
  FLAG BRUSHINGS AS DELETED
   */
  @Test
  public void flagBrushingsAsDeleted_invokesDaoDelete() {
    OrphanBrushing synched1 = brushing();
    OrphanBrushing synched2 = brushing();

    List<OrphanBrushing> initialList = Arrays.asList(synched1, synched2);
    mapper.flagBrushingsAsDeleted(initialList);

    ArgumentCaptor<List<OrphanBrushing>> captor = ArgumentCaptor.forClass(List.class);
    verify(orphanBrushingRepository).update(captor.capture());

    verify(synched1).setDeletedLocally(true);
    verify(synched2).setDeletedLocally(true);

    List<OrphanBrushing> capturedList = captor.getValue();
    assertEquals(2, capturedList.size());

    assertTrue(capturedList.contains(synched1));
    assertTrue(capturedList.contains(synched2));
  }

  /*
  DO REMOTE DELETE

  Ignored until backend endpoint for orphan brushings is created
   */
  @Test
  public void doRemoteDelete_emptyList_returnsFalse() {
    assertFalse(mapper.doRemoteDelete(Collections.emptyList()));
  }

  /*
  This is a temporary test until backend creates an endpoint for orphan brushings
   */
  @Test
  public void doRemoteDelete_notEmptyList_returnsTrue() {
    assertTrue(mapper.doRemoteDelete(Collections.singletonList(brushing())));
  }

  @Test
  @Ignore("Ignored until backend endpoint for orphan brushings is created")
  public void doRemoteDelete_invokesDeleteBrushings() {
    /*OrphanBrushing one = brushing();
    OrphanBrushing two = brushing();

    IKolibreeConnector connector = mock(IKolibreeConnector.class);
    when(connector.connector()).thenReturn(connector);

    List<OrphanBrushing> expectedList = Arrays.asList(one, two);
    mapper.doRemoteDelete(expectedList);

    verify(connector).deleteBrushings(eq(expectedList));*/
  }

  @Test
  @Ignore("Ignored until backend endpoint for orphan brushings is created")
  public void doRemoteDelete_returnsConnectorDeleteBrushingsValue() {
    /*OrphanBrushing one = brushing();
    OrphanBrushing two = brushing();

    IKolibreeConnector connector = mock(IKolibreeConnector.class);
    when(connector.connector()).thenReturn(connector);

    List<OrphanBrushing> expectedList = Arrays.asList(one, two);

    when(connector.deleteBrushings(eq(expectedList))).thenReturn(true);

    assertTrue(mapper.doRemoteDelete(expectedList));

    when(connector.deleteBrushings(eq(expectedList))).thenReturn(false);

    assertFalse(mapper.doRemoteDelete(expectedList));*/
  }

  /*
  DO LOCAL DELETE
   */
  @Test
  public void doLocalDelete_invokesDeleteBrushings() {
    OrphanBrushing one = brushing();
    OrphanBrushing two = brushing();

    List<OrphanBrushing> expectedList = Arrays.asList(one, two);
    mapper.doLocalDelete(expectedList);

    verify(orphanBrushingRepository).delete(eq(expectedList));
  }

  /*
  ASSIGN TARGET DURATION
   */
  @Test
  public void updateWithProfileData_singleBrushing_returnsNewBrushingWithGoalDurationUpdated() {
    long profileId = 43L;
    int oldGoalDuration1 = 34;
    int oldGoalDuration2 = 9;
    OrphanBrushing brushing1 =
        OrphanBrushing.create(0, oldGoalDuration1, "", TrustedClock.getNowOffsetDateTime(), "", "");
    OrphanBrushing brushing2 =
        OrphanBrushing.create(0, oldGoalDuration2, "", TrustedClock.getNowOffsetDateTime(), "", "");

    int expectedDuration = 120;
    Profile profile = mock(Profile.class);
    when(profile.getBrushingGoalTime()).thenReturn(expectedDuration);

    when(connector.getProfileWithId(eq(profileId))).thenReturn(profile);

    List<OrphanBrushing> newBrushings =
        mapper.updateWithProfileData(profileId, Arrays.asList(brushing1, brushing2));

    for (OrphanBrushing brushing : newBrushings) {
      assertEquals(expectedDuration, brushing.getGoalDuration());
    }
  }

  /*
  ASSIGN
   */
  @Test
  public void assign_invokesUpdateWithProfileData() {
    OrphanBrushing synched1 = brushing();
    OrphanBrushing synched2 = brushing();

    List<OrphanBrushing> initialList = Arrays.asList(synched1, synched2);
    long profileId = 94L;

    doReturn(initialList).when(mapper).updateWithProfileData(eq(profileId), eq(initialList));

    doNothing().when(mapper).doRemoteAssign(anyList());

    mapper.assign(profileId, initialList).test();

    verify(mapper).updateWithProfileData(eq(profileId), eq(initialList));
  }

  @Test
  public void assign_passesListWithUpdatedProfileDataToDoRemoteAssign() {
    long profileId = 12L;

    doNothing().when(mapper).doRemoteAssign(anyList());

    List<OrphanBrushing> expectedList = new ArrayList<>();
    doReturn(expectedList).when(mapper).updateWithProfileData(eq(profileId), anyList());

    mapper.assign(profileId, Collections.emptyList()).test();

    verify(mapper).doRemoteAssign(eq(expectedList));
  }

  /*
  ON SUCCESSFUL REMOTE ASSIGN
   */
  @Test
  public void onOrphanAssignedRemotely_invokesOrphanRepositoryDelete() {
    OrphanBrushing orphanBrushing = mock(OrphanBrushing.class);
    mapper.onOrphanAssignedRemotely(orphanBrushing);

    verify(orphanBrushingRepository).delete(eq(orphanBrushing));
  }

  /*
  DO REMOTE ASSIGN
   */
  @Test
  public void doRemoteAssign_readsFromOrphanBrushingsAndCreatesBrushing() {
    OrphanBrushing brushing1 = mock(OrphanBrushing.class);
    CreateBrushingData brushingData1 = mock(CreateBrushingData.class);
    when(brushing1.toCreateBrushingData(appVersions, checkupCalculator)).thenReturn(brushingData1);

    OrphanBrushing brushing2 = mock(OrphanBrushing.class);
    CreateBrushingData brushingData2 = mock(CreateBrushingData.class);
    when(brushing2.toCreateBrushingData(appVersions, checkupCalculator)).thenReturn(brushingData2);

    List<OrphanBrushing> brushings = Arrays.asList(brushing1, brushing2);

    long profileId = 94L;

    when(brushing2.getAssignedProfileId()).thenReturn(profileId);
    when(brushing1.getAssignedProfileId()).thenReturn(profileId);

    ProfileWrapper profileWrapper = mock(ProfileWrapper.class);
    when(connector.withProfileId(eq(profileId))).thenReturn(profileWrapper);

    mapper.doRemoteAssign(brushings);

    ArgumentCaptor<CreateBrushingData> captor = ArgumentCaptor.forClass(CreateBrushingData.class);
    verify(profileWrapper, times(brushings.size())).createBrushing(captor.capture());

    assertEquals(brushingData1, captor.getAllValues().get(0));
    assertEquals(brushingData2, captor.getAllValues().get(1));
  }

  @Test
  public void doRemoteAssign_assignSuccess_invokesOnOrphanAssignedRemotely() { // no way to test
    // assignFailure for now
    OrphanBrushing orphanBrushing = mock(OrphanBrushing.class);
    long profileId = 94L;
    when(orphanBrushing.getAssignedProfileId()).thenReturn(profileId);

    ProfileWrapper profileWrapper = mock(ProfileWrapper.class);
    when(connector.withProfileId(eq(profileId))).thenReturn(profileWrapper);

    doNothing().when(mapper).onOrphanAssignedRemotely(any(OrphanBrushing.class));

    mapper.doRemoteAssign(Collections.singletonList(orphanBrushing));

    verify(mapper).onOrphanAssignedRemotely(eq(orphanBrushing));
  }

  /*
  CREATE OFFLINE BRUSHINGS
   */
  @Test
  public void assignUnsynchedOrphanBrushings_invokesAssociate() {
    OrphanBrushing notsynched1 = brushing();
    OrphanBrushing notsynched2 = brushing();

    OrphanBrushing[] expectedBrushings = new OrphanBrushing[] {notsynched1, notsynched2};

    long profileId = 94L;
    List<OrphanBrushing> expectedList = Arrays.asList(expectedBrushings);
    mapper.doLocalAssign(profileId, expectedList);

    verify(notsynched1).setAssignedProfileId(eq(profileId));
    verify(notsynched2).setAssignedProfileId(eq(profileId));

    verify(orphanBrushingRepository).update(eq(expectedList));
  }

  /*
  EXTRACT UNSYNCHED BRUSHINGS
   */
  @Test
  public void extractUnsynchedBrushings_returnsArrayWithIdsNotsynchedInDb() {
    OrphanBrushing synched1 = brushing();
    when(synched1.isUploaded()).thenReturn(true);
    OrphanBrushing synched2 = brushing();
    when(synched2.isUploaded()).thenReturn(true);
    OrphanBrushing notsynched1 = brushing();
    OrphanBrushing notsynched2 = brushing();

    OrphanBrushing[] expectedBrushindTimestamps = new OrphanBrushing[] {notsynched1, notsynched2};

    List<OrphanBrushing> unsynchedList =
        mapper.extractUnsynchedBrushings(
            Arrays.asList(synched1, notsynched1, notsynched2, synched2));

    assertTrue(unsynchedList.contains(notsynched1));
    assertTrue(unsynchedList.contains(notsynched2));
    assertFalse(unsynchedList.contains(synched1));
    assertFalse(unsynchedList.contains(synched2));
  }

  /*
  BRUSHINGS TO UPDATE
   */
  @Test
  public void brushingsToUpdate_returnsListWithSynchedBrushings() {
    OrphanBrushing synched1 = brushing();
    when(synched1.isUploaded()).thenReturn(true);
    OrphanBrushing synched2 = brushing();
    when(synched2.isUploaded()).thenReturn(true);
    OrphanBrushing notsynched1 = brushing();
    OrphanBrushing notsynched2 = brushing();

    List<OrphanBrushing> synchedList =
        mapper.brushingsToUpdate(Arrays.asList(synched1, notsynched1, notsynched2, synched2));

    assertTrue(synchedList.contains(synched1));
    assertTrue(synchedList.contains(synched2));
    assertFalse(synchedList.contains(notsynched1));
    assertFalse(synchedList.contains(notsynched2));
  }
}
