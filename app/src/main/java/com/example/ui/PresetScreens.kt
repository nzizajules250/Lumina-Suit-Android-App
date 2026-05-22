package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.rememberAsyncImagePainter
import com.example.data.CartItem
import com.example.data.BespokeOrder
import com.example.data.WardrobeProfile

// Predefined mockup tabs for luxury boutique
enum class MockupType(val displayName: String) {
    DASHBOARD("Elite Suits"),
    ANALYTICS("Dress Shoes"),
    STORE("Bespoke Cart"),
    PROFILE("My Fit & Orders")
}

@Composable
fun PresetScreensContainer(
    selectedType: MockupType,
    viewModel: StudioViewModel? = null,
    onInteractiveAction: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        when (selectedType) {
            MockupType.DASHBOARD -> MockDashboard(viewModel, onInteractiveAction)
            MockupType.ANALYTICS -> MockAnalytics(viewModel, onInteractiveAction)
            MockupType.STORE -> MockProductStore(viewModel, onInteractiveAction)
            MockupType.PROFILE -> MockUserProfile(viewModel, onInteractiveAction)
        }
    }
}

// 1. ELITE SUITS SCREEN (CATALOG & CUSTOMIZER)
@Composable
fun MockDashboard(viewModel: StudioViewModel?, onAction: (String) -> Unit) {
    val scrollState = rememberScrollState()
    val currentProfileState = viewModel?.wardrobeProfile?.collectAsStateWithLifecycle(null)
    val currentProfile = currentProfileState?.value
    
    // Customization Modal Dialog State
    var selectedSuitForCustomizer by remember { mutableStateOf<BespokeSuitProduct?>(null) }
    var notificationMessage by remember { mutableStateOf<String?>(null) }

    val suits = listOf(
        BespokeSuitProduct(
            "Vincenzo 3-Piece Charcoal Suit",
            1250.00,
            "Masterfully woven Italian S-160 wool featuring premium soft shoulders, elegant peak lapel and authentic horn buttons.",
            "https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?w=600",
            "Charcoal"
        ),
        BespokeSuitProduct(
            "Imperial Royal Navy Tuxedo",
            1450.00,
            "Prestigious double-breasted midnight blazer with genuine silk satin lapels and full tailored hand-finished trousers.",
            "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600",
            "Royal Navy"
        ),
        BespokeSuitProduct(
            "Savile Row Classic Prince of Wales Jacket",
            980.00,
            "Traditional British check tweed blazer lined with rich gold silk paisley, custom tailored with ticket pockets.",
            "https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600",
            "Grey Plaid"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Luxury Greetings Heading
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome Back, Patron",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                Text(
                    text = "Jules Nziza",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFD4AF37)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "JN",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color.Black)
                )
            }
        }

        // Promotional Spotlight Graduated Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("dashboard_hero_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent)
        ) {
            val gradientBrush = Brush.linearGradient(
                colors = listOf(Color(0xFF1E1E1E), Color(0xFF333333))
            )
            Column(
                modifier = Modifier
                    .background(gradientBrush)
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ATELIER SEWING PHASE STATUS",
                        color = Color(0xFFD4AF37),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Active Sewing",
                        tint = Color(0xFFD4AF37)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Bespoke Fitting Progress",
                    color = Color.White,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black)
                )
                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { 0.78f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = Color(0xFFD4AF37),
                    trackColor = Color.White.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Phase 3: Fabric Lining Hand-stitched. Custom cuff buttoning in-progress.",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Text(
            text = "Bespoke Suit Collection",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
        )

        // Suit Catalog Grid
        suits.forEach { suit ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { selectedSuitForCustomizer = suit },
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column {
                    Image(
                        painter = rememberAsyncImagePainter(model = suit.imgUrl),
                        contentDescription = suit.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .background(Color.Gray.copy(alpha = 0.1f)),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = suit.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$${String.format("%.2f", suit.price)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                ),
                                modifier = Modifier.padding( someHorizontalSafeGap() )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = suit.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { selectedSuitForCustomizer = suit },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFD4AF37),
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Settings, contentDescription = "Tailor")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bespoke Customizer", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Dynamic success notifications toast
        notificationMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, tint = Color(0xFFD4AF37), contentDescription = "Saved")
                    Text(text = msg, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3500)
                notificationMessage = null
            }
        }
    }

    // TAILORING CUSTOMIZER MODAL
    selectedSuitForCustomizer?.let { suit ->
        Dialog(onDismissRequest = { selectedSuitForCustomizer = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                val scrollStateInner = rememberScrollState()
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(scrollStateInner),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tailor Customizer",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFFD4AF37)
                        )
                        IconButton(onClick = { selectedSuitForCustomizer = null }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Image(
                        painter = rememberAsyncImagePainter(model = suit.imgUrl),
                        contentDescription = suit.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )

                    Text(text = suit.title, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))

                    var selectedLining by remember { mutableStateOf("Royal Paisley Pattern") }
                    var monogramInitials by remember { mutableStateOf("J.N.") }
                    var chosenSize by remember { mutableStateOf(currentProfile?.chestSize ?: "40R") }

                    Text(text = "Choose Fabric Design Pattern", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    val linings = listOf("Royal Paisley Pattern", "Midnight Silk Satin", "Imperial Red Tartan", "Burgundy Brocade")
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(linings) { item ->
                            val active = item == selectedLining
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (active) Color(0xFFD4AF37) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedLining = item }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = item,
                                    fontSize = 11.sp,
                                    color = if (active) Color.Black else MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Predefined Chest Size Options
                    Text(text = "Bespoke Fitting Size", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    val chestSizes = listOf("38S", "40R", "42R", "44L", "46R")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        chestSizes.forEach { sz ->
                            val active = sz == chosenSize
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1.2f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (active) Color(0xFFD4AF37) else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { chosenSize = sz },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sz,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (active) Color.Black else MaterialTheme.onSurface()
                                )
                            }
                        }
                    }

                    Text(text = "Personalized Lapel Monogram Embroidery", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    OutlinedTextField(
                        value = monogramInitials,
                        onValueChange = { monogramInitials = it },
                        placeholder = { Text("E.g. J.N.") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            val notes = "Lining: $selectedLining. Monogram: $monogramInitials"
                            viewModel?.addToCart(suit.title, suit.price, chosenSize, notes, suit.imgUrl)
                            onAction("Added $chosenSize ${suit.title} to custom Atelier Cart")
                            notificationMessage = "Tailored ${suit.title} added to Wardrobe Cart!"
                            selectedSuitForCustomizer = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Add")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add to Atelier Cart - $${String.format("%.2f", suit.price)}", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// Helper functions for responsive UI spacing safely
private fun someHorizontalSafeGap() = 4.dp

@Composable
fun MaterialTheme.onSurface(): Color {
    return MaterialTheme.colorScheme.onSurface
}

data class BespokeSuitProduct(
    val title: String,
    val price: Double,
    val desc: String,
    val imgUrl: String,
    val defaultColor: String
)

// II. DRESS SHOES SCREEN (CHART & FITTING METRICS)
@Composable
fun MockAnalytics(viewModel: StudioViewModel?, onAction: (String) -> Unit) {
    val scrollState = rememberScrollState()
    var selectedShoeSize by remember { mutableStateOf("9 D (Standard)") }
    var notificationMessage by remember { mutableStateOf<String?>(null) }
    var configuredWidth by remember { mutableStateOf("D (Default)") }

    val shoes = listOf(
        BespokeShoeProduct(
            "Richmond Handdyed Antique Brogues",
            390.00,
            "Handfinished Italian calfskin with precise hand-punched full medallion brogue detail and durable Blake-stitched sole.",
            "https://images.unsplash.com/photo-1614252235316-8c857d38b5f4?w=600"
        ),
        BespokeShoeProduct(
            "Windsor Patent Black Oxford Court Shoes",
            440.00,
            "Pristine luxury high-sheen executive patent leather Oxfords perfect for the most exclusive black-tie events.",
            "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=600"
        ),
        BespokeShoeProduct(
            "Savile Welted Goodyear Monk Strap",
            420.00,
            "Rich deep mahogany leather shoes crafted with double brass buckle fasteners and an elegant premium leather storm welt.",
            "https://images.unsplash.com/photo-1608256246200-53e635b5b65f?w=600"
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Handcrafted Shoe Boutique",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        // Custom Canvas Shoe Fit Metric Curve Analyzer Chart
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shoe Width / Pressure Curve",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "Fit Width: $configuredWidth",
                        color = Color(0xFFD4AF37),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))

                // Canvas line graph representing width sizing ratios of luxury fits
                val lineColor = Color(0xFFD4AF37)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                ) {
                    val width = size.width
                    val height = size.height
                    
                    // Simple dynamic scaling points depending on selected shoe width
                    val offsetMultiplier = when(configuredWidth) {
                        "Narrow A" -> 0.35f
                        "EE Wider" -> 0.88f
                        "Wide E" -> 0.77f
                        else -> 0.55f
                    }

                    val points = listOf(
                        Offset(0f, height * 0.85f),
                        Offset(width * 0.25f, height * (0.60f - offsetMultiplier * 0.2f)),
                        Offset(width * 0.5f, height * (0.25f + offsetMultiplier * 0.15f)),
                        Offset(width * 0.75f, height * (0.75f - offsetMultiplier * 0.3f)),
                        Offset(width, height * 0.4f)
                    )

                    for (i in 0 until points.size - 1) {
                        drawLine(
                            color = lineColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 6f,
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 12f,
                            center = points[i]
                        )
                    }
                    drawCircle(
                        color = lineColor,
                        radius = 12f,
                        center = points.last()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Tab selection row for width fits
                val widths = listOf("Narrow A", "D (Default)", "Wide E", "EE Wider")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    widths.forEach { wd ->
                        val isSelected = wd == configuredWidth
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color(0xFFD4AF37) else MaterialTheme.colorScheme.surface)
                                .clickable { configuredWidth = wd }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = wd,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "Signature Footwear Catalog",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        shoes.forEach { shoe ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = shoe.imgUrl),
                        contentDescription = shoe.title,
                        modifier = Modifier
                            .size(96.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Gray.copy(alpha = 0.2f)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .height(96.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = shoe.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = shoe.desc,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$${String.format("%.2f", shoe.price)}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFD4AF37)
                                )
                            )
                            Button(
                                onClick = {
                                    val sizeLabel = "Size $selectedShoeSize, Width $configuredWidth"
                                    viewModel?.addToCart(shoe.title, shoe.price, sizeLabel, "Hand-polished shine.", shoe.imgUrl)
                                    onAction("Custom fit configured: ${shoe.title}")
                                    notificationMessage = "Shoes: ${shoe.title} added to Dress Wardrobe Cart!"
                                },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                                modifier = Modifier.height(28.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black)
                            ) {
                                Icon(imageVector = Icons.Default.Add, modifier = Modifier.size(14.dp), contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add to Cart", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }

        // Active selection sizing panel
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Calfskin Shoe Sizing Selected:", style = MaterialTheme.typography.bodyMedium)
                val sizes = listOf("8 Soft", "9 D", "10 D", "11 Wide")
                var selectedColIndex by remember { mutableStateOf(1) }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    sizes.forEachIndexed { i, s ->
                        val actAndHighlight = i == selectedColIndex
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (actAndHighlight) Color(0xFFD4AF37) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    selectedColIndex = i
                                    selectedShoeSize = s
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = s,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (actAndHighlight) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Action feedback dialog
        notificationMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Done, tint = Color(0xFFD4AF37), contentDescription = "Active")
                    Text(text = msg, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3000)
                notificationMessage = null
            }
        }
    }
}

data class BespokeShoeProduct(
    val title: String,
    val price: Double,
    val desc: String,
    val imgUrl: String
)

// III. FULL INTERACTIVE SHOPPING CART SCREEN (STORE)
@Composable
fun MockProductStore(viewModel: StudioViewModel?, onAction: (String) -> Unit) {
    val itemsState = viewModel?.cartItems?.collectAsStateWithLifecycle(emptyList())
    val cartItems = itemsState?.value ?: emptyList()
    
    val profileState = viewModel?.wardrobeProfile?.collectAsStateWithLifecycle(null)
    val userProfile = profileState?.value

    var promoInput by remember { mutableStateOf("") }
    var promoValidPercent by remember { mutableStateOf(0.0) }
    var userPromoNotes by remember { mutableStateOf("") }
    var deliveryAddressInput by remember { mutableStateOf("Savile luxury apartments unit 49B, London") }
    var finalReceiptForCheckout by remember { mutableStateOf<BespokeOrderReceipt?>(null) }

    val rawSubtotal = cartItems.sumOf { it.price * it.quantity }
    val artisanDiscount = rawSubtotal * promoValidPercent
    val luxuryArtisanTax = (rawSubtotal - artisanDiscount) * 0.08
    val expressCouriersDelivery = if (rawSubtotal > 1000.00 || rawSubtotal == 0.0) 0.00 else 50.00
    val grandTotal = (rawSubtotal - artisanDiscount) + luxuryArtisanTax + expressCouriersDelivery

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Atelier Shopping Bag",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD4AF37).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.ShoppingCart, tint = Color(0xFFD4AF37), contentDescription = null, modifier = Modifier.size(32.dp))
                    }
                    Text(
                        text = "Your Wardrobe Bag is Empty",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Customize bespoke suits or fine calfskin footwear from previous tabs to curate items for checkout fitting.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // LazyColumn to host items in bag
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(cartItems) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter(model = item.imgUrl),
                                contentDescription = item.title,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = item.title, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(text = "Fitting Profile: ${item.size}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD4AF37), fontWeight = FontWeight.SemiBold)
                                if (item.customNotes.isNotBlank()) {
                                    Text(text = "Monogram: ${item.customNotes}", style = MaterialTheme.typography.bodySmall, fontSize = 10.sp, maxLines = 1)
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(text = "$${String.format("%.2f", item.price * item.quantity)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                
                                // Steppers Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    IconButton(
                                        onClick = { viewModel?.updateCartItemQuantity(item.id, item.quantity - 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.KeyboardArrowDown, contentDescription = "Decrease")
                                    }
                                    Text(text = "${item.quantity}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    IconButton(
                                        onClick = { viewModel?.updateCartItemQuantity(item.id, item.quantity + 1) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "Increase")
                                    }
                                    IconButton(
                                        onClick = { viewModel?.removeFromCart(item.id) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(imageVector = Icons.Outlined.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary Checkout Card Panel
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Promo Input Area
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = promoInput,
                            onValueChange = { promoInput = it },
                            placeholder = { Text("Enter Boutique Code: ROYAL20") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Button(
                            onClick = {
                                if (promoInput.trim().equals("ROYAL20", ignoreCase = true)) {
                                    promoValidPercent = 0.20
                                    userPromoNotes = "Royal 20% Discount Code applied!"
                                } else if (promoInput.trim().equals("SARTORIAL10", ignoreCase = true)) {
                                    promoValidPercent = 0.10
                                    userPromoNotes = "Sartorial 10% Discount Code applied!"
                                } else {
                                    promoValidPercent = 0.0
                                    userPromoNotes = "Invalid Promo Code Entered."
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black)
                        ) {
                            Text("Apply", fontWeight = FontWeight.Bold)
                        }
                    }
                    if (userPromoNotes.isNotBlank()) {
                        Text(
                            text = userPromoNotes,
                            color = if (promoValidPercent > 0.0) Color(0xFFD4AF37) else MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Numeric lines
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Raw Custom Subtotal", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "$${String.format("%.2f", rawSubtotal)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    if (artisanDiscount > 0) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "Atelier Promo Discount (-${(promoValidPercent * 100).toInt()}%)", color = Color(0xFFD4AF37), style = MaterialTheme.typography.bodyMedium)
                            Text(text = "-$${String.format("%.2f", artisanDiscount)}", color = Color(0xFFD4AF37), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Couriers Shipping Fees (Global Fit)", style = MaterialTheme.typography.bodyMedium)
                        Text(text = if (expressCouriersDelivery == 0.0) "FREE" else "$${String.format("%.2f", expressCouriersDelivery)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Luxury Tailor Tax (8%)", style = MaterialTheme.typography.bodyMedium)
                        Text(text = "$${String.format("%.2f", luxuryArtisanTax)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = "Craft Total Sizing", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                        Text(text = "$${String.format("%.2f", grandTotal)}", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = Color(0xFFD4AF37)))
                    }

                    // Delivery input
                    Text(text = "Delivery Sizing Destination Address:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    OutlinedTextField(
                        value = deliveryAddressInput,
                        onValueChange = { deliveryAddressInput = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp)
                    )

                    Button(
                        onClick = {
                            val itemsSummary = cartItems.joinToString("\n") { "${it.quantity}x ${it.title} (Size: ${it.size})" }
                            val profileSummary = userProfile?.let {
                                "Chest: ${it.chestSize}, Waist: ${it.waistSize}, Sleeve: ${it.sleeveLength}"
                            } ?: "Standard Sizing"

                            viewModel?.checkoutCart(deliveryAddressInput, profileSummary)
                            onAction("Dispatched Custom Sartorial Order Checkout")
                            
                            // Load receipt detail view
                            finalReceiptForCheckout = BespokeOrderReceipt(
                                orderNo = "SR-${(10000..99999).random()}",
                                itemsStr = itemsSummary,
                                grandTotal = grandTotal,
                                address = deliveryAddressInput
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = "Secure Payment")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Secure Bespoke Checkout", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // SUCCESS CHECKOUT DRAWER DIALOG
    finalReceiptForCheckout?.let { rc ->
        Dialog(onDismissRequest = { 
            finalReceiptForCheckout = null 
        }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD4AF37).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.CheckCircle, tint = Color(0xFFD4AF37), contentDescription = null, modifier = Modifier.size(36.dp))
                    }

                    Text(
                        text = "Atelier Order Placed!",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Thank you for supporting hand-crafted luxury. Your private order has been successfully logged with our London tailoring team.",
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Order Number", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                Text(text = rc.orderNo, style = MaterialTheme.typography.bodySmall, color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                            Text(text = "Tailor Sizing Spec details:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Text(text = rc.itemsStr, style = MaterialTheme.typography.labelSmall, maxLines = 4, overflow = TextOverflow.Ellipsis)
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "Final Total Paid", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(text = "$${String.format("%.2f", rc.grandTotal)}", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFD4AF37), fontWeight = FontWeight.Black)
                            }
                        }
                    }

                    Button(
                        onClick = { finalReceiptForCheckout = null },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Continue shopping", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

data class BespokeOrderReceipt(
    val orderNo: String,
    val itemsStr: String,
    val grandTotal: Double,
    val address: String
)

// IV. WARDROBE FITTING & ORDERS HISTORY SCREEN (PROFILE)
@Composable
fun MockUserProfile(viewModel: StudioViewModel?, onAction: (String) -> Unit) {
    val scrollState = rememberScrollState()
    
    val profileState = viewModel?.wardrobeProfile?.collectAsStateWithLifecycle(null)
    val userProfile = profileState?.value

    val ordersState = viewModel?.allOrders?.collectAsStateWithLifecycle(emptyList())
    val completedOrders = ordersState?.value ?: emptyList()

    var editingChest by remember { mutableStateOf("40R") }
    var editingWaist by remember { mutableStateOf("34W") }
    var editingSleeve by remember { mutableStateOf("34R") }
    var editingLining by remember { mutableStateOf("Royal Paisley Pattern") }
    var editingColor by remember { mutableStateOf("Charcoal Navy") }

    LaunchedEffect(userProfile) {
        userProfile?.let {
            editingChest = it.chestSize
            editingWaist = it.waistSize
            editingSleeve = it.sleeveLength
            editingLining = it.liningStyle
            editingColor = it.preferenceColor
        }
    }

    var successProfileMsg by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Upper background royal masterheader
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF333333), Color(0xFF111111))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD4AF37)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = Color.Black
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Jules Nziza's Bespoke Lounge",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        }

        // 1. ORDER DETAILS & TAILOR SEWING TRACKER
        Text(
            text = "Active Tailoring Phases",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        if (completedOrders.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.List, tint = Color.Gray, contentDescription = "Empty History")
                    Text(
                        text = "No active custom orders tracked.",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        } else {
            completedOrders.forEach { order ->
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Bespoke Order: ${order.orderNumber}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = Color(0xFFD4AF37))
                            )
                            Text(
                                text = "Total: $${String.format("%.2f", order.totalAmount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = order.itemsSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.15f))
                        
                        // SEWING PROCESS TRACKER
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "Sewing & Fitting Tracker Status:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Add, tint = Color(0xFFD4AF37), contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Logged", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.weight(1f).height(2.dp).background(Color(0xFFD4AF37)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Build, tint = Color(0xFFD4AF37), contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Cutting", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                                Box(modifier = Modifier.weight(1f).height(2.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Check, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Fitting", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                                Box(modifier = Modifier.weight(1f).height(2.dp).background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f)))
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(imageVector = Icons.Default.Send, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f), contentDescription = null, modifier = Modifier.size(16.dp))
                                    Text("Shipped", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. MEASUREMENT SLIDERS
        Text(
            text = "Your Private Wardrobe Specifications",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Chest Slider options
                Text(text = "Chest Frame: $editingChest (Inches)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                val chestSizesList = listOf("36S", "38S", "40R", "42R", "44L", "46R")
                var selectedChestIndex by remember { mutableStateOf(1) }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    chestSizesList.forEachIndexed { idx, item ->
                        val act = item == editingChest
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (act) Color(0xFFD4AF37) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    editingChest = item
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (act) Color.Black else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Waist Sizes Row
                Text(text = "Waist Sizing Profile: $editingWaist", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                val waistSizesList = listOf("30W", "32W", "34W", "36W", "38W")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    waistSizesList.forEach { item ->
                        val act = item == editingWaist
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (act) Color(0xFFD4AF37) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    editingWaist = item
                                }
                                .padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = item, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (act) Color.Black else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                // Fabric Color Input
                Text(text = "Ideal Wool Thread Color Pattern", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editingColor,
                    onValueChange = { editingColor = it },
                    placeholder = { Text("E.g. Charcoal Navy / Olive Tweed") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                // Lining Material
                Text(text = "Preferred Blazer Lining Fabric", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = editingLining,
                    onValueChange = { editingLining = it },
                    placeholder = { Text("E.g. Royal Maroon Silk") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = {
                        viewModel?.updateWardrobeProfile(
                            chest = editingChest,
                            waist = editingWaist,
                            sleeve = editingSleeve,
                            prefColor = editingColor,
                            lining = editingLining
                        )
                        onAction("Updated Wardrobe Fitting Specifications Profiles")
                        successProfileMsg = "Bespoke Fittings profile updated successfully!"
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save Specs")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Sizing Profile", fontWeight = FontWeight.Bold)
                }
            }
        }

        successProfileMsg?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, tint = Color(0xFFD4AF37), contentDescription = null)
                    Text(text = msg, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                }
            }
            LaunchedEffect(msg) {
                kotlinx.coroutines.delay(3500)
                successProfileMsg = null
            }
        }
    }
}
