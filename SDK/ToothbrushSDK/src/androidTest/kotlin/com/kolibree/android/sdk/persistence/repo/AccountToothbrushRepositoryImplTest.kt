package com.kolibree.android.sdk.persistence.repo

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.kolibree.android.commons.ToothbrushModel
import com.kolibree.android.sdk.connection.toothbrush.Toothbrush
import com.kolibree.android.sdk.persistence.model.AccountToothbrush
import com.kolibree.android.sdk.persistence.room.AccountToothbrushDao
import com.kolibree.android.sdk.persistence.room.ToothbrushSDKRoomAppDatabase
import com.kolibree.android.sdk.version.DspVersion
import com.kolibree.android.sdk.version.HardwareVersion
import com.kolibree.android.sdk.version.SoftwareVersion
import com.kolibree.android.test.rules.UnitTestImmediateRxSchedulersOverrideRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList
import junit.framework.TestCase.assertTrue
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AccountToothbrushRepositoryImplTest {

    // to make sure that Room executes all the database operations instantly.
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val schedulersRule =
        UnitTestImmediateRxSchedulersOverrideRule()

    private lateinit var accountToothbrushDao: AccountToothbrushDao
    private lateinit var toothbrushSDKRoomAppDatabase: ToothbrushSDKRoomAppDatabase
    private val toothbrushes = ArrayList<AccountToothbrush>()
    private lateinit var accountToothbrushRepository: AccountToothbrushRepository

    @Before
    fun setup() {
        toothbrushSDKRoomAppDatabase = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().getTargetContext(),
            ToothbrushSDKRoomAppDatabase::class.java
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
        accountToothbrushDao = toothbrushSDKRoomAppDatabase.accountToothbrushDao()
        accountToothbrushRepository =
            AccountToothbrushRepositoryImpl(accountToothbrushDao, Schedulers.io())
        addDataInDB()
    }

    @After
    fun tearDown() {
        clearDB()
        toothbrushes.clear()
    }

    @Test
    fun verifyDataIsEmptyAfterCleanedDB() {
        clearDB()
        accountToothbrushRepository.getAccountToothbrushes(accountId1)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValues(emptyList())
    }

    @Test
    fun verifyDataIsEmptyAfterDeleteAll() {
        accountToothbrushRepository.getAccountToothbrushes(accountId1)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValues(toothbrushes.filter { it.accountId == accountId1 })

        accountToothbrushRepository.deleteAll().concatWith {
            accountToothbrushRepository.getAccountToothbrushes(accountId1)
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValue { it.isEmpty() }
        }
    }

    @Test
    fun verifyAraModelStored() {
        accountToothbrushRepository.getAraCount(accountId1)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValues(toothbrushes.filter { it.accountId == accountId1 && it.model.isAra }.size)

        accountToothbrushRepository.getAraCount(accountId2)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValues(toothbrushes.filter { it.accountId == accountId2 && it.model.isAra }.size)
    }

    @Test
    fun verifyCanRemoveAmodel() {
        accountToothbrushRepository.getAraCount(accountId1)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValues(toothbrushes.filter { it.accountId == accountId1 && it.model.isAra }.size)
    }

    @Test
    fun verifyCanRemoveByMacAddr() {
        accountToothbrushRepository.remove(mac2).concatWith {
            accountToothbrushRepository.getAccountToothbrushes(accountId1)
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValue { it.none { it.mac == mac2 } }
        }
    }

    @Test
    fun verifyCanRename() {

        val newName = "my_new_name"
        accountToothbrushRepository.rename(mac1, newName).concatWith {
            accountToothbrushRepository.getAccountToothbrushes(accountId1)
                .test()
                .assertNoErrors()
                .assertComplete()
                .assertValue { it.any { it.name == newName } }
        }
    }

    @Test
    fun verifyProfileIsAssociatedToToothbrush() {

        accountToothbrushRepository.isAssociated(accountId1, mac1)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValues(true)

        accountToothbrushRepository.isAssociated(accountId2, mac1)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValues(false)
    }

    @Test
    fun verifyCanAddToothbrushingToAccount() {
        val newToothbrush =
            createAccountToothbrush(ToothbrushModel.ARA, "newMac", accountId1, "test")

        accountToothbrushRepository.addToothbrushes(listOf(newToothbrush))
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValues(true)

        accountToothbrushRepository.getAccountToothbrushes(accountId1)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValues(toothbrushes.filter { it.accountId == accountId1 } + newToothbrush)
    }

    @Test
    fun verifyCanAssociateToAccount() {

        val name = "name"
        val mac = "mac"
        val serial = "serial"
        val profileId = 42L
        val model = ToothbrushModel.ARA
        val hardwareVersion = HardwareVersion(12, 13)
        val firmwareVersion = SoftwareVersion(14, 15, 16)
        val bootloaderVersion = SoftwareVersion(2, 4, 3)

        val toothbrush = mock<Toothbrush> {
            whenever(it.model).thenReturn(model)
            whenever(it.mac).thenReturn(mac)
            whenever(it.getName()).thenReturn(name)
            whenever(it.serialNumber).thenReturn(serial)
            whenever(it.firmwareVersion).thenReturn(firmwareVersion)
            whenever(it.hardwareVersion).thenReturn(hardwareVersion)
            whenever(it.bootloaderVersion).thenReturn(bootloaderVersion)
            whenever(it.dspVersion).thenReturn(DspVersion.NULL)
        }

        val expected = AccountToothbrush(
            name = name,
            mac = mac,
            serial = serial,
            model = model,
            accountId = accountId2,
            hardwareVersion = hardwareVersion,
            firmwareVersion = firmwareVersion,
            bootloaderVersion = bootloaderVersion,
            profileId = profileId
        )

        accountToothbrushRepository.associate(toothbrush, accountId2, profileId).test()
            .assertValue(expected)

        accountToothbrushRepository.getAccountToothbrushes(accountId2)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it.contains(expected) }
    }

    @Test
    fun verifyCanAssociateToAccountToothbrush_insertNew() {
        val name = "name"
        val mac = "mac"
        val serial = "serial"
        val profileId = 42L
        val model = ToothbrushModel.ARA
        val hardwareVersion = HardwareVersion(12, 13)
        val firmwareVersion = SoftwareVersion(14, 15, 16)
        val bootloaderVersion = SoftwareVersion(2, 4, 3)

        val accountToothbrush = AccountToothbrush(
            mac,
            name,
            model,
            -1L,
            0L,
            serial,
            hardwareVersion,
            firmwareVersion,
            bootloaderVersion
        )

        accountToothbrushDao.deleteAll()
        assertTrue(accountToothbrushDao.listAll().isEmpty())

        accountToothbrushRepository.associate(accountToothbrush, accountId2, profileId)
            .blockingAwait()

        val all = accountToothbrushRepository.listAll()
        Assert.assertTrue(all.isNotEmpty())
        Assert.assertEquals(1, all.size)

        val expected = AccountToothbrush(
            name = name,
            mac = mac,
            serial = serial,
            model = model,
            accountId = accountId2,
            profileId = profileId,
            hardwareVersion = hardwareVersion,
            firmwareVersion = firmwareVersion,
            bootloaderVersion = bootloaderVersion
        )

        accountToothbrushRepository.getAccountToothbrushes(accountId2)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it.contains(expected) }
    }

    @Test
    fun verifyCanAssociateToAccountToothbrush_updateExistingOne() {
        val name = "name123"
        val mac = "mac123"
        val serial = "serial123"
        val profileId = 50L
        val model = ToothbrushModel.ARA
        val hardwareVersion = HardwareVersion(123, 133)
        val firmwareVersion = SoftwareVersion(143, 153, 163)

        val accountToothbrush = AccountToothbrush(
            mac,
            name,
            model,
            -1L,
            -1L,
            serial,
            hardwareVersion,
            firmwareVersion
        )

        accountToothbrushDao.deleteAll()
        Assert.assertTrue(accountToothbrushRepository.listAll().isEmpty())

        accountToothbrushDao.insert(accountToothbrush)
        Assert.assertEquals(1, accountToothbrushRepository.listAll().size)

        accountToothbrushRepository.associate(accountToothbrush, accountId2, profileId)
            .blockingAwait()

        Assert.assertEquals(1, accountToothbrushRepository.listAll().size)

        val expected = AccountToothbrush(
            name = name,
            mac = mac,
            serial = serial,
            model = model,
            accountId = accountId2,
            profileId = profileId,
            hardwareVersion = hardwareVersion,
            firmwareVersion = firmwareVersion
        )

        accountToothbrushRepository.getAccountToothbrushes(accountId2)
            .test()
            .assertNoErrors()
            .assertComplete()
            .assertValue { it.contains(expected) }
    }

    /*
    UTILS
     */

    private fun addDataInDB() {
        val toothbrush1 = createAccountToothbrush(model1, mac1, accountId1, name1)
        val toothbrush2 = createAccountToothbrush(model2, mac2, accountId2, name2)
        val toothbrush3 = createAccountToothbrush(model3, mac3, accountId1, name3)
        val toothbrush4 = createAccountToothbrush(model3, mac4, accountId1, name2)

        accountToothbrushDao.insert(toothbrush1)
        accountToothbrushDao.insert(toothbrush2)
        accountToothbrushDao.insert(toothbrush3)
        accountToothbrushDao.insert(toothbrush4)

        toothbrushes.add(toothbrush1)
        toothbrushes.add(toothbrush2)
        toothbrushes.add(toothbrush3)
        toothbrushes.add(toothbrush4)
    }

    /**
     * Clean the content of the DB
     */
    private fun clearDB() {
        toothbrushSDKRoomAppDatabase.clearAllTables()
    }

    private fun createAccountToothbrush(
        model: ToothbrushModel,
        mac: String,
        accountId: Long,
        name: String
    ) =
        AccountToothbrush(
            model = model,
            mac = mac,
            name = name,
            accountId = accountId
        )

    companion object {
        val model1 = ToothbrushModel.CONNECT_E1
        val model2 = ToothbrushModel.ARA
        val model3 = ToothbrushModel.CONNECT_M1
        const val mac1 = "mac1"
        const val mac2 = "mac2"
        const val mac3 = "mac3"
        const val mac4 = "mac4"
        const val name1 = "name1"
        const val name2 = "name2"
        const val name3 = "name3"
        const val accountId1 = 1L
        const val accountId2 = 2L
    }
}
