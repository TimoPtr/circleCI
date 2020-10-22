package com.kolibree.android.app.ui.settings.secret.persistence.repo

import com.kolibree.android.app.ui.settings.secret.persistence.InstallationFlags
import com.kolibree.android.app.ui.settings.secret.persistence.InstallationFlags.Flag
import com.kolibree.android.app.ui.settings.secret.persistence.ModelAvailable
import com.kolibree.android.app.ui.settings.secret.persistence.ModelsAvailableDao
import com.kolibree.android.commons.ToothbrushModel
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

internal class ModelsAvailableRepositoryImpl
@Inject internal constructor(
    private val dao: ModelsAvailableDao,
    private val installationFlags: InstallationFlags
) : ModelsAvailableRepository {

    override fun modelAvailable(model: ToothbrushModel) = Completable.fromCallable {
        dao.update(model, true)
    }

    override fun modelNotAvailable(model: ToothbrushModel) =
        Completable.fromCallable { dao.update(model, false) }

    override fun isAvailable(model: ToothbrushModel): Boolean {
        return dao.getModel(model)
            .subscribeOn(Schedulers.io())
            .map {
                it.isAvailable
            }.defaultIfEmpty(false).blockingGet()
    }

    override fun addModels(all: List<ToothbrushModel>, available: List<ToothbrushModel>) {
        Completable.fromAction {
            val currentSize = dao.getAllModels().blockingGet().size
            val expectedSize = all.size
            if (currentSize != expectedSize ||
                installationFlags.needsToBeHandled(Flag.MAKE_ARA_VISIBLE_BY_DEFAULT)) {
                dao.deleteAll()
                dao.insertAll(all.map {
                    ModelAvailable(
                        model = it,
                        isAvailable = isToothbrushAvailable(it, available)
                    )
                })
                installationFlags.setHandled(Flag.MAKE_ARA_VISIBLE_BY_DEFAULT)
            }
        }
            .subscribeOn(Schedulers.io())
            .blockingAwait()
    }

    private fun isToothbrushAvailable(model: ToothbrushModel, available: List<ToothbrushModel>) =
        available.firstOrNull { it == model } != null

    /**
     * Delete all entry in the DB
     */
    override fun deleteAll() = Completable.fromCallable { dao.deleteAll() }

    /**
     * Get all models available non deleted stored locally
     *
     * @return a non-null ArrayList
     */
    override fun getModelsAvailable(): Flowable<List<ModelAvailable>> {
        return dao.getModelsAvailable()
    }

    override fun getAllModels(): List<ModelAvailable> {
        val res = dao.getAllModels()
            .subscribeOn(Schedulers.io())
            .blockingGet()
        return res
    }
}
