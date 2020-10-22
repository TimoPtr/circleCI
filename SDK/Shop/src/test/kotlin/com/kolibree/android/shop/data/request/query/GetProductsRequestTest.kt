/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query

import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.failearly.FailEarly
import com.kolibree.android.shop.ShopifyQueryDSL
import com.kolibree.android.shop.ShopifyQueryDSL.product
import com.kolibree.android.shop.ShopifyQueryDSL.products
import com.kolibree.android.shop.ShopifyQueryDSL.shop
import com.kolibree.android.shop.buildProduct
import com.kolibree.android.shop.data.configuration.ShopifyProductTag
import com.kolibree.android.shop.defaultPrice
import com.kolibree.android.shop.defaultProductOrdinal
import com.kolibree.android.shop.defaultVariantOrdinal
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.generateProductDescription
import com.kolibree.android.shop.generateProductHtmlDescription
import com.kolibree.android.shop.generateProductId
import com.kolibree.android.shop.generateProductImage
import com.kolibree.android.shop.generateProductTitle
import com.kolibree.android.shop.generateProductType
import com.kolibree.android.shop.generateVariantId
import com.kolibree.android.shop.generateVariantImage
import com.kolibree.android.shop.generateVariantSku
import com.kolibree.android.shop.generateVariantTitle
import com.kolibree.android.test.utils.failearly.delegate.NoopTestDelegate
import com.shopify.buy3.GraphResponse
import com.shopify.buy3.Storefront
import junit.framework.TestCase.assertEquals
import org.junit.Test

class GetProductsRequestTest : BaseUnitTest() {

    @Test
    fun `buildQuery creates correct build query with provided tag`() {
        val tag = "test_product_tag"
        val query = getProductRequest(tag).buildQuery()

        assertEquals(
            "{shop{products(first:100,query:\"tag:$tag\"){edges{node{" +
                "id," +
                "title," +
                "productType," +
                "description," +
                "descriptionHtml," +
                "priceRange{maxVariantPrice{amount,currencyCode}}," +
                "images(first:10){edges{node{id,originalSrc}}}," +
                "variants(first:10){edges{node{id,title,sku,price,image{id,originalSrc},availableForSale}}}" +
                "}}}}}",
            query.toString()
        )
    }

    @Test
    fun `buildResponse parses the correct Storefront response correctly`() {
        val products = getProductRequest().buildResponse(response = mockProducts())
        assertEquals(listOf(buildProduct()), products)
    }

    @Test
    fun `buildResponse handles multiple product response`() {
        val expectedResponse = listOf(
            buildProduct(productOrdinal = 1),
            buildProduct(productOrdinal = 2)
        )
        val products = getProductRequest().buildResponse(
            response = mockProducts(productOrdinals = listOf(1, 2))
        )
        assertEquals(expectedResponse, products)
    }

    @Test
    fun `buildResponse handles multiple product & variant response`() {
        val p1 = 1
        val p2 = 2
        val v1 = 1001
        val v2 = 2001

        val expectedResponse = listOf(
            buildProduct(productOrdinal = p1, variantOrdinal = v1),
            buildProduct(productOrdinal = p1, variantOrdinal = v2),
            buildProduct(productOrdinal = p2, variantOrdinal = v1),
            buildProduct(productOrdinal = p2, variantOrdinal = v2)
        )
        val products = getProductRequest().buildResponse(
            response = mockProducts(
                productOrdinals = listOf(p1, p2),
                variantData = listOf(v1 to true, v2 to true)
            )
        )
        assertEquals(expectedResponse, products)
    }

    @Test
    fun `buildResponse filters out variants not available for sale`() {
        val products = getProductRequest().buildResponse(
            response = mockProducts(variantData = listOf(defaultVariantOrdinal to false))
        )
        assertEquals(emptyList<Product>(), products)
    }

    @Test
    fun `buildResponse filters out products with incomplete data`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val expectedResponse = listOf(buildProduct())

        val products = getProductRequest().buildResponse(
            shop {
                products = products {
                    listOf(
                        product("some_product_id") {
                            // missing data
                        },
                        mockProduct()
                    )
                }
            }
        )
        assertEquals(expectedResponse, products)
    }

    @Test
    fun `buildResponse filters out products with no variant data`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val expectedResponse = listOf(buildProduct())

        val productOrdinal = 1
        val products = getProductRequest().buildResponse(
            shop {
                products = products {
                    listOf(
                        product(generateProductId(productOrdinal)) {
                            title = generateProductTitle(productOrdinal)
                            productType = generateProductType(productOrdinal)
                            description = generateProductDescription(productOrdinal)
                            descriptionHtml = generateProductHtmlDescription(productOrdinal)
                            priceRange = ShopifyQueryDSL.priceRange {
                                amount = defaultPrice.decimalAmount.toPlainString()
                                currencyCode =
                                    ShopifyQueryDSL.currencyCode { defaultPrice.currency.currencyCode }
                            }
                            images = ShopifyQueryDSL.noImages()
                        },
                        mockProduct()
                    )
                }
            }
        )
        assertEquals(expectedResponse, products)
    }

    @Test
    fun `buildResponse filters out products with incomplete variant data`() {
        FailEarly.overrideDelegateWith(NoopTestDelegate)

        val productOrdinal = 1
        val variantOrdinal = 10000
        val expectedResponse = listOf(buildProduct())

        val products = getProductRequest().buildResponse(
            shop {
                products = products {
                    listOf(
                        mockProduct(),
                        product(generateProductId(productOrdinal)) {
                            title = generateProductTitle(productOrdinal)
                            productType = generateProductType(productOrdinal)
                            description = generateProductDescription(productOrdinal)
                            descriptionHtml = generateProductHtmlDescription(productOrdinal)
                            priceRange = ShopifyQueryDSL.priceRange {
                                amount = defaultPrice.decimalAmount.toPlainString()
                                currencyCode =
                                    ShopifyQueryDSL.currencyCode { defaultPrice.currency.currencyCode }
                            }
                            images = ShopifyQueryDSL.noImages()
                            variants = ShopifyQueryDSL.singleVariant {
                                ShopifyQueryDSL.variant(
                                    generateVariantId(productOrdinal, variantOrdinal)
                                ) {
                                    // missing data
                                }
                            }
                        }
                    )
                }
            }
        )
        assertEquals(expectedResponse, products)
    }

    private fun getProductRequest(tag: String = "some_tag") = GetProductsRequest(ShopifyProductTag(tag))

    private fun mockProducts(
        productOrdinals: List<Int> = listOf(defaultProductOrdinal),
        variantData: List<Pair<Int, Boolean>> = listOf(defaultVariantOrdinal to true)
    ): GraphResponse<Storefront.QueryRoot> =
        shop {
            products = products {
                productOrdinals.map { productOrdinal -> mockProduct(productOrdinal, variantData) }
            }
        }

    private fun mockProduct(
        productOrdinal: Int = defaultProductOrdinal,
        variantData: List<Pair<Int, Boolean>> = listOf(defaultVariantOrdinal to true)
    ): Storefront.Product {
        return product(generateProductId(productOrdinal)) {
            title = generateProductTitle(productOrdinal)
            productType = generateProductType(productOrdinal)
            description = generateProductDescription(productOrdinal)
            descriptionHtml = generateProductHtmlDescription(productOrdinal)
            priceRange = ShopifyQueryDSL.priceRange {
                amount = defaultPrice.decimalAmount.toPlainString()
                currencyCode =
                    ShopifyQueryDSL.currencyCode { defaultPrice.currency.currencyCode }
            }
            images =
                ShopifyQueryDSL.images { listOf(generateProductImage(productOrdinal)) }
            variants = ShopifyQueryDSL.variants {
                variantData.map { (variantOrdinal, available) ->
                    ShopifyQueryDSL.variant(
                        generateVariantId(productOrdinal, variantOrdinal)
                    ) {
                        title = generateVariantTitle(productOrdinal, variantOrdinal)
                        sku = generateVariantSku(productOrdinal, variantOrdinal)
                        price = defaultPrice.decimalAmount
                        image = ShopifyQueryDSL.image {
                            generateVariantImage(productOrdinal, variantOrdinal)
                        }
                        availableForSale = available
                    }
                }
            }
        }
    }
}
