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

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val price: Double,
    val size: String,
    val quantity: Int = 1,
    val customNotes: String = "",
    val imgUrl: String = ""
)

@Entity(tableName = "bespoke_orders")
data class BespokeOrder(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val orderNumber: String,
    val timestamp: Long = System.currentTimeMillis(),
    val itemsSummary: String, // String summary details of items bought
    val totalAmount: Double,
    val fittingsStatus: String = "Order Placed", // "Order Placed", "Fabric Cutting", "Stitching & Fitting", "Ready for Collection"
    val deliveryAddress: String,
    val customSizeProfile: String = "" // Profile used at checkout
)

@Entity(tableName = "wardrobe_profiles")
data class WardrobeProfile(
    @PrimaryKey val id: Int = 1, // Singleton profile
    val chestSize: String = "40R",
    val waistSize: String = "34W",
    val sleeveLength: String = "34R",
    val preferenceColor: String = "Charcoal Navy",
    val liningStyle: String = "Royal Paisley Pattern"
)

