package com.kolibree.sdkws.appdata

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.clock.TrustedClock
import com.kolibree.sdkws.appdata.persistence.AppDataDao
import com.kolibree.sdkws.core.InternalKolibreeConnector
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject
import junit.framework.TestCase.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyBoolean
import org.mockito.ArgumentMatchers.anyLong
import org.threeten.bp.ZonedDateTime
import retrofit2.Response

/**
 * [AppDataManagerImpl] test units
 */
class AppDataManagerImplTest : BaseUnitTest() {

    private val appDataApi: AppDataApi = mock()

    private val appDataDao: AppDataDao = mock()

    private val kolibreeConnector: InternalKolibreeConnector = mock()

    private lateinit var appDataManager: AppDataManagerImpl

    @Before
    fun before() {
        whenever(kolibreeConnector.accountId).thenReturn(1L)
    }

    @Test
    fun haveConflict_onlyServerDataReturnsFalse() {
        init()

        val serverData = createAppData()
        assertFalse(appDataManager.haveConflict(serverData, AppDataImpl.NULL, AppDataImpl.NULL))
    }

    @Test
    fun haveConflict_lastSavedSameAsLastSyncReturnsFalse() {
        init()

        val localDateTime = TrustedClock.getNowZonedDateTime()
        val serverDateTime = localDateTime.plusHours(3)
        val serverData = createAppData(serverDateTime)
        val localData = createAppData(localDateTime)
        assertFalse(appDataManager.haveConflict(serverData, localData, localData))
    }

    @Test
    fun haveConflict_lastSavedMoreRecentThanLastSyncReturnsFalse() {
        init()

        val lastSyncDateTime = TrustedClock.getNowZonedDateTime()
        val lastSavedDateTime = lastSyncDateTime.plusHours(3)
        val serverData = createAppData(lastSyncDateTime)
        val lastSynchronized = createAppData(lastSyncDateTime)
        val lastSaved = createAppData(lastSavedDateTime)
        assertFalse(appDataManager.haveConflict(serverData, lastSynchronized, lastSaved))
    }

    @Test
    fun haveConflict_lastSavedAndServerMoreRecentThanLastSyncReturnsTrue() {
        init()

        val lastSyncDateTime = TrustedClock.getNowZonedDateTime()
        val serverDateTime = lastSyncDateTime.plusHours(1)
        val lastSavedDateTime = lastSyncDateTime.plusHours(3)
        val serverData = createAppData(serverDateTime)
        val lastSynchronized = createAppData(lastSyncDateTime)
        val lastSaved = createAppData(lastSavedDateTime)
        assertTrue(appDataManager.haveConflict(serverData, lastSynchronized, lastSaved))
    }

    @Test
    fun isNullAppData_returnsTrueWhenNull() {
        init()

        assertTrue(appDataManager.isNullAppData(AppDataImpl.NULL))
    }

    @Test
    fun isNullAppData_returnsFalseWhenNotNull() {
        init()

        assertFalse(appDataManager.isNullAppData(createAppData()))
    }

    /*
    synchronize
     */

    @Test
    fun `synchronize does nothing if isAppDataSyncEnabled is false`() {
        init(isAppDataEnabled = false)

        appDataManager.synchronize(1L).test().assertComplete()

        verify(appDataApi, never()).getAppData(any(), any())
        verify(appDataDao, never()).insert(any())
        verify(appDataApi, never()).saveAppData(anyLong(), anyLong(), any())
    }

    @Test
    fun synchronizeWithNoDataCompletesWithoutTouchingAnything() {
        init()

        whenever(appDataApi.getAppData(anyLong(), anyLong())).thenReturn(null)
        whenever(appDataDao.getAppData(anyLong(), anyBoolean())).thenReturn(Maybe.empty())
        appDataManager.synchronize(1L).test().await().assertComplete()
        verify(appDataDao, never()).insert(any())
        verify(appDataApi, never()).saveAppData(anyLong(), anyLong(), any())
    }

    @Test
    fun synchronizeWithOnlyLocalDataPostsData() {
        init()

        val local: AppDataImpl = mock()
        whenever(local.isSynchronized).thenReturn(false)
        whenever(local.getDateTime()).thenReturn(TrustedClock.getNowZonedDateTime())
        whenever(local.getProfileId()).thenReturn(1L)

        val response: Response<Void> = mock()
        whenever(appDataApi.saveAppData(anyLong(), anyLong(), any()))
            .thenReturn(Single.just(response))
        whenever(appDataApi.getAppData(anyLong(), anyLong())).thenReturn(null)
        whenever(appDataDao.getAppData(anyLong(), eq(true))).thenReturn(Maybe.empty())
        whenever(appDataDao.getAppData(anyLong(), eq(false))).thenReturn(Maybe.just(local))

        val testObserver = appDataManager.synchronize(1L).test()
        testObserver.await()
        verify(appDataApi).saveAppData(anyLong(), anyLong(), any())
        verify(appDataDao).insert(any())
        testObserver.assertComplete()
    }

    @Test
    fun synchronizeWithNoLocalDataStoresRemoteData() {
        init()

        val remote: AppDataImpl =
            AppDataImpl.create(1L, 1, TrustedClock.getNowZonedDateTime(), "{}") as AppDataImpl
        remote.isSynchronized = false

        whenever(appDataApi.getAppData(anyLong(), anyLong())).thenReturn(Single.just(remote))
        whenever(appDataDao.getAppData(anyLong(), anyBoolean())).thenReturn(Maybe.empty())

        val testObserver = appDataManager.synchronize(1L).test()
        testObserver.await()
        verify(appDataApi, never()).saveAppData(anyLong(), anyLong(), any())
        verify(appDataDao).insert(any())
        testObserver.assertComplete()
    }

    @Test
    fun synchronizeWithConflictAndNoConflictSolverThrowsException() {
        init()

        val lastSyncDateTime = TrustedClock.getNowZonedDateTime()
        val serverData = createAppData(lastSyncDateTime.plusHours(5))
        val lastSyncData = createAppData(lastSyncDateTime)
        val lastSavedData = createAppData(lastSyncDateTime.plusHours(3))
        val conflictSolver: AppDataConflictSolver = mock()
        whenever(conflictSolver.onConflict(any(), any(), any())).thenReturn(serverData)
        whenever(appDataApi.getAppData(any(), any())).thenReturn(Single.just(serverData))

        whenever(appDataDao.getAppData(anyLong(), eq(false))).thenReturn(Maybe.just(lastSavedData))
        whenever(appDataDao.getAppData(anyLong(), eq(true))).thenReturn(Maybe.just(lastSyncData))

        val testObserver = appDataManager.synchronize(1L).test().await()
        testObserver.assertError(Exception::class.java)
    }

    @Test
    fun synchronizeWithConflictAndConflictSolverCalledOnConflict() {
        init()

        val lastSyncDateTime = TrustedClock.getNowZonedDateTime()
        val serverData = createAppData(lastSyncDateTime.plusHours(5))
        val lastSyncData = createAppData(lastSyncDateTime)
        val lastSavedData = createAppData(lastSyncDateTime.plusHours(3))
        val conflictSolver: AppDataConflictSolver = mock()
        whenever(conflictSolver.onConflict(any(), any(), any())).thenReturn(serverData)
        whenever(appDataApi.getAppData(any(), any())).thenReturn(Single.just(serverData))
        whenever(appDataApi.saveAppData(any(), any(), any()))
            .thenReturn(Single.just(Response.success<Void>(null)))
        whenever(appDataDao.getAppData(anyLong(), eq(false))).thenReturn(Maybe.just(lastSavedData))
        whenever(appDataDao.getAppData(anyLong(), eq(true))).thenReturn(Maybe.just(lastSyncData))

        appDataManager.setAppDataConflictSolver(conflictSolver)
        val testObserver = appDataManager.synchronize(1L).test()
        testObserver.await()
        testObserver.assertComplete()
        verify(conflictSolver).onConflict(any(), any(), any())
    }

    @Test
    fun downloadAppDataErrorReturnsNoData() {
        init()

        whenever(appDataApi.getAppData(anyLong(), anyLong())).thenThrow(RuntimeException("404"))
        appDataManager.downloadAppData(1L).test()
            .await()
            .assertValue { value ->
                value.getDateTime() == AppDataImpl.NULL.getDateTime() && value.getProfileId() == AppDataImpl.NULL.getProfileId()
            }
    }

    /*
    saveAppData
     */

    @Test
    fun `saveAppData updates database synchronously then goes async for synchronization`() {
        init()

        val appData = mock<AppDataImpl>()
        val saveAppDataSubject = SingleSubject.create<Response<Void>>()

        whenever(appDataApi.saveAppData(anyLong(), anyLong(), any()))
            .thenReturn(saveAppDataSubject)

        val testObserver = appDataManager.saveAppData(appData).test()

        verify(appDataDao).insert(appData) // Database is updated synchronously
        verify(appData, never()).isSynchronized = true
        testObserver
            .assertNoErrors()
            .assertNotComplete()

        saveAppDataSubject.onSuccess(Response.success(null))

        testObserver
            .assertNoErrors()
            .assertComplete()

        verify(appDataApi).saveAppData(any(), any(), any())
        verify(appData).isSynchronized = true
        verify(appDataDao, times(2)).insert(appData)
    }

    /*
    UTILS
     */

    private fun init(isAppDataEnabled: Boolean = true) {
        appDataManager = AppDataManagerImpl(appDataApi, appDataDao, kolibreeConnector, isAppDataEnabled)
    }

    private fun createAppData(date: ZonedDateTime = TrustedClock.getNowZonedDateTime()) =
        AppDataImpl.create(1L, 1, date, "{}") as AppDataImpl
}
