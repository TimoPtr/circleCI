package com.kolibree.android.offlinebrushings;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import com.kolibree.android.accountinternal.profile.models.Profile;
import com.kolibree.android.offlinebrushings.persistence.OrphanBrushingRepository;
import com.kolibree.android.processedbrushings.CheckupCalculator;
import com.kolibree.android.utils.KolibreeAppVersions;
import com.kolibree.sdkws.core.IKolibreeConnector;
import io.reactivex.Completable;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import timber.log.Timber;

/**
 * Mapper between orphan Brushings and the storage layer, either remote or local
 *
 * <p>Created by miguelaragues on 23/10/17.
 */
@Keep
public class OrphanBrushingMapper {

  private final IKolibreeConnector connector;
  private final OrphanBrushingRepository orphanBrushingRepository;
  private final KolibreeAppVersions appVersions;
  private final CheckupCalculator checkupCalculator;

  @Inject
  OrphanBrushingMapper(
      IKolibreeConnector connector,
      OrphanBrushingRepository orphanBrushingRepository,
      KolibreeAppVersions appVersions,
      CheckupCalculator checkupCalculator) {
    this.connector = connector;
    this.orphanBrushingRepository = orphanBrushingRepository;
    this.checkupCalculator = checkupCalculator;
    this.appVersions = appVersions;
  }

  /**
   * Delete the specified orphan brushings
   *
   * <p>This may mean:
   *
   * <p>1. Delete a brushing, for cases where the brushing had never been uploaded to the server 2.
   * Ask the server to delete th brushing, when it was already synchronized with the server
   *
   * <p>Before attempting to update synched brushings, we flag them as locally deleted
   */
  public Completable delete(List<OrphanBrushing> brushings) {
    return Completable.fromAction(
        () -> {
          List<OrphanBrushing> unsynchedBrushings = extractUnsynchedBrushings(brushings);

          doLocalDelete(unsynchedBrushings);

          List<OrphanBrushing> brushingsToUpdate = brushingsToUpdate(brushings);

          flagBrushingsAsDeleted(brushingsToUpdate);

          if (doRemoteDelete(brushingsToUpdate)) {
            doLocalDelete(brushingsToUpdate);
          }
        });
  }

  /**
   * Assigns the Orphan Brushings to the specified profileId
   *
   * <p>This may mean:
   *
   * <p>1. Creating a new Brushing, for cases where the OrphanBrushing had never been uploaded to
   * the server 2. Update an OrphanBrushing, when it was already synchronized with the server
   *
   * <p>Before attempting to update synched OrphanBrushing, we flag them as unsynchronized.
   */
  public Completable assign(long profileId, List<OrphanBrushing> brushings) {
    return Completable.fromAction(
        () -> {
          List<OrphanBrushing> brushingsWithProfileData =
              updateWithProfileData(profileId, brushings);

          doRemoteAssign(brushingsWithProfileData);
        });
  }

  /**
   * This method will either create a new brushing with the data contained in the OrphanBrushing, or
   * instruct the server to assign a given orphan brushing to a profile id
   *
   * <p>Until an endpoint for Orphan brushings is created, we'll always create a new Brushing
   */
  @VisibleForTesting
  void doRemoteAssign(List<OrphanBrushing> brushings) {
    /*
    We need to do a POST brushing create, not an assign
     */
    for (int i = 0, size = brushings.size(); i < size; i++) {
      OrphanBrushing orphanBrushing = brushings.get(i);

      if (orphanBrushing.getAssignedProfileId() == null) {
        Timber.w("Attempted to assign an orphan brushing without profile id assigned");
        continue;
      }

      connector
          .withProfileId(orphanBrushing.getAssignedProfileId())
          .createBrushing(orphanBrushing.toCreateBrushingData(appVersions, checkupCalculator));

      onOrphanAssignedRemotely(orphanBrushing);
    }
  }

  @VisibleForTesting
  void onOrphanAssignedRemotely(OrphanBrushing orphanBrushing) {
    orphanBrushingRepository.delete(orphanBrushing);
  }

  /**
   * Updates DB record with <code>brushingsToAssign.timestamp</code> and sets the profile owner to
   * <code>profileId</code>
   */
  @VisibleForTesting
  void doLocalAssign(long profileId, List<OrphanBrushing> brushings) {
    if (!brushings.isEmpty()) {
      for (int i = 0; i < brushings.size(); i++) {
        brushings.get(i).setAssignedProfileId(profileId);
      }

      orphanBrushingRepository.update(brushings);
    }
  }

  /**
   * Creates an array containing all brushing ids that need to be updated, which are those that are
   * already present in the server
   *
   * @return an array with the brushings to be updated. Can be empty.
   */
  @VisibleForTesting
  @WorkerThread
  List<OrphanBrushing> brushingsToUpdate(List<OrphanBrushing> brushings) {
    List<OrphanBrushing> brushingsToUpdate = new ArrayList<>();
    for (int i = 0; i < brushings.size(); i++) {
      if (brushings.get(i).isUploaded()) {
        brushingsToUpdate.add(brushings.get(i));
      }
    }

    return brushingsToUpdate;
  }

  /**
   * Creates an array containing all brushings that aren't present in the server, only in local
   * storage
   *
   * @return an array with the brushings that are only local. Can be empty.
   */
  @VisibleForTesting
  @WorkerThread
  List<OrphanBrushing> extractUnsynchedBrushings(List<OrphanBrushing> brushings) {
    List<OrphanBrushing> unsynchedBrushings = new ArrayList<>();
    for (int i = 0; i < brushings.size(); i++) {
      if (!brushings.get(i).isUploaded()) {
        unsynchedBrushings.add(brushings.get(i));
      }
    }

    return unsynchedBrushings;
  }

  @VisibleForTesting
  void flagBrushingsAsDeleted(List<OrphanBrushing> brushings) {
    for (int i = 0; i < brushings.size(); i++) {
      brushings.get(i).setDeletedLocally(true);
    }

    orphanBrushingRepository.update(brushings);
  }

  @VisibleForTesting
  boolean doRemoteDelete(List<OrphanBrushing> brushingsToDelete) {
    return !brushingsToDelete.isEmpty();
  }

  @VisibleForTesting
  void doLocalDelete(List<OrphanBrushing> brushings) {
    if (!brushings.isEmpty()) {
      orphanBrushingRepository.delete(brushings);
    }
  }

  @VisibleForTesting
  @NonNull
  List<OrphanBrushing> updateWithProfileData(long newProfileId, List<OrphanBrushing> brushings) {
    List<OrphanBrushing> newBrushings = new ArrayList<>();

    Profile newProfile = connector.getProfileWithId(newProfileId);
    if (newProfile != null) {
      final int newGoalTime = newProfile.getBrushingGoalTime();
      for (int i = 0, size = brushings.size(); i < size; i++) {
        OrphanBrushing brushing = brushings.get(i);

        brushing.setGoalDuration(newGoalTime);

        brushing.setAssignedProfileId(newProfileId);

        newBrushings.add(brushing);
      }
    }

    return newBrushings;
  }
}
