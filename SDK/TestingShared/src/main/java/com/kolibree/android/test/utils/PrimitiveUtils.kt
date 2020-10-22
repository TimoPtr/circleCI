/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.test.utils

import androidx.annotation.Keep
import kotlin.random.Random

@Keep
fun randomSigned16(minValue: Short = Short.MIN_VALUE, maxValue: Short = Short.MAX_VALUE): Short {
    return Random.nextInt(minValue.toInt(), maxValue.toInt()).toShort()
}

@Keep
fun randomUnsignedSigned16(maxValue: Int = 65535): Int {
    require(maxValue <= 65535) { "max cannot be bigger than 65535 for Unsigned16" }
    return randomPositiveInt(maxValue = maxValue)
}

@Keep
fun randomUnsigned8(maxValue: Short = 256): Short {
    require(maxValue <= 256) { "max cannot be bigger than 256 for Unsigned8" }
    return randomPositiveInt(maxValue = maxValue.toInt()).toShort()
}

@Keep
fun randomPositiveInt(maxValue: Int = Integer.MAX_VALUE, minValue: Int = 0): Int {
    check(minValue >= 0)

    return Random.nextInt(minValue, maxValue)
}

@Keep
fun randomInt(maxValue: Int = Integer.MAX_VALUE): Int = Random.nextInt(maxValue)

@Keep
fun randomByte(): Byte = Random.nextBytes(1)[0]
