/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.offlinebrushings.persistence;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.kolibree.android.offlinebrushings.OrphanBrushing;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Single;
import java.util.List;

/** Created by miguelaragues on 28/11/17. */
@Dao
public abstract class OrphanBrushingDao {

  @Query("SELECT * FROM orphan_brushing WHERE id = :id")
  public abstract Single<OrphanBrushing> read(long id);

  @Query("SELECT * FROM orphan_brushing")
  public abstract Flowable<List<OrphanBrushing>> readAll();

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  public abstract long insert(OrphanBrushing orphanBrushing);

  @Delete
  public abstract void delete(OrphanBrushing... orphanBrushing);

  @Delete
  public abstract void delete(List<OrphanBrushing> orphanBrushing);

  @Update
  public abstract void update(OrphanBrushing... orphanBrushing);

  @Update
  public abstract void update(List<OrphanBrushing> orphanBrushing);

  @Query("SELECT COUNT(*) FROM orphan_brushing")
  public abstract Flowable<Integer> count();

  @Query("DELETE FROM orphan_brushing")
  abstract Completable deleteAll();
}
