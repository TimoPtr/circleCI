package com.kolibree.android.pirate

import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.annotation.VisibleForApp
import com.kolibree.android.extensions.plusAssign
import com.kolibree.android.game.BrushingCreator
import com.kolibree.android.game.GameViewState
import com.kolibree.android.game.LegacyBaseGameViewModel
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.data.model.gopirate.UpdateGoPirateData
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

@VisibleForApp
class PirateFragmentViewModel internal constructor(
    connector: IKolibreeConnector,
    provider: KLTBConnectionProvider,
    toothbrushMac: Single<String>,
    appVersions: KolibreeAppVersions,
    brushingCreator: BrushingCreator
) : LegacyBaseGameViewModel<PirateFragmentViewState>(
    connector,
    provider,
    toothbrushMac,
    appVersions,
    brushingCreator
), BasePirateFragmentViewModel {

    @VisibleForTesting
    var pirateData: UpdateGoPirateData? = null

    override fun initialViewState(): PirateFragmentViewState =
        PirateFragmentViewState(GameViewState.ACTION_NONE)

    override fun onBrushingCompleted(
        brushingData: CreateBrushingData,
        pirateData: UpdateGoPirateData?
    ) {
        this.pirateData = pirateData

        super.onBrushingCompleted(brushingData, 0)
    }

    override fun updatePirateData(pirateData: UpdateGoPirateData?) {
        this.pirateData = pirateData

        disposables += doSendPirateDataCompletable()
            .subscribe(
                {
                    // no-op
                },
                { emitSomethingWentWrong() }
            )
    }

    private fun doSendPirateDataCompletable(): Completable = Completable.defer {
        if (pirateData != null) {
            val localPirateData = pirateData
            currentProfileSingle()
                .flatMapCompletable { profile ->
                    updateGoPirateDataCompletable(profile, localPirateData)
                }
                .doOnComplete {
                    if (pirateData != null && pirateData == localPirateData) {
                        pirateData = null
                    }
                }
        } else {
            Completable.complete()
        }
    }

    private fun updateGoPirateDataCompletable(
        profile: Profile,
        localPirateData: UpdateGoPirateData?
    ): Completable {
        return Completable.fromAction {
            connector
                .withProfileId(profile.id)
                .updateGoPirateData(localPirateData!!)
        }
    }

    private fun currentProfileSingle(): Single<Profile> {
        return connector.currentProfileFlowable()
            .subscribeOn(Schedulers.io())
            .take(1)
            .singleOrError()
    }

    override fun beforeSendDataSavedCompletable(): Completable = doSendPirateDataCompletable()

    @VisibleForApp
    class Factory private constructor(
        connector: IKolibreeConnector,
        provider: KLTBConnectionProvider,
        toothbrushMac: Single<String>?,
        appVersions: KolibreeAppVersions,
        brushingCreator: BrushingCreator
    ) : InternalFactory(
        connector,
        provider,
        toothbrushMac,
        appVersions,
        brushingCreator
    ), BasePirateFragmentViewModelFactory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = PirateFragmentViewModel(
            connector,
            connectionProvider,
            toothbrushMac!!,
            appVersions,
            brushingCreator
        ) as T

        @VisibleForApp
        class Builder(
            connector: IKolibreeConnector,
            provider: KLTBConnectionProvider,
            appVersions: KolibreeAppVersions,
            brushingCreator: BrushingCreator
        ) : InternalBuilder<Factory, Builder>(connector, provider, appVersions, brushingCreator) {

            public override fun build(): Factory = Factory(
                connector,
                connectionProvider,
                toothbrushMacSingle,
                appVersions,
                brushingCreator
            )
        }
    }
}
