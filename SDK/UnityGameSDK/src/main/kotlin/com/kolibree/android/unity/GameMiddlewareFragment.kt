/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.unity

import android.os.Bundle
import androidx.annotation.Keep
import com.kolibree.android.KolibreeExperimental
import com.kolibree.android.app.ui.fragment.BaseUnityGameFragment
import com.kolibree.android.app.unity.KolibreeUnityPlayer
import com.kolibree.android.app.unity.UnityGameResult
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.gameprogress.domain.logic.GameProgressRepository
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.connection.mac
import com.kolibree.android.unity.di.DaggerGameMiddlewareComponent
import com.kolibree.game.middleware.NativeSDKInstance
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.kolibree.sdkws.core.avro.AvroFileUploader
import javax.inject.Inject
import timber.log.Timber

/**
 * Base class for fragments hosting Unity games with GameMiddleware support.
 *
 * Since we can have only one player in the active application task, Unity fragments doesn't have their own UI.
 * Instead, they delegate this to host activity, which must be of type [GameMiddlewareActivity]. From there,
 * they receive Unity player where they're executed.
 *
 * Hosts game middleware dependencies which are tied to this fragment's lifecycle.
 *
 *
 * TODO when the initial version is done, migrate to proper MVI structure
 *  https://kolibree.atlassian.net/browse/KLTB002-9389
 */
@Keep
@KolibreeExperimental
abstract class GameMiddlewareFragment : BaseUnityGameFragment() {

    @Inject
    internal lateinit var kolibreeConnector: InternalKolibreeConnector

    @Inject
    internal lateinit var gameProgressRepository: GameProgressRepository

    @Inject
    internal lateinit var avroFileUploader: AvroFileUploader

    /**
     * Holds all game middleware related dependencies provided by [GameMiddlewareComponent],
     * to remove collisions with injection in [BaseUnityGameFragment].
     */
    internal class DependencyProvider {

        @Inject
        lateinit var nativeSDKInstance: NativeSDKInstance
    }

    private val dependencyProvider = DependencyProvider()

    override val activity: GameMiddlewareActivity?
        get() {
            validateActivity()
            return super.activity as? GameMiddlewareActivity
        }

    override fun onCreateInternal(savedInstanceState: Bundle?) {
        super.onCreateInternal(savedInstanceState)
        setupDependencies()
    }

    override fun onDestroyInternal() {
        cleanupDependencies()
        super.onDestroyInternal()
    }

    private fun setupDependencies() {
        instantiateDependencies(
            onSuccess = { dependencyProvider ->
                KolibreeUnityPlayer.nativeSDKInstance = dependencyProvider.nativeSDKInstance
            },
            onFailure = { throwable ->
                Timber.e(throwable)
                finishGame(UnityGameResult<Void>(game = game(), success = false))
            }
        )
    }

    private fun cleanupDependencies() {
        KolibreeUnityPlayer.nativeSDKInstance = null
    }

    private inline fun instantiateDependencies(
        crossinline onSuccess: (DependencyProvider) -> Unit,
        onFailure: (Throwable) -> Unit
    ) {
        val chosenToothbrushMac = arguments?.getString(ARGUMENT_KEY_MAC)
        val chosenToothbrushModel = arguments?.getString(ARGUMENT_KEY_MODEL)

        unityHostActivity()?.kolibreeServiceInteractor?.service?.knownConnections?.let { knownConnections ->
            if (knownConnections.isEmpty()) {
                onFailure(IllegalStateException("No knownConnections available!"))
                return
            }

            val chosenConnection = knownConnections.firstOrNull {
                it.mac() == chosenToothbrushMac &&
                    it.toothbrush().model.internalName == chosenToothbrushModel
            }

            if (chosenConnection == null) {
                onFailure(IllegalStateException("Connection for MAC $chosenToothbrushMac not available!"))
                return
            }

            // TODO convert to subcomponent
            // https://kolibree.atlassian.net/browse/KLTB002-9389
            DaggerGameMiddlewareComponent.factory().create(
                context = requireContext(),
                kltbConnection = chosenConnection,
                kolibreeConnector = kolibreeConnector,
                gameProgressRepository = gameProgressRepository,
                lifecycle = lifecycle, // Usage of UnityPlayerLifecycleActivity is strictly forbidden
                avroFileUploader = avroFileUploader
            ).inject(dependencyProvider)

            onSuccess(dependencyProvider)
        } ?: onFailure(IllegalStateException("No knownConnections available!"))
    }

    private fun validateActivity() {
        super.activity?.let {
            FailEarly.failInConditionMet(
                condition = it !is GameMiddlewareActivity,
                message = "Unity game with GameMiddleware requires GameMiddlewareActivity as " +
                    "a host in order to run, but found ${super.activity?.javaClass}, " +
                    "please check your architecture!"
            ) {
                it.finish()
            }
        }
    }

    override fun setChosenConnection(connection: KLTBConnection) {
        if (arguments == null) arguments = Bundle()

        arguments?.apply {
            putString(ARGUMENT_KEY_MAC, connection.mac())
            putString(ARGUMENT_KEY_MODEL, connection.toothbrush().model.internalName)
        }
    }
}

private const val ARGUMENT_KEY_MAC = "GameMiddlewareFragment.MAC"
private const val ARGUMENT_KEY_MODEL = "GameMiddlewareFragment.MODEL"
