package com.example.ui

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GeneratedLayout(
    val title: String,
    val description: String? = null,
    val theme: String? = "dark", // "dark" or "light"
    val components: List<LayoutComponent>
)

@JsonClass(generateAdapter = true)
data class LayoutComponent(
    val type: String, // "header", "stats_row", "feature_card", "action_banner", "list_view", "progress_card"
    val title: String? = null,
    val text: String? = null,
    val value: String? = null,
    val color: String? = null, // "Primary", "Secondary", "Tertiary", "Success", "Warning", "Error"
    val progress: Float? = null, // between 0.0 and 1.0
    val items: List<String>? = null, // for lists or items
    val image: String? = null // optional hotlink image URL
)
