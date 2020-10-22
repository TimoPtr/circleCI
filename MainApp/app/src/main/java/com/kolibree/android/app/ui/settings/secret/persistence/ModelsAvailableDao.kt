/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.app.ui.settings.secret.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.kolibree.android.commons.ToothbrushModel
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single

/**
 * Created by guillaumeagis on 13/05/18.
 * Store stats into Room
 */

@Dao
internal interface ModelsAvailableDao {

    @Query("SELECT * FROM models_available where is_available = 1")
    fun getModelsAvailable(): Flowable<List<ModelAvailable>>

    @Query("SELECT * FROM models_available WHERE model =:model")
    fun getModel(model: ToothbrushModel): Maybe<ModelAvailable>

    @Query("SELECT * FROM models_available")
    fun getAllModels(): Single<List<ModelAvailable>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(entity: ModelAvailable): Long

    @Query("UPDATE models_available SET is_available =:isAvailable WHERE model =:model")
    fun update(model: ToothbrushModel, isAvailable: Boolean)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(param: List<ModelAvailable>): List<Long>

    @Query("DELETE FROM models_available")
    fun deleteAll()

    @Query("DELETE FROM models_available where model = :model")
    fun delete(model: ToothbrushModel)
}
