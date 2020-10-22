/*
 * Copyright (c) 2019 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.lint.extension

import com.intellij.psi.PsiElement

@Suppress("ReturnCount")
internal fun isInternal(element: PsiElement, name: String?): Boolean {
    if (name == null) {
        return false
    }
    with(element) {
        if (originalElement == null || originalElement.text.isNullOrBlank()) {
            return false
        }

        val nameOccurences = findNameOccurrences(this, name)
        if (nameOccurences.isEmpty()) {
            return false
        }

        return nameOccurences.any { occurrence -> findInternalKeyword(this, occurrence) }
    }
}

private fun findNameOccurrences(element: PsiElement, name: String): List<Int> {
    val occurrences = mutableListOf<Int>()
    var lastOccurrence: Int? = null
    with(element) {
        while (lastOccurrence != -1) {
            val indexOfName = originalElement.text.indexOf(
                name,
                startIndex = lastOccurrence?.let { it + 1 } ?: 0,
                ignoreCase = true
            )
            if (indexOfName != -1) {
                occurrences += indexOfName
            }
            lastOccurrence = indexOfName
        }
    }
    return occurrences
}

private fun findInternalKeyword(element: PsiElement, startIndex: Int): Boolean {
    with(element) {
        var preceedings = originalElement.text.substring(0, startIndex)
        val indexOfLastNewline = preceedings.lastIndexOf('\n')
        if (indexOfLastNewline != -1) {
            preceedings = preceedings.substring(indexOfLastNewline + 1, preceedings.length)
        }
        return preceedings.contains("internal ")
    }
}
