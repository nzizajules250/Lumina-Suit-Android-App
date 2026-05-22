package com.example.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MockupDao {
    @Query("SELECT * FROM mockup_generations ORDER BY timestamp DESC")
    fun getAllGenerations(): Flow<List<MockupGeneration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneration(generation: MockupGeneration)

    @Query("DELETE FROM mockup_generations WHERE id = :id")
    suspend fun deleteGenerationById(id: Int)

    @Query("SELECT * FROM parsed_galleries ORDER BY timestamp DESC")
    fun getAllParsedGalleries(): Flow<List<ParsedGallery>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGallery(gallery: ParsedGallery)

    @Query("DELETE FROM parsed_galleries WHERE id = :id")
    suspend fun deleteGalleryById(id: Int)

    // E-Commerce Cart Items
    @Query("SELECT * FROM cart_items ORDER BY id ASC")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(item: CartItem)

    @Update
    suspend fun updateCartItem(item: CartItem)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteCartItem(id: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()

    // E-Commerce Bespoke Orders
    @Query("SELECT * FROM bespoke_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<BespokeOrder>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: BespokeOrder)

    // Wardrobe Fitting Profile (Singleton)
    @Query("SELECT * FROM wardrobe_profiles WHERE id = 1 LIMIT 1")
    fun getWardrobeProfile(): Flow<WardrobeProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveWardrobeProfile(profile: WardrobeProfile)
}

@Database(
    entities = [
        MockupGeneration::class, 
        ParsedGallery::class, 
        CartItem::class, 
        BespokeOrder::class, 
        WardrobeProfile::class
    ], 
    version = 2, 
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mockupDao(): MockupDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mockup_studio_db"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
