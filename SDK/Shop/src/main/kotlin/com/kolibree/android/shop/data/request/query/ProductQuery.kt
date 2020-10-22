/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.request.query

import com.shopify.buy3.Storefront.ImageConnectionQuery
import com.shopify.buy3.Storefront.ProductConnectionQuery
import com.shopify.buy3.Storefront.ProductQuery
import com.shopify.buy3.Storefront.ProductVariantConnectionQuery

const val NUMBER_OF_IMAGES = 10

const val NUMBER_OF_VARIANTS = 10

internal fun queryForProduct(product: ProductConnectionQuery) =
    product.edges { productEdge ->
        productEdge.node { productNode ->
            productNode
                .title()
                .productType()
                .description()
                .descriptionHtml()
                .priceRange { priceRange ->
                    priceRange.maxVariantPrice { max ->
                        max.amount()
                            .currencyCode()
                    }
                }
                .firstImages(NUMBER_OF_IMAGES) { images ->
                    images.edges { imageEdge ->
                        imageEdge.node { imageNode ->
                            imageNode
                                .id()
                                .originalSrc()
                        }
                    }
                }
                .firstVariants(NUMBER_OF_VARIANTS) { variant ->
                    variant.edges { variantEdge ->
                        variantEdge.node { variantNode ->
                            variantNode.title()
                                .sku()
                                .price()
                                .image { imageNode ->
                                    imageNode
                                        .id()
                                        .originalSrc()
                                }
                                .availableForSale()
                        }
                    }
                }
        }
    }

private fun ProductQuery.firstImages(
    numberOfImages: Int,
    queryDefinition: (ImageConnectionQuery) -> Unit
): ProductQuery =
    images(
        { args -> args.first(numberOfImages) },
        { queryBuilder -> queryDefinition(queryBuilder) }
    )

private fun ProductQuery.firstVariants(
    numberOfVariants: Int,
    queryDefinition: (ProductVariantConnectionQuery) -> Unit
): ProductQuery =
    variants(
        { args -> args.first(numberOfVariants) },
        { queryBuilder -> queryDefinition(queryBuilder) }
    )
