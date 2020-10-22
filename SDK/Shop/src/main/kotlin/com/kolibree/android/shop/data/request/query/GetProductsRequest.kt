/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query

import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.shop.data.configuration.ShopifyProductTag
import com.kolibree.android.shop.data.request.GraphClientRequest
import com.kolibree.android.shop.domain.model.Price
import com.kolibree.android.shop.domain.model.Product
import com.shopify.buy3.Storefront.Image
import com.shopify.buy3.Storefront.ImageConnection
import com.shopify.buy3.Storefront.ProductConnectionQuery
import com.shopify.buy3.Storefront.ProductVariant
import com.shopify.buy3.Storefront.QueryRoot
import com.shopify.buy3.Storefront.QueryRootQuery
import com.shopify.buy3.Storefront.ShopQuery
import java.util.Currency
import javax.inject.Inject

typealias StorefrontProduct = com.shopify.buy3.Storefront.Product

internal class GetProductsRequest @Inject constructor(
    productTag: ShopifyProductTag
) : GraphClientRequest<List<Product>>() {

    override val queryBuilder: (QueryRootQuery) -> QueryRootQuery = { root ->
        root.shop { shop ->
            shop.firstShopProductsWithTag(NUMBER_OF_PRODUCTS, productTag.value) { product ->
                queryForProduct(product)
            }
        }
    }

    override val responseBuilder: (QueryRoot) -> List<Product> = { response ->
        response.shop
            .products
            .edges
            .map { edge -> edge.node }
            .mapNotNull(::parseProducts)
            .flatten()
    }

    private fun parseProducts(productData: StorefrontProduct): List<Product>? = try {
        productData.variants.edges
            .map { edge -> edge.node }
            .filter { variant -> variant.availableForSale /* == true */ }
            .mapNotNull { variant -> parseProduct(productData, variant) }
    } catch (e: RuntimeException) {
        FailEarly.fail(exception = e, message = "Error while parsing products data")
        null
    }

    private fun parseProduct(
        productsData: StorefrontProduct,
        variantData: ProductVariant
    ): Product? = try {
        val currency = Currency.getInstance(
            productsData.priceRange
                .maxVariantPrice
                .currencyCode
                .name
        )
        Product(
            productId = productsData.id.toString(),
            variantId = variantData.id.toString(),
            productTitle = productsData.title,
            variantTitle = variantData.title,
            productType = productsData.productType,
            description = productsData.description,
            htmlDescription = productsData.descriptionHtml,
            price = Price.create(
                decimalAmount = variantData.price,
                currency = currency
            ),
            variantImage = parseImage(variantData.image),
            productImages = parseImages(productsData.images),
            sku = variantData.sku
        )
    } catch (e: RuntimeException) {
        FailEarly.fail(exception = e, message = "Error while parsing product & variant data")
        null
    }

    private fun parseImages(data: ImageConnection): List<String> =
        data.edges.map { parseImage(it.node) }

    private fun parseImage(image: Image): String = image.originalSrc
}

private fun ShopQuery.firstShopProductsWithTag(
    count: Int,
    tag: String,
    query: (ProductConnectionQuery) -> Unit
) = products(
    { args -> args.first(count).query(tag) },
    { queryBuilder -> query(queryBuilder) }
)

private const val NUMBER_OF_PRODUCTS = 100
