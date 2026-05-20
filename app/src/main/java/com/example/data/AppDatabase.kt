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
}

@Database(entities = [MockupGeneration::class, ParsedGallery::class], version = 1, exportSchema = false)
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
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
