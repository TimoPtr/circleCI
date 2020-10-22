package com.kolibree.android.game.bi

import com.kolibree.android.app.test.BaseUnitTest
import org.junit.Assert.assertEquals
import org.junit.Test

/** [Contract] tests */
class ContractTest : BaseUnitTest() {

    /*
    ToothbrushModelName
     */

    @Test
    fun `value of ToothbrushModelName ARA is ara`() {
        assertEquals("ara", Contract.ToothbrushModelName.ARA)
    }

    @Test
    fun `value of ToothbrushModelName M1 is cm1`() {
        assertEquals("cm1", Contract.ToothbrushModelName.M1)
    }

    @Test
    fun `value of ToothbrushModelName E1 is ce1`() {
        assertEquals("ce1", Contract.ToothbrushModelName.E1)
    }

    @Test
    fun `value of ToothbrushModelName E2 is ce2`() {
        assertEquals("ce2", Contract.ToothbrushModelName.E2)
    }

    @Test
    fun `value of ToothbrushModelName B1 is cb1`() {
        assertEquals("cb1", Contract.ToothbrushModelName.B1)
    }

    @Test
    fun `value of ToothbrushModelName PQL is pql`() {
        assertEquals("pql", Contract.ToothbrushModelName.PQL)
    }

    /*
    Games
     */

    @Test
    fun `value of ActivityName COACH is C`() {
        assertEquals("C", Contract.ActivityName.COACH)
    }

    @Test
    fun `value of ActivityName COACH+ is C`() {
        assertEquals("C", Contract.ActivityName.COACH_PLUS)
    }

    @Test
    fun `value of ActivityName GO_PIRATE is P`() {
        assertEquals("P", Contract.ActivityName.GO_PIRATE)
    }

    @Test
    fun `value of ActivityName RABBIDS is R`() {
        assertEquals("R", Contract.ActivityName.RABBIDS)
    }

    @Test
    fun `value of ActivityName FREE_BRUSHING is F`() {
        assertEquals("F", Contract.ActivityName.FREE_BRUSHING)
    }

    /*
    BrushingMode
     */

    @Test
    fun `value of BrushingMode MANUAL is M`() {
        assertEquals("M", Contract.BrushingMode.MANUAL)
    }

    @Test
    fun `value of BrushingMode VIBRATING is V`() {
        assertEquals("V", Contract.BrushingMode.VIBRATING)
    }

    /*
    Handedness
     */

    @Test
    fun `value of Handedness RIGHT_HANDED is R`() {
        assertEquals("R", Contract.Handedness.RIGHT_HANDED)
    }

    @Test
    fun `value of Handedness LEFT_HANDED is L`() {
        assertEquals("L", Contract.Handedness.LEFT_HANDED)
    }

    @Test
    fun `value of Handedness UNKNOWN is N`() {
        assertEquals("N", Contract.Handedness.UNKNOWN)
    }
}
