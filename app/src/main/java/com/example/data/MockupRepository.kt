package com.example.data

import kotlinx.coroutines.flow.Flow

class MockupRepository(private val mockupDao: MockupDao) {
    val allGenerations: Flow<List<MockupGeneration>> = mockupDao.getAllGenerations()
    val allParsedGalleries: Flow<List<ParsedGallery>> = mockupDao.getAllParsedGalleries()

    // E-Commerce states
    val cartItems: Flow<List<CartItem>> = mockupDao.getCartItems()
    val allOrders: Flow<List<BespokeOrder>> = mockupDao.getAllOrders()
    val wardrobeProfile: Flow<WardrobeProfile?> = mockupDao.getWardrobeProfile()

    suspend fun insertGeneration(generation: MockupGeneration) = mockupDao.insertGeneration(generation)

    suspend fun deleteGeneration(id: Int) = mockupDao.deleteGenerationById(id)

    suspend fun insertGallery(gallery: ParsedGallery) = mockupDao.insertGallery(gallery)

    suspend fun deleteGallery(id: Int) = mockupDao.deleteGalleryById(id)

    // E-Commerce operations
    suspend fun insertCartItem(item: CartItem) = mockupDao.insertCartItem(item)

    suspend fun updateCartItem(item: CartItem) = mockupDao.updateCartItem(item)

    suspend fun deleteCartItem(id: Int) = mockupDao.deleteCartItem(id)

    suspend fun clearCart() = mockupDao.clearCart()

    suspend fun insertOrder(order: BespokeOrder) = mockupDao.insertOrder(order)

    suspend fun saveWardrobeProfile(profile: WardrobeProfile) = mockupDao.saveWardrobeProfile(profile)
}
