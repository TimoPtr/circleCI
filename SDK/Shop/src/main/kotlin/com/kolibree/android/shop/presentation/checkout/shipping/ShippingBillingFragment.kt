/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.presentation.checkout.shipping

import android.annotation.TargetApi
import android.os.Build
import android.os.Bundle
import android.view.View
import com.google.android.material.textfield.TextInputEditText
import com.kolibree.android.app.base.BaseMVIFragment
import com.kolibree.android.app.ui.input.hideSoftInput
import com.kolibree.android.shop.R
import com.kolibree.android.shop.databinding.FragmentShippingBillingBinding
import com.kolibree.android.shop.domain.model.Address.Input
import com.kolibree.android.shop.domain.model.Address.Input.ADDRESS_LINE_1
import com.kolibree.android.shop.domain.model.Address.Input.CITY
import com.kolibree.android.shop.domain.model.Address.Input.COUNTRY
import com.kolibree.android.shop.domain.model.Address.Input.FIRST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.LAST_NAME
import com.kolibree.android.shop.domain.model.Address.Input.POSTAL_CODE
import com.kolibree.android.shop.domain.model.Address.Input.PROVINCE
import com.kolibree.android.tracker.AnalyticsEvent
import com.kolibree.android.tracker.TrackableScreen

internal class ShippingBillingFragment :
    BaseMVIFragment<
        ShippingBillingViewState,
        ShippingBillingAction,
        ShippingBillingViewModel.Factory,
        ShippingBillingViewModel,
        FragmentShippingBillingBinding>(), TrackableScreen {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAutoFill()
    }

    override fun onPause() {
        super.onPause()
        binding.root.hideSoftInput()
    }

    override fun getViewModelClass() = ShippingBillingViewModel::class.java

    override fun getLayoutId(): Int = R.layout.fragment_shipping_billing

    override fun execute(action: ShippingBillingAction) = when (action) {
        is ShippingBillingAction.ScrollToError -> scrollToError(action.error)
    }

    private fun scrollToError(error: Pair<AddressType, Input>) {
        val errorView = when (error.first) {
            AddressType.SHIPPING -> getShippingErrorView(error.second)
            AddressType.BILLING -> getBillingErrorView(error.second)
        }

        binding.scrollView.smoothScrollTo(
            0,
            errorView.top - resources.getDimensionPixelOffset(R.dimen.dot_double)
        )
    }

    private fun getShippingErrorView(input: Input) = when (input) {
        FIRST_NAME -> binding.shippingFirstName
        LAST_NAME -> binding.shippingLastName
        ADDRESS_LINE_1 -> binding.shippingAddress
        CITY -> binding.shippingCity
        POSTAL_CODE -> binding.shippingPostalCode
        COUNTRY -> binding.shippingCountry
        PROVINCE -> binding.shippingProvince
    }

    private fun getBillingErrorView(input: Input) = when (input) {
        FIRST_NAME -> binding.billingFirstName
        LAST_NAME -> binding.billingLastName
        ADDRESS_LINE_1 -> binding.billingAddress
        CITY -> binding.billingCity
        POSTAL_CODE -> binding.billingPostalCode
        COUNTRY -> binding.billingCountry
        PROVINCE -> binding.billingProvince
    }

    private fun setupAutoFill() {
        // Because we have 2 addresses in the same layout, we need to make them independent
        // when it comes to autofill. Otherwise they will fight with each other.
        enableAutoFillOnFocus(shippingAutoFillInputs(), billingAutoFillInputs())
        enableAutoFillOnFocus(billingAutoFillInputs(), shippingAutoFillInputs())
    }

    @TargetApi(Build.VERSION_CODES.O)
    private fun enableAutoFillOnFocus(
        active: Array<TextInputEditText>,
        inactive: Array<TextInputEditText>
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        active.forEach { view ->
            view.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (hasFocus && !view.isImportantForAutofill) {
                    active.forEach { it.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_YES }
                    inactive.forEach { it.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO }
                }
            }
        }
    }

    private fun shippingAutoFillInputs() = with(binding) {
        arrayOf(
            shippingFirstNameInput,
            shippingLastNameInput,
            shippingAddressInput,
            shippingCityInput,
            shippingPostalCodeInput,
            shippingProvinceInput
            // TODO remove this after non-US checkout is integrated
            // shippingCountryInput
        )
    }

    private fun billingAutoFillInputs() = with(binding) {
        arrayOf(
            billingFirstNameInput,
            billingLastNameInput,
            billingAddressInput,
            billingCityInput,
            billingPostalCodeInput,
            billingProvinceInput,
            billingCountryInput
        )
    }

    override fun getScreenName(): AnalyticsEvent = ShippingBillingAnalytics.main()
}
