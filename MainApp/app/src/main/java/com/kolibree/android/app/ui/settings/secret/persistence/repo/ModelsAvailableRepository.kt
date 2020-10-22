package com.kolibree.android.app.ui.settings.secret.persistence.repo

import com.kolibree.android.app.ui.settings.secret.persistence.ModelAvailable
import com.kolibree.android.commons.ToothbrushModel
import io.reactivex.Completable
import io.reactivex.Flowable

/**
 * Created by Guillaume Agis on 08/11/2018.
 */
internal interface ModelsAvailableRepository {
    fun deleteAll(): Completable?
    fun getModelsAvailable(): Flowable<List<ModelAvailable>>
    fun addModels(all: List<ToothbrushModel>, available: List<ToothbrushModel>)
    fun modelNotAvailable(model: ToothbrushModel): Completable
    fun modelAvailable(model: ToothbrushModel): Completable
    fun getAllModels(): List<ModelAvailable>
    fun isAvailable(model: ToothbrushModel): Boolean
}
