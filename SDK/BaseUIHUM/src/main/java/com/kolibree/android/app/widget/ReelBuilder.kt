/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.app.widget

import com.kolibree.android.failearly.FailEarly
import kotlin.math.pow

internal fun getDigits(value: Int, minDigits: Int = 0): IntArray {
    val valueString = value.toString()
    val digits: MutableList<Int> = mutableListOf()
    digits.addAll(
        valueString.map { it.toString().toInt() }
    )
    while (digits.size < minDigits) {
        digits.add(0, 0)
    }
    return digits.toIntArray()
}

internal fun buildReels(startDigits: IntArray, endDigits: IntArray): Array<Array<String>> {
    if (startDigits.size != endDigits.size) {
        FailEarly.fail("startDigits and endDigits must be the same size.")
    }
    if (startDigits.value() > endDigits.value()) {
        FailEarly.fail("startDigits must be smaller than endDigits")
    }
    return startDigits.mapIndexed { index, startDigit ->
        var endDigit = endDigits[index]
        if (endDigit < startDigit) endDigit += TEN
        (startDigit..endDigit).map {
            (if (it < TEN) it else it - TEN).toString()
        }.toTypedArray()
    }.toTypedArray()
}

private fun IntArray.value(): Int {
    var sum = 0
    forEachIndexed { index, i ->
        val power = (size - 1) - index
        sum += (i * TEN_FLOAT.pow(power)).toInt()
    }
    return sum
}

private const val TEN = 10
private const val TEN_FLOAT = 10f
