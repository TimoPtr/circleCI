/*
 * Copyright (c) 2018 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 * Proprietary and confidential
 */

package com.kolibree.android.commons.profile

import org.junit.Assert.assertEquals
import org.junit.Test

/** [Gender] tests  */
class GenderTest {

    /*
    findBySerializedName
     */

    @Test
    fun `findBySerializedName with GENDER_MALE_SERIALIZED_VALUE returns MALE`() {
        assertEquals(Gender.MALE, Gender.findBySerializedName(GENDER_MALE_SERIALIZED_VALUE))
    }

    @Test
    fun `findBySerializedName with GENDER_FEMALE_SERIALIZED_VALUE returns FEMALE`() {
        assertEquals(Gender.FEMALE, Gender.findBySerializedName(GENDER_FEMALE_SERIALIZED_VALUE))
    }

    @Test
    fun `findBySerializedName with value for prefer not to answer returns PREFER_NOT_TO_ANSWER`() {
        assertEquals(
            Gender.PREFER_NOT_TO_ANSWER,
            Gender.findBySerializedName(GENDER_PREFER_NOT_TO_ANSWER_SERIALIZED_VALUE)
        )
    }

    @Test
    fun `findBySerializedName with GENDER_UNKNOWN_SERIALIZED_VALUE returns UNKNOWN`() {
        assertEquals(Gender.UNKNOWN, Gender.findBySerializedName(GENDER_UNKNOWN_SERIALIZED_VALUE))
    }

    @Test
    fun `findBySerializedName with weird value returns UNKNOWN`() {
        assertEquals(Gender.UNKNOWN, Gender.findBySerializedName("I'm a weird value"))
    }

    @Test
    fun `findBySerializedName with null value returns UNKNOWN`() {
        assertEquals(Gender.UNKNOWN, Gender.findBySerializedName(null))
    }

    /*
    serializedName
     */

    @Test
    fun `serialized name of MALE is GENDER_MALE_SERIALIZED_VALUE`() {
        assertEquals(GENDER_MALE_SERIALIZED_VALUE, Gender.MALE.serializedName)
    }

    @Test
    fun `serialized name of FEMALE is GENDER_FEMALE_SERIALIZED_VALUE`() {
        assertEquals(GENDER_FEMALE_SERIALIZED_VALUE, Gender.FEMALE.serializedName)
    }

    @Test
    fun `serialized name of PREFER_NOT_TO_ANSWER is GENDER_PREFER_NOT_TO_ANSWER_SERIALIZED_VALUE`() {
        assertEquals(
            GENDER_PREFER_NOT_TO_ANSWER_SERIALIZED_VALUE,
            Gender.PREFER_NOT_TO_ANSWER.serializedName
        )
    }

    @Test
    fun `serialized name of UNKNOWN is GENDER_UNKNOWN_SERIALIZED_VALUE`() {
        assertEquals(GENDER_UNKNOWN_SERIALIZED_VALUE, Gender.UNKNOWN.serializedName)
    }

    /*
    Constants
     */

    @Test
    fun `value of GENDER_MALE_SERIALIZED_VALUE is M`() {
        assertEquals("M", GENDER_MALE_SERIALIZED_VALUE)
    }

    @Test
    fun `value of GENDER_FEMALE_SERIALIZED_VALUE is F`() {
        assertEquals("F", GENDER_FEMALE_SERIALIZED_VALUE)
    }

    @Test
    fun `value of GENDER_PREFER_NOT_TO_ANSWER_SERIALIZED_VALUE is NC`() {
        assertEquals("NC", GENDER_PREFER_NOT_TO_ANSWER_SERIALIZED_VALUE)
    }

    @Test
    fun `value of GENDER_UNKNOWN_SERIALIZED_VALUE is U`() {
        assertEquals("U", GENDER_UNKNOWN_SERIALIZED_VALUE)
    }
}
