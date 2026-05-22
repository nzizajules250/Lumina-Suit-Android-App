package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.network.*
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StudioViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = MockupRepository(db.mockupDao())

    // Database state flows
    val layoutHistory: StateFlow<List<MockupGeneration>> = repository.allGenerations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val galleryHistory: StateFlow<List<ParsedGallery>> = repository.allParsedGalleries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // E-Commerce Database state flows
    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<BespokeOrder>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wardrobeProfile: StateFlow<WardrobeProfile?> = repository.wardrobeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        // Pre-populate default profile under coroutine scope
        viewModelScope.launch {
            try {
                val current = repository.wardrobeProfile.firstOrNull()
                if (current == null) {
                    repository.saveWardrobeProfile(WardrobeProfile())
                }
            } catch (e: Exception) {
                Log.e("StudioViewModel", "Preseed profile issue: ${e.message}")
            }
        }
    }

    // Cart operations
    fun addToCart(title: String, price: Double, size: String, customNotes: String, imgUrl: String) {
        viewModelScope.launch {
            try {
                val currentList = repository.cartItems.firstOrNull() ?: emptyList()
                val existingItem = currentList.find { it.title == title && it.size == size }
                if (existingItem != null) {
                    repository.updateCartItem(existingItem.copy(
                        quantity = existingItem.quantity + 1,
                        customNotes = if (customNotes.isNotBlank()) customNotes else existingItem.customNotes
                    ))
                } else {
                    repository.insertCartItem(CartItem(
                        title = title,
                        price = price,
                        size = size,
                        quantity = 1,
                        customNotes = customNotes,
                        imgUrl = imgUrl
                    ))
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add item to Cart: ${e.message}"
            }
        }
    }

    fun updateCartItemQuantity(id: Int, newQuantity: Int) {
        viewModelScope.launch {
            try {
                if (newQuantity <= 0) {
                    repository.deleteCartItem(id)
                } else {
                    val currentList = repository.cartItems.firstOrNull() ?: emptyList()
                    val existingItem = currentList.find { it.id == id }
                    if (existingItem != null) {
                        repository.updateCartItem(existingItem.copy(quantity = newQuantity))
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update item quantity: ${e.message}"
            }
        }
    }

    fun removeFromCart(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteCartItem(id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to remove item from Cart: ${e.message}"
            }
        }
    }

    fun checkoutCart(deliveryAddress: String, currentProfileSummary: String) {
        viewModelScope.launch {
            try {
                val items = repository.cartItems.firstOrNull() ?: emptyList()
                if (items.isEmpty()) {
                    _errorMessage.value = "Your Cart is empty."
                    return@launch
                }
                val summary = items.joinToString(", ") { "${it.quantity}x ${it.title} (${it.size})" }
                val totalAmount = items.sumOf { it.price * it.quantity }
                val orderNo = "SR-${(10000..99999).random()}"

                val order = BespokeOrder(
                    orderNumber = orderNo,
                    itemsSummary = summary,
                    totalAmount = totalAmount,
                    fittingsStatus = "Order Placed",
                    deliveryAddress = if (deliveryAddress.isNotBlank()) deliveryAddress else "Atelier Pick-up Boutique, Row 7",
                    customSizeProfile = currentProfileSummary
                )
                repository.insertOrder(order)
                repository.clearCart()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to compile custom order checkout: ${e.message}"
            }
        }
    }

    fun updateWardrobeProfile(chest: String, waist: String, sleeve: String, prefColor: String, lining: String) {
        viewModelScope.launch {
            try {
                repository.saveWardrobeProfile(WardrobeProfile(
                    chestSize = chest,
                    waistSize = waist,
                    sleeveLength = sleeve,
                    preferenceColor = prefColor,
                    liningStyle = lining
                ))
            } catch (e: Exception) {
                _errorMessage.value = "Failed to apply profile changes: ${e.message}"
            }
        }
    }

    // UI state flows
    private val _currentLayout = MutableStateFlow<GeneratedLayout?>(null)
    val currentLayout: StateFlow<GeneratedLayout?> = _currentLayout.asStateFlow()

    private val _currentGallery = MutableStateFlow<ParsedGallery?>(null)
    val currentGallery: StateFlow<ParsedGallery?> = _currentGallery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val layoutAdapter = moshi.adapter(GeneratedLayout::class.java)

    // Method to clear statuses
    fun clearError() {
        _errorMessage.value = null
    }

    fun clearActiveLayout() {
        _currentLayout.value = null
    }

    fun clearActiveGallery() {
        _currentGallery.value = null
    }

    fun selectHistoryLayout(gen: MockupGeneration) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val layout = withContext(Dispatchers.Default) {
                    layoutAdapter.fromJson(gen.jsonLayout)
                }
                _currentLayout.value = layout
                _currentGallery.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load cached mockup layout: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectHistoryGallery(gal: ParsedGallery) {
        _currentGallery.value = gal
        _currentLayout.value = null
    }

    // Call Gemini API to generate mockup JSON layout
    fun generateAIStudioLayout(prompt: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _currentLayout.value = null
            _currentGallery.value = null

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
                _errorMessage.value = "Gemini API key is missing or unset. Please configure GEMINI_API_KEY via the Secrets panel inside your Google AI Studio project settings to run live generations."
                _isLoading.value = false
                return@launch
            }

            try {
                val systemInstruction = Content(
                    parts = listOf(Part(text = """
                        You are a professional UX/UI design assistant specialized in Luxury Tailoring, Bespoke Suits, and Handcrafted Dress Shoes. You generate state-of-the-art interactive mobile layouts for prestigious designer boutiques in raw structured JSON conforming to the requested schema.
                        
                        Schema instructions:
                        - "title": String (The screen title, e.g. "Royal Savile Row Customizer")
                        - "description": String (Subtitle or descriptive tailoring summary of page state, e.g. "Select fabric thread, cuff embroidery, and lining")
                        - "theme": String ("dark" or "light", matching layout style context)
                        - "components": Array of Component Objects.
                        - Each Component Object MUST specify:
                          - "type": String (Must be EXACTLY one of: "header", "stats_row", "feature_card", "action_banner", "list_view", "progress_card")
                          Optional component fields depending on type:
                          - "title": String (Title card label or description block title)
                          - "text": String (Primary detailed info snippet, e.g. "Woven with double-plied superfine merino yarn")
                          - "value": String (Stat value or target metric representation e.g. "$1,450 CAD" or "94% Handfinished")
                          - "color": String ("Primary", "Secondary", "Tertiary", "Success", "Warning", "Error")
                          - "progress": Float (Float fractional value between 0.0 and 1.0, e.g. 0.94)
                          - "items": Array of Strings (For lists or sub-metrics labels, e.g. ["Hand-finished lapel stitching", "Authentic horn buttons", "Pure silk cuff inserts"])
                          - "image": String (Optional hotlink image URL illustrating elements from Unsplash relative to luxury fashion, suits, or dress shoes, e.g. 'https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?w=600')

                        Guidelines:
                        1. Think of visual luxury aesthetics, premium thread options, custom shoe fits, and supply cohesive statistics, pricing, and rich fabric details.
                        2. ALWAYS yield a raw, valid JSON object matching the specs. 
                        3. DO NOT wrap JSON inside markdown blocks (```json ... ```) or output conversational words. Output only pure JSON string starting with { and ending with }.
                    """.trimIndent()))
                )

                val contentRequest = Content(
                    parts = listOf(Part(text = "Build a dynamic screen layout prompt: $prompt"))
                )

                val request = GeminiRequest(
                    contents = listOf(contentRequest),
                    systemInstruction = systemInstruction,
                    generationConfig = GenerationConfig(
                        temperature = 0.7f
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }

                val responseText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (responseText.isNullOrBlank()) {
                    _errorMessage.value = "Gemini API returned an empty layout generation."
                    return@launch
                }

                val cleanedJson = cleanJsonString(responseText)
                val layout = withContext(Dispatchers.Default) {
                    layoutAdapter.fromJson(cleanedJson)
                }

                if (layout != null) {
                    _currentLayout.value = layout
                    // Save to the database
                    withContext(Dispatchers.IO) {
                        repository.insertGeneration(
                            MockupGeneration(
                                prompt = prompt,
                                jsonLayout = cleanedJson
                            )
                        )
                    }
                } else {
                    _errorMessage.value = "Failed to parse the screen mockup response schema."
                }

            } catch (e: Exception) {
                Log.e("StudioViewModel", "Generation Error", e)
                _errorMessage.value = "An error occurred during Gemini layout generation: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // HTML Media Hotlink Parser 
    fun extractGalleryFromHtml(htmlOrUrl: String, title: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _currentLayout.value = null
            _currentGallery.value = null

            if (htmlOrUrl.isBlank() || title.isBlank()) {
                _errorMessage.value = "Both HTML source code and group title must be supplied to launch hotlinker."
                _isLoading.value = false
                return@launch
            }

            try {
                val imageUrls = withContext(Dispatchers.Default) {
                    // Extract img tags src
                    val imgRegex = Regex("""<img[^>]+src=["'](https?://[^"']+)["']""", RegexOption.IGNORE_CASE)
                    val matchesSrc = imgRegex.findAll(htmlOrUrl).map { it.groupValues[1] }.toList()

                    // Extract raw URLs ending with extensions
                    val urlRegex = Regex("""https?://[^\s"'<>]+?\.(?:png|jpg|jpeg|gif|webp|svg)(?:\?[^\s"'<>]*)?""", RegexOption.IGNORE_CASE)
                    val matchesUrls = urlRegex.findAll(htmlOrUrl).map { it.value }.toList()

                    (matchesSrc + matchesUrls).distinct()
                }

                if (imageUrls.isEmpty()) {
                    _errorMessage.value = "No media hotlinks or standard <img src> elements found in the input block."
                    return@launch
                }

                // Serialize the lists of images
                val imageAdapter = moshi.adapter(List::class.java)
                val stringifiedJson = withContext(Dispatchers.Default) {
                    imageAdapter.toJson(imageUrls)
                }

                val gallery = ParsedGallery(
                    sourceHtmlOrUrl = htmlOrUrl,
                    title = title,
                    imageUrlArrayJson = stringifiedJson
                )

                withContext(Dispatchers.IO) {
                    repository.insertGallery(gallery)
                }

                _currentGallery.value = gallery

            } catch (e: Exception) {
                _errorMessage.value = "Parsing error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun removeLayout(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGeneration(id)
        }
    }

    fun removeGallery(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteGallery(id)
        }
    }

    private fun cleanJsonString(raw: String): String {
        var clean = raw.trim()
        if (clean.startsWith("```")) {
            val lines = clean.lines()
            val startIndex = if (lines.first().startsWith("```json") || lines.first().startsWith("```")) 1 else 0
            val endIndex = if (lines.last().trim() == "```") lines.size - 1 else lines.size
            clean = lines.subList(startIndex, endIndex).joinToString("\n").trim()
        }
        return clean
    }
}
