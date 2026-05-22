package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@Composable
fun LayoutRendererContainer(
    layout: GeneratedLayout,
    onActionTriggered: (String) -> Unit
) {
    val isDark = layout.theme?.lowercase() == "dark"
    val backgroundColor = if (isDark) Color(0xFF121212) else Color(0xFFFAFBFD)
    val contentColor = if (isDark) Color.White else Color(0xFF1A1C1E)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header for parsed representation
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = layout.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    )
                    layout.description?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = contentColor.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDark) Color(0xFF2C2C2C) else Color(0xFFECEFF3))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDark) "Dark Mock" else "Light Mock",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            // Draw each component list
            layout.components.forEachIndexed { index, comp ->
                when (comp.type.lowercase().trim()) {
                    "header" -> RenderHeader(comp, contentColor, onActionTriggered)
                    "stats_row" -> RenderStatsRow(comp, isDark, onActionTriggered)
                    "feature_card" -> RenderFeatureCard(comp, isDark, onActionTriggered)
                    "action_banner" -> RenderActionBanner(comp, onActionTriggered)
                    "list_view" -> RenderListView(comp, isDark, onActionTriggered)
                    "progress_card" -> RenderProgressCard(comp, isDark, onActionTriggered)
                    else -> {
                        // Unknown fallback
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF1F3F5)
                            )
                        ) {
                            Text(
                                text = "Element: ${comp.type} (Title: ${comp.title})",
                                modifier = Modifier.padding(12.dp),
                                color = contentColor
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RenderHeader(comp: LayoutComponent, contentColor: Color, onAction: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction("Header Tapped: ${comp.title ?: ""}") }
            .padding(vertical = 4.dp)
    ) {
        val accentColor = getSemanticColor(comp.color, MaterialTheme.colorScheme.primary)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
            Text(
                text = comp.title ?: "Section Section",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                color = contentColor
            )
        }
        comp.text?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun RenderStatsRow(comp: LayoutComponent, isDark: Boolean, onAction: (String) -> Unit) {
    val items = comp.items ?: listOf("Stat 1", "Stat 2")
    val value = comp.value ?: "N/A"
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Main stat card
        Card(
            modifier = Modifier
                .weight(1.2f)
                .clickable { onAction("Main Stat Tapped: $value") },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) Color(0xFF1E293B) else Color(0xFFEFF6FF)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = comp.title ?: "Core Rate",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color(0xFF94A3B8) else Color(0xFF3B82F6)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                    color = if (isDark) Color.White else Color(0xFF1E3A8A)
                )
                comp.text?.let {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Secondary stats side cards
        if (items.isNotEmpty()) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.take(2).forEach { item ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAction("Sub Stat Tapped: $item") },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF8FAFC)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color.White else Color(0xFF334155),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RenderFeatureCard(comp: LayoutComponent, isDark: Boolean, onAction: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction("Feature Card Tapped: ${comp.title ?: ""}") },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF1A1A1A) else Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            comp.image?.let { imgUrl ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                ) {
                    AsyncImage(
                        model = imgUrl,
                        contentDescription = comp.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.5f))
                                )
                            )
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                comp.title?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = if (isDark) Color.White else Color.Black
                    )
                }
                comp.text?.let {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.6f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                comp.progress?.let { p ->
                    Spacer(modifier = Modifier.height(12.dp))
                    val colorAccent = getSemanticColor(comp.color, MaterialTheme.colorScheme.primary)
                    LinearProgressIndicator(
                        progress = { p },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = colorAccent,
                        trackColor = colorAccent.copy(alpha = 0.2f)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Progress: ${(p * 100).toInt()}% Done",
                        style = MaterialTheme.typography.labelSmall,
                        color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun RenderActionBanner(comp: LayoutComponent, onAction: (String) -> Unit) {
    val gradientBrush = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Column(
            modifier = Modifier
                .background(gradientBrush)
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = comp.title ?: "Exclusive Promotion",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = comp.text ?: "Claim premium dynamic access on generated layouts.",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = { onAction("Banner Action: ${comp.value ?: "Proceed"}") },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = comp.value ?: "Get Started",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Composable
fun RenderListView(comp: LayoutComponent, isDark: Boolean, onAction: (String) -> Unit) {
    val items = comp.items ?: emptyList()
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        comp.title?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                color = if (isDark) Color.White else Color.Black,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        items.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAction("List Item Selected: $item") },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF1E1E1E) else Color(0xFFF1F3F5)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = item,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDark) Color.White else Color.Black
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open",
                        modifier = Modifier.size(16.dp),
                        tint = (if (isDark) Color.White else Color.Black).copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun RenderProgressCard(comp: LayoutComponent, isDark: Boolean, onAction: (String) -> Unit) {
    val progress = comp.progress ?: 0.5f
    val accentColor = getSemanticColor(comp.color, MaterialTheme.colorScheme.tertiary)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAction("Progress Metric Clicked: $progress") },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) Color(0xFF231F2E) else Color(0xFFFAF5FF)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = comp.title ?: "Work Productivity Metric",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isDark) Color.White else Color(0xFF581C87)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comp.text ?: "Continuous delivery integration percentage tracking",
                    style = MaterialTheme.typography.bodySmall,
                    color = (if (isDark) Color.White else Color.Black).copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(64.dp)
            ) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.size(64.dp),
                    color = accentColor,
                    strokeWidth = 6.dp
                )
                Text(
                    text = "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Black),
                    color = if (isDark) Color.White else Color(0xFF581C87)
                )
            }
        }
    }
}

private fun getSemanticColor(colorStr: String?, defaultColor: Color): Color {
    return when (colorStr?.lowercase()?.trim()) {
        "primary" -> Color(0xFF3B82F6) // Bright blue
        "secondary" -> Color(0xFF10B981) // Teal-emerald
        "tertiary" -> Color(0xFF8B5CF6) // Royal purple
        "success" -> Color(0xFF22C55E) // Bright green
        "warning" -> Color(0xFFF59E0B) // Amber-orange
        "error" -> Color(0xFFEF4444) // Coral red
        else -> defaultColor
    }
}
