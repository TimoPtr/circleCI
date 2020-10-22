/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.amazondash.domain

import android.content.SharedPreferences
import com.kolibree.android.amazondash.domain.AmazonDashVerifyStateUseCaseImpl.Companion.PREFS_STATE_KEY
import com.kolibree.android.app.test.BaseUnitTest
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AmazonDashVerifyStateUseCaseImplTest : BaseUnitTest() {

    private lateinit var useCase: AmazonDashVerifyStateUseCase

    private var state: String? = null

    override fun setup() {
        super.setup()
        useCase = AmazonDashVerifyStateUseCaseImpl(mockSharedPrefs())
        state = null
    }

    @Test
    fun `generates new state each time`() {
        val stateA = useCase.createNewState().blockingGet()
        val stateB = useCase.createNewState().blockingGet()

        assertTrue(stateA.isNotBlank())
        assertTrue(stateB.isNotBlank())
        assertNotEquals(stateA, stateB)
    }

    @Test
    fun `state does not contain not allowed chars`() {
        repeat(10) {
            val state = useCase.createNewState().blockingGet()
            assertFalse(NOT_ALLOWED.any { state.contains(it) })
        }
    }

    @Test
    fun `returns true if state is valid`() {
        val testState = "123"
        state = testState

        val testObserver = useCase.verifyAndClear(testState).test()

        testObserver.assertValue(true)
    }

    @Test
    fun `returns false if state is invalid`() {
        val testState = "123"
        state = "456"

        val testObserver = useCase.verifyAndClear(testState).test()

        testObserver.assertValue(false)
    }

    @Test
    fun `saves state after create`() {
        assertNull(state)
        val generatedState = useCase.createNewState().blockingGet()

        assertEquals(state, generatedState)
    }

    @Test
    fun `removes state after verification`() {
        state = "123"

        useCase.verifyAndClear(state).blockingGet()

        assertNull(state)
    }

    private fun mockSharedPrefs(): SharedPreferences {
        val prefs = mock<SharedPreferences>()
        val editor = mock<SharedPreferences.Editor>()

        whenever(prefs.edit()).thenReturn(editor)
        whenever(prefs.getString(PREFS_STATE_KEY, null)).then { state }
        whenever(editor.putString(eq(PREFS_STATE_KEY), any())).then { invocation ->
            state = invocation.getArgument(1) as String
            editor
        }
        whenever(editor.remove(eq(PREFS_STATE_KEY))).then {
            state = null
            editor
        }

        return prefs
    }
}

private val NOT_ALLOWED = listOf('&', '=', '‘', '/', '\\', '<', '>', '“')
