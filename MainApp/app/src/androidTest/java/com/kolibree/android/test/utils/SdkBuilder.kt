/*
 * Copyright (c) 2017 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils

import com.jakewharton.rxrelay2.BehaviorRelay
import com.kolibree.android.accountinternal.account.ParentalConsent
import com.kolibree.android.accountinternal.internal.AccountInternal
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.accountinternal.profile.persistence.models.ProfileInternal
import com.kolibree.android.app.BaseKolibreeApplication
import com.kolibree.android.app.dagger.EspressoAppComponent
import com.kolibree.android.commons.AvailableUpdate
import com.kolibree.android.commons.UpdateType.TYPE_BOOTLOADER
import com.kolibree.android.commons.UpdateType.TYPE_DSP
import com.kolibree.android.commons.UpdateType.TYPE_FIRMWARE
import com.kolibree.android.commons.UpdateType.TYPE_GRU
import com.kolibree.android.errors.NetworkNotAvailableException
import com.kolibree.android.network.api.ApiError
import com.kolibree.android.offlinebrushings.OrphanBrushing
import com.kolibree.android.offlinebrushings.sync.LastSyncData
import com.kolibree.android.offlinebrushings.sync.NeverSync
import com.kolibree.android.sdk.connection.KLTBConnection
import com.kolibree.android.sdk.core.ServiceConnected
import com.kolibree.android.sdk.scan.ToothbrushScanResult
import com.kolibree.android.test.mocks.DEFAULT_TEST_ACCOUNT_ID
import com.kolibree.android.test.mocks.KLTBConnectionBuilder
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_FW_VERSION
import com.kolibree.android.test.mocks.KLTBConnectionBuilder.Companion.DEFAULT_GRU_VERSION
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.mocks.createAccountInternal
import com.kolibree.android.test.mocks.createProfileInternal
import com.kolibree.android.test.mocks.rewards.RedeemBuilder
import com.kolibree.android.test.mocks.rewards.TransferBuilder
import com.kolibree.android.test.utils.rewards.RedeemMocker
import com.kolibree.android.test.utils.rewards.TransferMocker
import com.kolibree.pairing.session.PairingSession
import com.kolibree.sdkws.brushing.wrapper.IBrushing
import com.kolibree.sdkws.core.ProfileWrapper
import com.kolibree.sdkws.data.model.Brushing
import com.kolibree.sdkws.data.model.FacebookLoginData
import com.kolibree.sdkws.data.model.GruwareData
import com.kolibree.sdkws.data.model.LoginData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList
import java.util.Arrays
import java.util.HashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.threeten.bp.OffsetDateTime

/** Created by miguelaragues on 7/12/17.  */
class SdkBuilder private constructor() {
    private val brushingsMap = HashMap<Long, ArrayList<Brushing>>()
    private var ownerProfile: Profile? = null
    private var activeProfile: Profile? = null
    private var accountEmail: String? = null
    private var profiles: List<Profile>? = null
    private var setupMainScreen: Boolean = false
    private var shouldUpdateAppObservable: Observable<Boolean>? = null
    private var bluetoothStateObservable: Observable<Boolean>? = null
    private var numberOfBrushingsForProfile: Int? = null
    private var connections: Array<out KLTBConnection>? = null
    private var bluetoothEnabled: Boolean? = null
    private var parentalConsent: ParentalConsent? = null
    private var refreshObservable: Observable<Long>? = null
    private var toothbrushScanResult: ToothbrushScanResult? = null
    private var toothbrushScanResultRelay: BehaviorRelay<ToothbrushScanResult>? = null
    private var loginEnabled: Boolean = false
    private var supportCreateProfile: Boolean? = null
    private var remoteParentalConsentNeeded: Boolean? = null
    private var updateAccountSupport: Boolean? = null
    private var updateProfileSupport: Profile? = null
    private var supportDataCollecting: Boolean = false
    private var accountId: Long = DEFAULT_ACCOUNT_ID
    private var supportLogout: Boolean = false
    private var bluetothScanSupport: Boolean = false
    private var redeemBuilder: RedeemBuilder? = null
    private var transferBuilder: TransferBuilder? = null
    private var unityPlayerAttach = false
    private var userLoggedIn = false
    private var orphanBrushings: List<OrphanBrushing>? = null
    private var forgetConnectionSupport: Boolean = false
    private var gruwareData: GruwareData? = null
    private var amazonDrsEnabled: Boolean = false

    fun withUserLoggedIn(userLoggedIn: Boolean): SdkBuilder {
        this.userLoggedIn = userLoggedIn

        return this
    }

    fun withOwnerProfile(profile: Profile): SdkBuilder {
        ownerProfile = profile

        return this
    }

    fun withActiveProfile(profile: Profile): SdkBuilder {
        activeProfile = profile

        return this
    }

    fun withProfiles(vararg profiles: Profile): SdkBuilder {
        this.profiles = Arrays.asList(*profiles)

        return this
    }

    fun withCreateProfileSupport(succeed: Boolean): SdkBuilder {
        this.supportCreateProfile = succeed

        return this
    }

    fun withEmail(email: String): SdkBuilder {
        this.accountEmail = email

        return this
    }

    fun withBrushingsForProfile(profileId: Long, vararg brushings: Brushing): SdkBuilder {
        return withBrushingsForProfile(profileId, ArrayList(Arrays.asList(*brushings)))
    }

    fun withBrushingsForProfile(profileId: Long, brushings: List<Brushing>): SdkBuilder {
        brushingsMap[profileId] = ArrayList(brushings)

        return this
    }

    fun withOrphanBrushings(orphanBrushings: List<OrphanBrushing>): SdkBuilder {
        this.orphanBrushings = orphanBrushings

        return this
    }

    fun withNumberOfBrushingsForProfile(
        profileId: Long,
        numberOfBrushingsForProfile: Int
    ): SdkBuilder {
        this.numberOfBrushingsForProfile = numberOfBrushingsForProfile

        return this
    }

    fun withBluetoothStateObservable(bluetoothStateObservable: Observable<Boolean>): SdkBuilder {
        this.bluetoothStateObservable = bluetoothStateObservable

        return this
    }

    fun withBluetoothEnabled(bluetoothEnabled: Boolean): SdkBuilder {
        this.bluetoothEnabled = bluetoothEnabled

        return this
    }

    fun withKLTBConnections(vararg connections: KLTBConnection): SdkBuilder {
        this.connections = connections

        return this
    }

    fun withToothbrushScanResult(toothbrushScanResult: ToothbrushScanResult): SdkBuilder {
        this.toothbrushScanResult = toothbrushScanResult

        return withBluetoothScanSupport()
    }

    fun withToothbrushScanResultRelay(
        toothbrushScanResultRelay: BehaviorRelay<ToothbrushScanResult>
    ): SdkBuilder {
        this.toothbrushScanResult = toothbrushScanResultRelay.value
        this.toothbrushScanResultRelay = toothbrushScanResultRelay

        return withBluetoothScanSupport()
    }

    fun withBluetoothScanSupport(): SdkBuilder {
        this.bluetothScanSupport = true

        return withBluetoothEnabled(true)
    }

    fun withRefreshObservable(refreshObservable: Observable<Long>): SdkBuilder {
        this.refreshObservable = refreshObservable

        return this
    }

    fun withUpdateAppObservable(shouldUpdateAppObservable: Observable<Boolean>): SdkBuilder {
        this.shouldUpdateAppObservable = shouldUpdateAppObservable

        return this
    }

    fun withAmazonDrsEnabled(amazonDrsEnabled: Boolean): SdkBuilder {
        this.amazonDrsEnabled = amazonDrsEnabled

        return this
    }

    fun withGruwareData(gruwareData: GruwareData): SdkBuilder {
        this.gruwareData = gruwareData
        return this
    }

    fun prepareForMainScreen(): SdkBuilder {
        this.setupMainScreen = true

        return this
    }

    @Deprecated(
        message = "Favor withParentalConsent",
        replaceWith = ReplaceWith(expression = "withParentalConsent")
    )
    fun withParentalConsentNeeded(parentalConsentNeeded: Boolean): SdkBuilder {
        return withParentalConsent(
            if (parentalConsentNeeded) ParentalConsent.PENDING else ParentalConsent.GRANTED
        )
    }

    /**
     * This value will be overriden if you also use AppMocker.withParentalConsent
     */
    fun withParentalConsent(parentalConsent: ParentalConsent): SdkBuilder {
        this.parentalConsent = parentalConsent

        return this
    }

    fun withRemoteParentalConsentNeeded(parentalConsentNeeded: Boolean): SdkBuilder {
        this.remoteParentalConsentNeeded = parentalConsentNeeded

        return this
    }

    fun withLoginEnabled(loginEnabled: Boolean): SdkBuilder {
        this.loginEnabled = loginEnabled

        return this
    }

    fun withUpdateAccountSupport(): SdkBuilder {
        this.updateAccountSupport = true

        return this
    }

    fun withUpdateProfileSupport(profile: Profile): SdkBuilder {
        this.updateProfileSupport = profile

        return this
    }

    fun supportLogout(): SdkBuilder {
        supportLogout = true

        return this
    }

    fun withForgetConnectionSupport(): SdkBuilder {
        forgetConnectionSupport = true

        return this
    }

    fun withAccountId(accountId: Long): SdkBuilder {
        this.accountId = accountId

        return this
    }

    fun withSupportAllowDataCollecting(): SdkBuilder {
        supportDataCollecting = true

        return this
    }

    fun withRedeemBuilder(redeemBuilder: RedeemBuilder): SdkBuilder {
        this.redeemBuilder = redeemBuilder
        return this
    }

    fun withTransferBuilder(transferBuilder: TransferBuilder): SdkBuilder {
        this.transferBuilder = transferBuilder
        return this
    }

    fun withUnityPlayerAttach(): SdkBuilder {
        unityPlayerAttach = true

        return this
    }

    /** Given all the parameters, mock the SDK  */
    fun build() {
        if (userLoggedIn) {
            setupAccountAndProfileData()
        }

        setupFacadeAndKolibreeService()

        setupParentalConsent()

        persistConnections()

        if (setupMainScreen) {
            // ensure it's called before setupRefreshObservable
            setupMainScreen()
        }

        if (supportLogout) {
            setupLogout()
        }

        if (supportDataCollecting) {
            setupDataCollecting()
        }

        if (bluetoothEnabled != null) {
            setupBluetoothUtils()
        }

        if (refreshObservable != null) {
            setupRefreshObservable()
        }

        if (shouldUpdateAppObservable != null) {
            setupUpdateAppObservable()
        }

        if (bluetothScanSupport) {
            setupToothbrushScan()
        }

        if (loginEnabled) {
            setupLogin()
        }

        if (supportCreateProfile != null) {
            supportCreateProfile()
        }

        if (updateAccountSupport != null) {
            supportUpdateAccount()
        }

        if (redeemBuilder != null) {
            setupRedeemNetworkService()
        }

        if (transferBuilder != null) {
            setupTransferNetworkService()
        }

        if (orphanBrushings != null) {
            setupOrphanBrushings()
        }

        setupUnityPlayerAttach()

        setupGetMyData()
    }

    private fun setupGetMyData() {
        whenever(component().kolibreeConnector().myData).thenReturn(Completable.complete())
    }

    private fun persistConnections() {
        if (connections != null) {
            for (connection in connections!!) {
                connection
                    .userMode()
                    .profileOrSharedModeId()
                    // if we use a standard scheduler and we've used ImmediateRxSchedulersRule, this'll
                    // always timeout
                    .timeout(
                        500,
                        TimeUnit.MILLISECONDS,
                        Schedulers.from(Executors.newSingleThreadExecutor())
                    )
                    .flatMapCompletable { profileId ->
                        component()
                            .toothbrushRepository()
                            .associate(connection.toothbrush(), profileId, accountId)
                    }
                    .blockingAwait()
            }
        }
    }

    private fun setupDataCollecting() {
        whenever(component().kolibreeConnector().allowDataCollecting(any()))
            .thenReturn(Completable.complete())
    }

    private fun setupLogout() {
        whenever(component().kolibreeConnector().logout()).thenReturn(Completable.complete())
    }

    private fun supportUpdateAccount() {
        whenever(component().kolibreeConnector().updateAccount(any(), any()))
            .thenReturn(Completable.complete())
    }

    private fun supportCreateProfile() {
        whenever(component().kolibreeConnector().createProfile(any()))
            .thenAnswer {
                if (supportCreateProfile == true) {
                    Single.just(activeProfile)
                } else {
                    Single.error(
                        ApiError(
                            "Test forced error create profile",
                            404,
                            ""
                        )
                    )
                }
            }
    }

    private fun setupRefreshObservable() {
        whenever(component().kolibreeConnector().refreshObservable).thenReturn(refreshObservable)
    }

    private fun setupUpdateAppObservable() {
        whenever(component().kolibreeConnector().isAppUpdateNeeded).thenReturn(
            shouldUpdateAppObservable
        )
    }

    private fun setupToothbrushScan() {
        var observableToothbrushScanResult: Observable<ToothbrushScanResult>
        toothbrushScanResultRelay.let { toothbrushScanResultRelay ->
            if (toothbrushScanResultRelay != null) {
                observableToothbrushScanResult = toothbrushScanResultRelay
            } else {
                observableToothbrushScanResult = Observable.create { e ->
                    toothbrushScanResult?.let { toothbrushScanResult ->
                        e.onNext(toothbrushScanResult)
                    }
                    e.onComplete()
                }
            }
        }

        if (connections != null) {
            if (toothbrushScanResult != null) {
                whenever(
                    component().kolibreeService().createAndEstablishConnection(
                        eq(
                            toothbrushScanResult!!
                        )
                    )
                )
                    .thenReturn(connections!![0])
            }

            observableToothbrushScanResult =
                observableToothbrushScanResult.map { emittedScanResult ->
                    if (emittedScanResult !== toothbrushScanResult) {
                        whenever(
                            component()
                                .kolibreeService()
                                .createAndEstablishConnection(eq(emittedScanResult))
                        )
                            .thenReturn(connections!![0])
                    }

                    emittedScanResult
                }
        }

        whenever(component().pairingAssistant().scannerObservable())
            .thenReturn(observableToothbrushScanResult)
        whenever(component().pairingAssistant().realTimeScannerObservable())
            .thenReturn(observableToothbrushScanResult.map { scanResult ->
                listOf(scanResult)
            })

        whenever(component().bluetoothUtils().deviceSupportsBle()).thenReturn(true)

        if (toothbrushScanResult != null) {
            val pairingSession = mockPairingSessionForToothbrushScanResult()

            whenever(component().pairingAssistant().pair(toothbrushScanResult!!))
                .thenReturn(Single.just(pairingSession))
        }
    }

    private fun mockPairingSessionForToothbrushScanResult(): PairingSession {
        var connectionToReturn: KLTBConnection? = null
        if (connections != null) {
            for (connection in connections!!) {
                if (connection.toothbrush().mac == toothbrushScanResult!!.mac) {
                    connectionToReturn = connection
                    break
                }
            }
        }

        if (connectionToReturn == null) {
            connectionToReturn = KLTBConnectionBuilder.createWithDefaultState()
                .withSupportForSetOperationsOnUserMode()
                .withSetNameSupport()
                .build()
        }

        val pairingSession = mock(PairingSession::class.java)
        whenever(pairingSession.connection()).thenReturn(connectionToReturn)

        return pairingSession
    }

    private fun setupMainScreen() {
        // brushhead checker
        val nbOfBrushingsSince =
            (if (numberOfBrushingsForProfile != null) numberOfBrushingsForProfile else brushingsMap.size)!!.toLong()

        whenever(component().brushingsRepository().countBrushingsSince(any(), any()))
            .thenReturn(Single.just(nbOfBrushingsSince))

        whenever(component().brushingsRepository().countBrushings(any()))
            .thenReturn(Single.just(nbOfBrushingsSince))

        whenever(component().brushingsRepository().deleteBrushing(any(), any(), any())).thenReturn(
            Completable.complete()
        )

        val associatedProfileBrushings = ArrayList<Brushing>()
        if (!brushingsMap.isEmpty()) {
            for (profileId in brushingsMap.keys) {
                for (brushing in brushingsMap[profileId]!!) {
                    associatedProfileBrushings.add(brushing)
                }
            }
            whenever(component().brushingsRepository().getBrushings(any()))
                .thenAnswer { invocation -> Single.just(brushingsMap[invocation.arguments[0] as Long]!!) }
        }

        whenever(component().brushingsRepository().getNonDeletedBrushings())
            .thenReturn(Flowable.just(associatedProfileBrushings))

        // ToolbarToothbrushFragment
        if (bluetoothStateObservable == null) {
            bluetoothStateObservable = Observable.just(true)
        }

        if (shouldUpdateAppObservable == null) {
            shouldUpdateAppObservable = Observable.just(false)
        }

        setupBluetoothUtils()

        setupLastSyncObservable()

        if (refreshObservable == null) {
            refreshObservable = Observable.empty()
        }

        if (gruwareData == null) {
            // Ota checker
            whenever(
                component()
                    .gruwareRepository()
                    .getGruwareInfo(any(), any(), any(), any())
            )
                .thenReturn(Single.error(NetworkNotAvailableException()))
        } else {
            whenever(
                component().gruwareRepository().getGruwareInfo(any(), any(), any(), any())
            ).thenReturn(
                Single.just(gruwareData)
            )
        }

        // DspAwaker. Invoked from Activity picker screen
        whenever(component().dspAwaker().keepAlive(any(), any())).thenReturn(Completable.complete())
    }

    private fun setupBluetoothUtils() {
        if (bluetoothStateObservable == null && bluetoothEnabled != null) {
            bluetoothStateObservable = Observable.just(bluetoothEnabled!!)
        }

        whenever(component().bluetoothUtils().bluetoothStateObservable())
            .thenReturn(bluetoothStateObservable)

        whenever(component().bluetoothUtils().isBluetoothEnabled)
            .thenReturn(if (bluetoothEnabled != null) bluetoothEnabled else true)
    }

    private fun setupLastSyncObservable() {
        whenever(component().lastSyncObservable().observable())
            .thenReturn(Observable.just<LastSyncData>(NeverSync("")))
        whenever(component().lastSyncObservable().getLastSyncData(any())).thenReturn(NeverSync(""))
    }

    private fun setupAccountAndProfileData() {
        if (ownerProfile != null && profiles != null && !profiles!!.contains(ownerProfile!!)) {
            throw IllegalStateException("Owner profile is not in profile list")
        }
        if (activeProfile != null && profiles != null && !profiles!!.contains(activeProfile!!)) {
            throw IllegalStateException("Active profile is not in profile list")
        }
        val kolibreeConnector = BaseKolibreeApplication.appComponent.kolibreeConnector()

        if (activeProfile == null) {
            activeProfile = if (profiles != null && profiles!!.isNotEmpty()) {
                profiles!![0]
            } else {
                ProfileBuilder.create().withId(ProfileBuilder.DEFAULT_ID).withPoints().build()
            }
        }

        if (profiles == null && activeProfile != null) {
            profiles = listOf(activeProfile!!)
        }

        if (accountEmail != null) {
            whenever(kolibreeConnector.email).thenReturn(accountEmail)
        }

        val internalProfiles = mutableListOf<ProfileInternal>()
        if (profiles != null) {
            whenever(kolibreeConnector.profileList).thenReturn(profiles!!)
            whenever(kolibreeConnector.profileListSingle).thenReturn(Single.just(profiles!!))

            whenever(kolibreeConnector.currentProfileFlowable())
                .thenReturn(
                    Flowable.defer {
                        component().currentProfileProvider().currentProfileFlowable()
                    })

            for (profile in profiles!!) {
                val profileWrapper = mock(ProfileWrapper::class.java)
                val profileId = profile.id
                var profileBrushings = brushingsMap[profileId]
                if (profileBrushings == null) {
                    profileBrushings = ArrayList()
                }
                whenever(profileWrapper.brushing).thenReturn(Single.just(profileBrushings))
                whenever(profileWrapper.isAllowedToBrush).thenReturn(true)
                whenever(profileWrapper.edit(any())).thenReturn(Single.just(true))
                whenever(profileWrapper.lastBrushingSession)
                    .thenReturn(findLastBrushingSession(profileBrushings))

                doAnswer {
                    activeProfile = profile
                    setupActiveProfileMethods(activeProfile)
                    null
                }
                    .whenever(profileWrapper)
                    .setCurrent()

                whenever(kolibreeConnector.withProfileId(eq(profile.id))).thenReturn(profileWrapper)
                whenever(kolibreeConnector.getProfileWithId(eq(profile.id))).thenReturn(profile)
                whenever(kolibreeConnector.getProfileWithIdSingle(eq(profile.id)))
                    .thenReturn(Single.just(profile))

                whenever(
                    component()
                        .brushingsRepository()
                        .getBrushingsSince(any(), eq(profileId))
                ).thenReturn(Single.just(profileBrushings))

                whenever(
                    component()
                        .brushingsRepository()
                        .brushingsFlowable(eq(profileId))
                ).thenReturn(Flowable.just(profileBrushings))

                whenever(
                    component()
                        .brushingsRepository()
                        .getBrushingsBetween(any(), any(), eq(profileId))
                ).thenAnswer {
                    val begin = it.getArgument<OffsetDateTime>(0)
                    val end = it.getArgument<OffsetDateTime>(1)

                    val single: Single<List<IBrushing>> = Single.just(profileBrushings
                        .filter { brushing ->
                            val dateTime = brushing.dateTime

                            // both begin and after are inclusive
                            (dateTime == begin || dateTime.isAfter(begin)) &&
                                (dateTime == end || dateTime.isBefore(end))
                        }
                    )

                    single
                }

                internalProfiles.add(createProfileInternal(profile, accountId.toInt()))
            }
        }

        val account = createAccount(internalProfiles)

        component().accountDatastore().setAccount(account)

        setupActiveProfileMethods(activeProfile)

        setupOwnerProfileMethods(ownerProfile ?: activeProfile)

        whenever(kolibreeConnector.accountId).thenReturn(accountId)

        component().onUserLoggedInCallback().onUserLoggedIn()

        mockProfileManager(internalProfiles)

        whenever(
            component().brushingsRepository()
                .fetchRemoteBrushings(
                    eq(accountId),
                    any(),
                    anyOrNull(),
                    anyOrNull(),
                    anyOrNull()
                )
        )
            .thenReturn(Single.just(Unit))
    }

    private fun mockProfileManager(internalProfiles: MutableList<ProfileInternal>) {
        whenever(component().profileManager().getProfileInternalLocally(any()))
            .thenAnswer { invocation ->
                val id = invocation.arguments[0] as Long
                val profileInternal = internalProfiles.find { it.id == id }
                if (profileInternal != null) {
                    Single.just(profileInternal)
                } else {
                    Single.error(Exception("Profile not found in SDKBuilder"))
                }
            }

        whenever(component().profileManager().updateOrInsertProfileLocally(any()))
            .thenAnswer { invocation -> Single.just(invocation.arguments[0] as ProfileInternal) }
        whenever(component().profileManager().getProfilesLocally())
            .thenReturn(Single.just(profiles!!))

        if (updateProfileSupport != null) {
            supportUpdateProfile()
        }
    }

    private fun createAccount(profiles: List<ProfileInternal>): AccountInternal {
        val internalProfiles = if (profiles.isEmpty()) {
            listOf(createProfileInternal(accountId = accountId, isOwnerProfile = true))
        } else {
            sanitizedProfilesWithOwner(profiles)
        }

        val ownerProfileId: Long =
            internalProfiles.firstOrNull { it.isOwnerProfile }?.id
                ?: throw IllegalArgumentException("There must be an owner")

        return createAccountInternal(
            id = accountId,
            email = accountEmail,
            parentalConsent = parentalConsent,
            profiles = internalProfiles,
            ownerProfileId = ownerProfileId
        )
    }

    private fun sanitizedProfilesWithOwner(profiles: List<ProfileInternal>): List<ProfileInternal> {
        if (profiles.isEmpty()) throw IllegalArgumentException("Profiles can't be empty")

        val ownerProfiles = profiles.count { it.isOwnerProfile }
        return if (ownerProfiles != 1) {
            // zero or multiple owner profiles, select the first of the array
            val profileListWithOwner = profiles.toMutableList()

            profileListWithOwner[0] = profiles[0].copy(isOwnerProfile = true)

            profileListWithOwner
        } else {
            profiles
        }
    }

    private fun setupActiveProfileMethods(activeProfile: Profile?) {
        val kolibreeConnector = BaseKolibreeApplication.appComponent.kolibreeConnector()

        if (activeProfile != null) {
            whenever(kolibreeConnector.currentProfile).thenReturn(activeProfile)
            val activeWrapper = kolibreeConnector.withProfileId(activeProfile.id)
            whenever(kolibreeConnector.withCurrentProfile()).thenReturn(activeWrapper)

            component()
                .accountDatastore()
                .getAccountMaybe()
                .onTerminateDetach()
                .subscribe(
                    { account ->
                        account.currentProfileId = activeProfile.id
                        component().accountDatastore().updateCurrentProfileId(account)
                    }, { it.printStackTrace() })
        }
    }

    private fun setupOwnerProfileMethods(ownerProfile: Profile?) {
        val kolibreeConnector = BaseKolibreeApplication.appComponent.kolibreeConnector()

        if (ownerProfile != null) {
            whenever(kolibreeConnector.ownerProfile).thenReturn(ownerProfile)
        }
    }

    private fun findLastBrushingSession(list: List<Brushing>): Brushing? {
        var last: Brushing? = null

        for (brushing in list) {
            if (last == null || last.dateTime > brushing.dateTime) {
                last = brushing
            }
        }

        return last
    }

    private fun setupParentalConsent() {
        val kolibreeConnector = BaseKolibreeApplication.appComponent.kolibreeConnector()

        whenever(kolibreeConnector.parentalConsentStatus())
            .thenReturn(if (parentalConsent != null) parentalConsent else ParentalConsent.GRANTED)

        if (remoteParentalConsentNeeded != null) {
            whenever(component().kolibreeConnector().needsParentalConsent(any()))
                .thenReturn(Single.just(remoteParentalConsentNeeded!!))
        }
    }

    private fun setupLogin() {
        val brushingRepository = component().brushingsRepository()
        whenever(brushingRepository.deleteAll()).thenReturn(Completable.complete())

        val connector = BaseKolibreeApplication.appComponent.kolibreeConnector()
        whenever(connector.login(any<LoginData>())).thenReturn(Completable.complete())
        whenever(connector.login(any<String>())).thenReturn(Completable.complete())
        whenever(connector.login(any<FacebookLoginData>())).thenReturn(Single.just(true))
        whenever(connector.validateMagicLinkCode(any())).thenReturn(Single.just("validCode"))
        whenever(connector.requestMagicLink(any())).thenReturn(Completable.complete())
        whenever(connector.syncAndNotify()).thenReturn(Single.just(false))
        whenever(connector.syncAndNotify()).thenReturn(Single.just(false))
        whenever(connector.createAnonymousAccount(any()))
            .thenReturn(Completable.complete())
    }

    private fun setupFacadeAndKolibreeService() {
        val facade = BaseKolibreeApplication.appComponent.kolibreeFacade()

        whenever(facade.connect()).thenReturn(Observable.just(true))
        whenever(facade.connector()).thenReturn(BaseKolibreeApplication.appComponent.kolibreeConnector())

        whenever(facade.connector().isDataCollectingAllowed).thenReturn(true)

        whenever(facade.connector().isAmazonDrsEnabled).thenReturn(amazonDrsEnabled)

        val kolibreeService = component().kolibreeService()
        whenever(kolibreeService.applicationContext).thenReturn(component().context())

        whenever(facade.service()).thenReturn(Single.just(kolibreeService))

        whenever(component().serviceProvider().connectOnce())
            .thenReturn(Single.just(kolibreeService))

        whenever(component().serviceProvider().connectStream())
            .thenReturn(BehaviorRelay.createDefault(ServiceConnected(kolibreeService)))

        connections?.apply {
            whenever(kolibreeService.knownConnections).thenReturn(toList())
            whenever(component().connectionPool().getKnownConnections())
                .thenReturn(toList())

            whenever(kolibreeService.getConnection(com.nhaarman.mockitokotlin2.any()))
                .thenAnswer { invocation ->
                    val macArgument: String =
                        invocation.getArgument(0) ?: throw NullPointerException(
                            "Attempted to get connection for null mac. This exception is thrown from SdkBuilder"
                        )

                    find { it.toothbrush().mac == macArgument }
                }

            for (connection in this) {
                val mac = connection.toothbrush().mac

                if (!connection.userMode().isSharedModeEnabled().blockingGet()) {
                    whenever(
                        facade
                            .connector()
                            .doesCurrentAccountKnow(
                                eq(
                                    connection.userMode().profileId().blockingGet()
                                )
                            )
                    )
                        .thenReturn(true)
                }

                whenever(component().connectionProvider().existingActiveConnection(mac))
                    .thenReturn(Single.just(connection))

                whenever(component().connectionProvider().getKLTBConnectionSingle(mac))
                    .thenReturn(Single.just(connection))
            }
        }

        whenever(kolibreeService.knownConnectionsOnceAndStream)
            .thenReturn(Flowable.just(connections?.toList() ?: listOf()))

        if (forgetConnectionSupport) {
            whenever(kolibreeService.forgetCompletable(any()))
                .thenReturn(Completable.complete())
        }
    }

    private fun supportUpdateProfile() {
        whenever(component().profileManager().updateProfile(any(), any()))
            .thenAnswer { invocation -> Single.just<Profile>(invocation.getArgument(1) as Profile) }
    }

    private fun setupRedeemNetworkService() {
        RedeemMocker.mockClaimRedeem(component().redeemNetworkService(), redeemBuilder)
    }

    private fun setupTransferNetworkService() {
        TransferMocker.mockTransferSmiles(component().transferNetworkService(), transferBuilder)
    }

    private fun setupUnityPlayerAttach() {
        whenever(component().attachUnityPlayerWrapper().isAttach).thenReturn(unityPlayerAttach)
    }

    private fun setupOrphanBrushings() {
        val orphanBrushingDao = component().orphanBrushingRepository()

        orphanBrushings?.forEach {
            orphanBrushingDao.insert(it)
        }
    }

    private fun component(): EspressoAppComponent {
        return BaseKolibreeApplication.appComponent as EspressoAppComponent
    }

    companion object {

        const val DEFAULT_ACCOUNT_ID = DEFAULT_TEST_ACCOUNT_ID

        val DEFAULT_GRUWARE_DATA = GruwareData.create(
            AvailableUpdate.create(DEFAULT_FW_VERSION.toString(), "", TYPE_FIRMWARE, 0L),
            AvailableUpdate.create(DEFAULT_GRU_VERSION.toString(), "", TYPE_GRU, 0L),
            AvailableUpdate.empty(TYPE_BOOTLOADER),
            AvailableUpdate.empty(TYPE_DSP)
        )

        @JvmStatic
        fun create(): SdkBuilder {
            return SdkBuilder()
                .withUserLoggedIn(true)
        }
    }
}
