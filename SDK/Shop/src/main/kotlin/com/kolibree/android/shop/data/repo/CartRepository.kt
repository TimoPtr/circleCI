/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.repo

import androidx.annotation.Keep
import androidx.annotation.VisibleForTesting
import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.persitence.CartDao
import com.kolibree.android.shop.data.persitence.model.CartEntryEntity
import com.kolibree.android.shop.data.persitence.model.CartEntryEntity.Companion.createEntity
import com.kolibree.android.shop.domain.model.Product
import com.kolibree.android.shop.domain.model.QuantityProduct
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

@Keep
interface CartRepository {
    /**
     * Get the product in the cart
     */
    fun getCartProducts(): Flowable<List<QuantityProduct>>

    /**
     * Remove exactly 1 occurrence of the given product from the cart
     * @return the current quantity of this product
     */
    fun removeProduct(product: Product): Single<Int>

    /**
     * Add exactly 1 occurrence of the given product to the cart
     * @return the current quantity of this product
     */
    fun addProduct(product: Product): Single<Int>

    /**
     * Add product with quantity
     * @param product product to add
     * @param quantity exact quantity of product
     */
    fun addProduct(product: Product, quantity: Int): Completable

    /**
     * Remove all occurrence of the given product from the cart
     */
    fun removeAllProducts(product: Product): Completable

    /**
     * Get a Flowable that give you the current total number of product in the cart.
     * If the actual number of item is unknown it will trigger
     * getCartProducts to compute the actual number of products in the cart
     */
    fun getCartProductsCount(): Flowable<Int>

    /**
     * Remove everything from the cart
     */
    fun clear(): Completable
}

internal class CartRepositoryImpl @Inject constructor(
    private val shopifyClient: ShopifyClientWrapper,
    private val currentProfileProvider: CurrentProfileProvider,
    private val cartDao: CartDao
) : CartRepository {

    /**
     * This flowable emit for the current profile all the product that are in the cart with a quantity > 0
     * and it skip the product that are in the DB that might not be at the moment in the shopifyClient.getProducts
     *
     * If we ever have a performance issue here we could share this flowable like we did in CurrentProfileProvider
     */
    override fun getCartProducts(): Flowable<List<QuantityProduct>> = currentProfileProvider.currentProfileFlowable()
        .subscribeOn(Schedulers.io())
        .flatMap(::getQuantityProducts)
        .map { quantityProducts ->
            quantityProducts.filter { it.quantity > 0 } }

    override fun getCartProductsCount(): Flowable<Int> =
        getCartProducts().map(::computeCartCount).hide()

    override fun removeProduct(product: Product): Single<Int> =
        currentProfileProvider.currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .map { profile ->
                val entry = getCartEntryFromProduct(profile.id, product)

                entry?.run {
                    val newEntry = decreaseQuantity()
                    cartDao.insertEntry(newEntry)
                    newEntry.quantity
                } ?: 0
            }

    override fun addProduct(product: Product): Single<Int> =
        currentProfileProvider.currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .map { profile ->
                val entry = getCartEntryFromProduct(profile.id, product)

                val newEntry = entry?.run { increaseQuantity() } ?: product.createEntity(profile.id, 1)

                cartDao.insertEntry(newEntry)
                newEntry.quantity
            }

    override fun addProduct(product: Product, quantity: Int): Completable =
        currentProfileProvider.currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .map { profile ->
                val entry = getCartEntryFromProduct(profile.id, product)

                val newEntry = entry?.run {
                    this.copy(quantity = quantity)
                } ?: product.createEntity(profile.id, quantity)

                cartDao.insertEntry(newEntry)
            }.ignoreElement()

    override fun removeAllProducts(product: Product): Completable {
        return currentProfileProvider.currentProfileSingle()
            .subscribeOn(Schedulers.io())
            .map { profile ->
                val entry = getCartEntryFromProduct(profile.id, product)
                entry?.run {
                    cartDao.insertEntry(this.copy(quantity = 0))
                }
            }.ignoreElement()
    }

    override fun clear(): Completable = currentProfileProvider.currentProfileSingle().subscribeOn(Schedulers.io())
        .flatMapCompletable { profile ->
            Completable.fromAction {
                cartDao.truncateEntriesForProfile(profile.id)
            }
        }

    @VisibleForTesting
    fun getCartEntryFromProduct(profileId: Long, product: Product): CartEntryEntity? =
        cartDao.getEntryByProfileAndProductVariant(
            profileId = profileId,
            productId = product.productId,
            variantId = product.variantId
        )

    @VisibleForTesting
    fun computeCartCount(products: List<QuantityProduct>) = products.map { it.quantity }.sum()

    @VisibleForTesting
    fun getQuantityProducts(profile: Profile): Flowable<List<QuantityProduct>> =
        shopifyClient.getProducts().flatMapPublisher { products ->
            cartDao.getCartEntriesForProfileStream(profile.id).map { entries ->
                entries.mapNotNull { entry ->
                    getQuantityProductFromCartEntry(products, entry)
                }
            }
        }

    // It skips product from the DB that has no match (maybe we need to remove it)
    @VisibleForTesting
    fun getQuantityProductFromCartEntry(products: List<Product>, entry: CartEntryEntity): QuantityProduct? =
        products.firstOrNull { it.productId == entry.productId && it.variantId == entry.variantId }
            ?.let { product ->
                entry.toQuantityProduct(product)
            }
}
