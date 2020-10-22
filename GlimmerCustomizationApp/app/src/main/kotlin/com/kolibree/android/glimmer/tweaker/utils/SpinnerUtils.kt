/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.tweaker.utils

import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.annotation.StringRes

internal fun Spinner.setup(values: List<String>, listener: AdapterView.OnItemSelectedListener) {
    adapter = ArrayAdapter(
        context,
        android.R.layout.simple_spinner_dropdown_item,
        values
    )
    onItemSelectedListener = listener
}

internal fun Spinner.setupWithStringRes(
    @StringRes values: List<Int>,
    listener: AdapterView.OnItemSelectedListener
) {
    adapter = ArrayAdapter(
        context,
        android.R.layout.simple_spinner_dropdown_item,
        values.map { context.getString(it) }
    )
    onItemSelectedListener = listener
}
