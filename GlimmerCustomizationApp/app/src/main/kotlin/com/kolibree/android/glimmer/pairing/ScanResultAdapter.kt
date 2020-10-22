/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.glimmer.pairing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kolibree.android.glimmer.R
import com.kolibree.android.glimmer.pairing.ScanResultAdapter.ScanResultHolder
import com.kolibree.android.sdk.scan.ToothbrushScanResult

internal class ScanResultAdapter(
    private val onScanResultClickCallback: (scanResult: ToothbrushScanResult) -> Unit
) : RecyclerView.Adapter<ScanResultHolder>() {

    private val scanResults: MutableList<ToothbrushScanResult> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ScanResultHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_scan_result, parent, false)
        )

    override fun getItemCount() = scanResults.size

    override fun onBindViewHolder(holder: ScanResultHolder, position: Int) {
        val scanResult = scanResults[position]
        holder.name.text = scanResult.name
        holder.model.text = scanResult.model.commercialName
        holder.mac.text = scanResult.mac
        holder.item.setOnClickListener { onScanResultClickCallback(scanResult) }
    }

    fun onScanResultList(results: List<ToothbrushScanResult>) = scanResults.apply {
        clear()
        addAll(results)
        notifyDataSetChanged()
    }

    internal class ScanResultHolder(root: View) : RecyclerView.ViewHolder(root) {
        val item: View = root
        val name: TextView = root.findViewById(R.id.scan_result_name)
        val model: TextView = root.findViewById(R.id.scan_result_model)
        val mac: TextView = root.findViewById(R.id.scan_result_mac)
    }
}
