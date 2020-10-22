/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.persistence;

import androidx.annotation.NonNull;
import com.kolibree.android.offlinebrushings.OrphanBrushing;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.List;
import javax.inject.Inject;
import org.jetbrains.annotations.NotNull;

/**
 * Wrapper around RecordedSessionDao
 *
 * <p>Created by miguelaragues on 20/11/17.
 */
class OrphanBrushingRepositoryRoom implements OrphanBrushingRepository {

  final OrphanBrushingDao orphanBrushingDao;

  @Inject
  OrphanBrushingRepositoryRoom(OrphanBrushingDao orphanBrushingDao) {
    this.orphanBrushingDao = orphanBrushingDao;
  }

  @NonNull
  @Override
  public Flowable<Integer> count() {
    return orphanBrushingDao.count();
  }

  @NonNull
  @Override
  public Single<OrphanBrushing> read(long id) {
    return orphanBrushingDao.read(id);
  }

  @NonNull
  @Override
  public Flowable<List<OrphanBrushing>> readAll() {
    return orphanBrushingDao.readAll();
  }

  @Override
  public long insert(OrphanBrushing orphanBrushing) {
    long insertedId = orphanBrushingDao.insert(orphanBrushing);

    orphanBrushing.setId(insertedId);

    return insertedId;
  }

  @Override
  public void delete(List<OrphanBrushing> orphanBrushing) {
    orphanBrushingDao.delete(orphanBrushing);
  }

  @Override
  public void update(List<OrphanBrushing> orphanBrushing) {
    orphanBrushingDao.update(orphanBrushing);
  }

  @Override
  public void delete(OrphanBrushing... orphanBrushings) {
    orphanBrushingDao.delete(orphanBrushings);
  }

  @NotNull
  @Override
  public Completable truncate() {
    return orphanBrushingDao.deleteAll();
  }
}
