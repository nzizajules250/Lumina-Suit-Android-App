package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mockup_generations")
data class MockupGeneration(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val prompt: String,
    val jsonLayout: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "parsed_galleries")
data class ParsedGallery(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sourceHtmlOrUrl: String,
    val title: String,
    val imageUrlArrayJson: String, // Stringified JSON array of image URLs
    val timestamp: Long = System.currentTimeMillis()
)
