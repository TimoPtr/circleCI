/*
 * Copyright (c) 2020 Kolibree. All rights reserved
 *
 * Copying this file via any medium without the prior written consent of Kolibree is strictly
 * prohibited
 *
 * Proprietary and confidential
 */

package com.kolibree.android.shop.data.repo

import com.kolibree.android.accountinternal.CurrentProfileProvider
import com.kolibree.android.accountinternal.profile.models.Profile
import com.kolibree.android.app.test.BaseUnitTest
import com.kolibree.android.shop.buildProduct
import com.kolibree.android.shop.data.ShopifyClientWrapper
import com.kolibree.android.shop.data.persitence.CartDao
import com.kolibree.android.shop.data.persitence.model.CartEntryEntity
import com.kolibree.android.shop.domain.model.QuantityProduct
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.processors.PublishProcessor
import junit.framework.TestCase.assertNull
import org.junit.Assert.assertEquals
import org.junit.Test

internal class CartRepositoryImplTest : BaseUnitTest() {

    private lateinit var repository: CartRepositoryImpl

    private val shopifyClient = mock<ShopifyClientWrapper>()
    private val currentProfileProvider = mock<CurrentProfileProvider>()
    private val cartDao = mock<CartDao>()
    private val profile = mock<Profile>()
    private val profileId = 1L

    override fun setup() {
        super.setup()

        whenever(profile.id).thenReturn(profileId)
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(Flowable.just(profile))
        whenever(currentProfileProvider.currentProfileSingle()).thenReturn(Single.just(profile))

        repository = spy(CartRepositoryImpl(shopifyClient, currentProfileProvider, cartDao))
    }

    @Test
    fun `getCartProducts returns only product from cart, with quantity greater than 0 and invokes emitCartCount`() {
        val quantityProducts = listOf(
            QuantityProduct(0, mock()),
            QuantityProduct(1, mock()),
            QuantityProduct(0, mock()),
            QuantityProduct(2, mock())
        )
        val expectedQuantities = listOf(quantityProducts[1], quantityProducts[3])

        doReturn(Flowable.just(quantityProducts)).whenever(repository).getQuantityProducts(profile)

        repository.getCartProducts().test().assertComplete()
            .assertValue(expectedQuantities)

        verify(repository).getQuantityProducts(profile)
        verify(currentProfileProvider).currentProfileFlowable()
    }

    @Test
    fun `getCartProducts emits cart product when profile changes`() {
        val processorProfile = PublishProcessor.create<Profile>()
        val profile2 = mock<Profile>()
        whenever(currentProfileProvider.currentProfileFlowable()).thenReturn(processorProfile)

        val quantityProducts = listOf(
            QuantityProduct(0, mock()),
            QuantityProduct(1, mock()),
            QuantityProduct(0, mock()),
            QuantityProduct(2, mock())
        )
        val expectedQuantities = listOf(quantityProducts[1], quantityProducts[3])

        doReturn(Flowable.just(quantityProducts)).whenever(repository).getQuantityProducts(profile)
        doReturn(Flowable.just(emptyList<QuantityProduct>())).whenever(repository).getQuantityProducts(profile2)

        val testObserver = repository.getCartProducts().test()

        processorProfile.offer(profile)
        processorProfile.offer(profile2)
        processorProfile.offer(profile)

        testObserver.assertValues(expectedQuantities, emptyList(), expectedQuantities)

        verify(repository, times(2)).getQuantityProducts(profile)
        verify(repository).getQuantityProducts(profile2)
        verify(currentProfileProvider).currentProfileFlowable()
    }

    @Test
    fun `removeProduct does nothing if product not in the DB and emit 0`() {
        val product = buildProduct()

        doReturn(null).whenever(repository).getCartEntryFromProduct(profileId, product)

        repository.removeProduct(product).test().assertValue(0)

        verify(currentProfileProvider).currentProfileSingle()
        verify(repository).getCartEntryFromProduct(profileId, product)
    }

    @Test
    fun `removeProduct invokes decreaseQuantity on the entity from the DB and emit new quantity and global cart quantity`() {
        val product = buildProduct()
        val entity = mock<CartEntryEntity>()
        val decreasedEntity = mock<CartEntryEntity>()

        doReturn(entity).whenever(repository).getCartEntryFromProduct(profileId, product)
        whenever(entity.quantity).thenReturn(201)
        whenever(decreasedEntity.quantity).thenReturn(200)
        whenever(entity.decreaseQuantity()).thenReturn(decreasedEntity)

        repository.removeProduct(product).test().assertValue(200)

        verify(currentProfileProvider).currentProfileSingle()
        verify(repository).getCartEntryFromProduct(profileId, product)
        verify(cartDao).insertEntry(decreasedEntity)
    }

    @Test
    fun `addProduct adds product to database if not present and emit quantity`() {
        val product = spy(buildProduct())

        doReturn(null).whenever(repository).getCartEntryFromProduct(profileId, product)

        repository.addProduct(product).test().assertComplete().assertValue(1)

        verify(currentProfileProvider).currentProfileSingle()
        verify(cartDao).insertEntry(any())
    }

    @Test
    fun `addProduct invokes increaseQuantity and emit new quantity`() {
        val product = spy(buildProduct())
        val entity = mock<CartEntryEntity>()
        val increasedEntity = mock<CartEntryEntity>()

        whenever(entity.increaseQuantity()).thenReturn(increasedEntity)
        whenever(increasedEntity.quantity).thenReturn(2)
        doReturn(entity).whenever(repository).getCartEntryFromProduct(profileId, product)

        repository.addProduct(product).test().assertComplete().assertValue(2)

        verify(currentProfileProvider).currentProfileSingle()
        verify(entity).increaseQuantity()
        verify(cartDao).insertEntry(increasedEntity)
    }

    @Test
    fun `getCartProductsCount returns cartCount`() {
        doReturn(Flowable.just(emptyList<QuantityProduct>())).whenever(repository).getCartProducts()

        doReturn(10).whenever(repository).computeCartCount(any())

        repository.getCartProductsCount().test().assertValue(10)

        verify(repository).getCartProducts()
        verify(repository).computeCartCount(any())
    }

    @Test
    fun `clear invokes cartDao truncateEntriesForProfile`() {
        repository.clear().test().assertComplete()

        verify(currentProfileProvider).currentProfileSingle()
        verify(cartDao).truncateEntriesForProfile(profileId)
    }

    @Test
    fun `getCartEntryFromProduct invokes cartDao getEntryByProfileAndProductVariant`() {
        val product = buildProduct()
        val entry = mock<CartEntryEntity>()

        whenever(
            cartDao.getEntryByProfileAndProductVariant(
                profileId,
                product.productId,
                product.variantId
            )
        ).thenReturn(entry)

        assertEquals(entry, repository.getCartEntryFromProduct(profileId, product))

        verify(cartDao).getEntryByProfileAndProductVariant(profileId, product.productId, product.variantId)
    }

    @Test
    fun `computeCartCount returns 0 if products is empty`() {
        assertEquals(0, repository.computeCartCount(emptyList()))
    }

    @Test
    fun `computeCartCount emits sum of all product quantity`() {
        val productQuantities = listOf(
            QuantityProduct(0, mock()),
            QuantityProduct(1, mock()),
            QuantityProduct(1000, mock())
        )

        assertEquals(1001, repository.computeCartCount(productQuantities))
    }

    @Test
    fun `getQuantityProducts invokes shopifyClient getProducts and cartDao getCartEntriesForProfileStream and getQuantityProductFromCartEntry`() {
        val product1 = buildProduct(variantOrdinal = 1)
        val product2 = buildProduct(variantOrdinal = 2)

        val products = listOf(product1, product2)
        val entities = listOf(
            CartEntryEntity(profileId, product1.productId, product1.variantId, 0),
            CartEntryEntity(profileId, product2.productId, product2.variantId, 1)
        )
        val quantityProduct = mock<QuantityProduct>()

        doReturn(quantityProduct).whenever(repository).getQuantityProductFromCartEntry(any(), any())
        whenever(shopifyClient.getProducts()).thenReturn(Single.just(products))
        whenever(cartDao.getCartEntriesForProfileStream(profileId)).thenReturn(Flowable.just(entities))

        repository.getQuantityProducts(profile).test().assertValue(listOf(quantityProduct, quantityProduct))

        inOrder(shopifyClient, cartDao) {
            verify(shopifyClient).getProducts()
            verify(cartDao).getCartEntriesForProfileStream(profileId)
        }

        entities.forEach { entity ->
            verify(repository).getQuantityProductFromCartEntry(products, entity)
        }
    }

    @Test
    fun `getQuantityProducts returns only products from the cart that are present in shopifyClient`() {
        val entities = listOf(
            CartEntryEntity(profileId, "hello", "world", 0)
        )

        whenever(shopifyClient.getProducts()).thenReturn(Single.just(emptyList()))
        whenever(cartDao.getCartEntriesForProfileStream(profileId)).thenReturn(Flowable.just(entities))

        repository.getQuantityProducts(profile).test().assertValue(emptyList())

        inOrder(shopifyClient, cartDao) {
            verify(shopifyClient).getProducts()
            verify(cartDao).getCartEntriesForProfileStream(profileId)
        }
    }

    @Test
    fun `getQuantityProductFromCartEntry invokes toQuantityProduct on first product match and returns the result`() {
        val product = buildProduct()
        val entry = CartEntryEntity(profileId, product.productId, product.variantId, 0)
        val quantityProduct = entry.toQuantityProduct(product)
        assertEquals(
            quantityProduct,
            repository.getQuantityProductFromCartEntry(listOf(buildProduct(productOrdinal = 10), product), entry)
        )
    }

    @Test
    fun `getQuantityProductFromCartEntry returns null when product list is empty`() {
        assertNull(repository.getQuantityProductFromCartEntry(emptyList(), CartEntryEntity(profileId, "", "", 0)))
    }

    @Test
    fun `getQuantityProductFromCartEntry returns null when no match between product and entity`() {
        assertNull(
            repository.getQuantityProductFromCartEntry(
                listOf(buildProduct()),
                CartEntryEntity(profileId, "hello", "world", 0)
            )
        )
    }

    @Test
    fun `removeAllProducts sets quantity to 0`() {
        val product = buildProduct()
        val entity = CartEntryEntity(
            profileId = profileId,
            quantity = 12,
            variantId = "variantId",
            productId = "productId"
        )

        doReturn(entity).whenever(repository).getCartEntryFromProduct(profileId, product)

        repository.removeAllProducts(product).test().assertComplete()

        verify(currentProfileProvider).currentProfileSingle()
        verify(repository).getCartEntryFromProduct(profileId, product)
        verify(cartDao).insertEntry(entity.copy(quantity = 0))
    }

    @Test
    fun `addProduct inserts product with an appropriate quantity`() {
        val productId = "product_11:23"
        val variantId = "variant_00:99"
        val product = buildProduct().copy(
            productId = productId,
            variantId = variantId
        )

        doReturn(null).whenever(repository).getCartEntryFromProduct(profileId, product)

        val quantity = 99
        repository.addProduct(product, quantity).test().assertComplete()

        verify(currentProfileProvider).currentProfileSingle()
        verify(repository).getCartEntryFromProduct(profileId, product)

        val expectedEntity = CartEntryEntity(profileId, productId, variantId, quantity)
        verify(cartDao).insertEntry(expectedEntity)
    }
}
