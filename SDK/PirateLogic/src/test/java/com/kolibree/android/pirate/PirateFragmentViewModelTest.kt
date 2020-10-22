package com.kolibree.android.pirate

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.game.BrushingCreator
import com.kolibree.android.sdk.core.KLTBConnectionProvider
import com.kolibree.android.test.mocks.ProfileBuilder
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.kolibree.android.utils.KolibreeAppVersions
import com.kolibree.sdkws.core.IKolibreeConnector
import com.kolibree.sdkws.core.ProfileWrapper
import com.kolibree.sdkws.data.model.CreateBrushingData
import com.kolibree.sdkws.data.model.gopirate.UpdateGoPirateData
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.doNothing

/** Created by miguelaragues on 17/10/17.  */
class PirateFragmentViewModelTest : BaseUnitTest() {

    @Mock
    internal lateinit var connector: IKolibreeConnector

    @Mock
    internal lateinit var brushingCreator: BrushingCreator

    @Mock
    internal lateinit var connectionProvider: KLTBConnectionProvider

    @Mock
    internal lateinit var profileWrapper: ProfileWrapper

    private lateinit var viewModel: PirateFragmentViewModel

    private val profile = ProfileBuilder.create().build()

    override fun setup() {
        super.setup()

        FailEarly.overrideDelegateWith(NoopTestDelegate)

        whenever(connector.currentProfileFlowable()).thenReturn(Flowable.just(profile))
        whenever(connector.withProfileId(profile.id)).thenReturn(profileWrapper)
        whenever(connector.getProfileWithId(eq(profile.id))).thenReturn(profile)
        viewModel = spy(
            PirateFragmentViewModel(
                connector = connector,
                provider = connectionProvider,
                toothbrushMac = Single.just(DEFAULT_MAC),
                brushingCreator = brushingCreator,
                appVersions = KolibreeAppVersions("1.0", "2")
            )
        )
    }

    /*
  ON BRUSHING COMPLETED
   */
    @Test
    fun onBrushingCompleted_storesPirateData() {
        assertNull(viewModel.pirateData)

        doNothing().whenever(viewModel).onBrushingCompleted(any(), anyInt())

        val expectedPirateData = mock<UpdateGoPirateData>()
        viewModel.onBrushingCompleted(mock(), expectedPirateData)

        assertEquals(expectedPirateData, viewModel.pirateData)
    }

    @Test
    fun onBrushingCompleted_invokesParentOnBrushingCompletedWithZeroPoints() {
        val expectedBrushingData = mock<CreateBrushingData>()

        doNothing().whenever(viewModel).onBrushingCompleted(eq(expectedBrushingData), eq(0))

        viewModel.onBrushingCompleted(expectedBrushingData, mock<UpdateGoPirateData>())

        verify(viewModel).onBrushingCompleted(eq(expectedBrushingData), eq(0))
    }

    /*
  UPDATE PIRATE DATA
   */
    @Test
    fun updatePirateData_noPirateData_doesNotStorePirateData() {
        viewModel.updatePirateData(null)

        verify(profileWrapper, never()).updateGoPirateData(any())
    }

    @Test
    fun updatePirateData_invokesUpdateGoPirateData() {
        val expectedPirateData = mock<UpdateGoPirateData>()
        viewModel.updatePirateData(expectedPirateData)

        verify(profileWrapper).updateGoPirateData(expectedPirateData)
    }

    @Test
    fun updateGoPirateData_nullifiesPirateDataAfterSendingIt() {
        val expectedPirateData = mock<UpdateGoPirateData>()

        viewModel.updatePirateData(expectedPirateData)

        assertNull(viewModel.pirateData)
    }

    /*
    beforeSendDataSavedCompletable
     */
    @Test
    fun beforeSendDataSavedCompletable_noPirateData_doesNotStorePirateData() {
        assertNull(viewModel.pirateData)

        viewModel.beforeSendDataSavedCompletable().test().assertComplete()

        verify(profileWrapper, never()).updateGoPirateData(any())
    }

    @Test
    fun beforeSendDataSavedCompletable_invokesUpdateGoPirateData() {
        val expectedPirateData = mock<UpdateGoPirateData>()
        viewModel.pirateData = expectedPirateData

        viewModel.beforeSendDataSavedCompletable().test().assertComplete()

        verify(profileWrapper).updateGoPirateData(expectedPirateData)
    }

    @Test
    fun beforeSendDataSavedCompletable_nullifiesPirateDataAfterSendingIt() {
        viewModel.pirateData = mock()

        viewModel.beforeSendDataSavedCompletable().test().assertComplete()

        assertNull(viewModel.pirateData)
    }

    companion object {

        private const val DEFAULT_MAC = "AA:56"
    }
}
