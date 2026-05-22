package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.MockupGeneration
import com.example.data.ParsedGallery
import com.example.ui.*
import com.example.ui.theme.MyApplicationTheme
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

enum class MainTab(val displayName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    PRESETS("Atelier Shop", Icons.Default.ShoppingCart),
    AI_SHAPER("Style Genius", Icons.Default.Star),
    HOTLINKER("Trend Lookbook", Icons.Default.Search),
    INFO("Atelier Details", Icons.Default.Info)
}

class MainActivity : ComponentActivity() {
    private val viewModel: StudioViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: StudioViewModel) {
    var activeTab by remember { mutableStateOf(MainTab.PRESETS) }
    
    // Observers
    val layoutHistory by viewModel.layoutHistory.collectAsStateWithLifecycle()
    val galleryHistory by viewModel.galleryHistory.collectAsStateWithLifecycle()
    val currentLayout by viewModel.currentLayout.collectAsStateWithLifecycle()
    val currentGallery by viewModel.currentGallery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Modal view states
    var activePresetTab by remember { mutableStateOf(MockupType.DASHBOARD) }
    var selectedImageForZoom by remember { mutableStateOf<String?>(null) }
    var interactivePresetMsg by remember { mutableStateOf<String?>(null) }
    var generatedActionMsg by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("app_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "SARTORIAL ATELIER",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            text = "Premium Suits, Dress Shoes & Bespoke Fitting",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = { activeTab = MainTab.INFO }) {
                        Icon(imageVector = Icons.Outlined.Info, contentDescription = "About App")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                MainTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = activeTab == tab,
                        onClick = {
                            activeTab = tab
                            viewModel.clearActiveLayout()
                            viewModel.clearActiveGallery()
                        },
                        icon = { Icon(imageVector = tab.icon, contentDescription = tab.displayName) },
                        label = { Text(text = tab.displayName, style = MaterialTheme.typography.labelSmall) },
                        alwaysShowLabel = true,
                        modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            
            // Primary Loading Indicator Overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.4f))
                        .clickable(enabled = false) {},
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Text(
                                text = "Processing visual media...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Error Toast-style bar at top
            errorMessage?.let { err ->
                LaunchedEffect(err) {
                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                    viewModel.clearError()
                }
            }

            // Standard Screens Layout inside tabs
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn(spring()) togetherWith fadeOut(spring())
                },
                label = "Main_Tab_Navigation"
            ) { targetTab ->
                when (targetTab) {
                    MainTab.PRESETS -> {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Sub Tab Selection Row
                            ScrollableTabRow(
                                selectedTabIndex = activePresetTab.ordinal,
                                edgePadding = 16.dp,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                contentColor = MaterialTheme.colorScheme.onSurface
                            ) {
                                MockupType.values().forEach { type ->
                                    Tab(
                                        selected = activePresetTab == type,
                                        onClick = { activePresetTab = type },
                                        text = { Text(text = type.displayName, style = MaterialTheme.typography.labelMedium) }
                                    )
                                }
                            }
                            PresetScreensContainer(
                                selectedType = activePresetTab,
                                viewModel = viewModel,
                                onInteractiveAction = { action ->
                                    interactivePresetMsg = action
                                }
                            )
                        }
                    }

                    MainTab.AI_SHAPER -> {
                        ShaperTabScreen(
                            viewModel = viewModel,
                            history = layoutHistory,
                            onLayoutSelected = { viewModel.selectHistoryLayout(it) }
                        )
                    }

                    MainTab.HOTLINKER -> {
                        HotlinkerTabScreen(
                            viewModel = viewModel,
                            history = galleryHistory,
                            onGallerySelected = { viewModel.selectHistoryGallery(it) }
                        )
                    }

                    MainTab.INFO -> {
                        InfoTabScreen()
                    }
                }
            }

            // OVERLAYS for viewing generated content
            AnimatedVisibility(
                visible = currentLayout != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                currentLayout?.let { layout ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        LayoutRendererContainer(
                            layout = layout,
                            onActionTriggered = { act ->
                                generatedActionMsg = act
                            }
                        )
                        
                        // Close floating button
                        Button(
                            onClick = { viewModel.clearActiveLayout() },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 32.dp)
                                .testTag("close_layout_renderer_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
                        ) {
                            Text("Exit Layout Preview")
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = currentGallery != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                currentGallery?.let { gallery ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.background)
                    ) {
                        GalleryViewScreen(
                            gallery = gallery,
                            onImageClicked = { selectedImageForZoom = it },
                            onBack = { viewModel.clearActiveGallery() }
                        )
                    }
                }
            }

            // DIALOGS

            // Quick display dialog for dynamic action events (e.g., presets action responses)
            interactivePresetMsg?.let { actionStr ->
                AlertDialog(
                    onDismissRequest = { interactivePresetMsg = null },
                    title = { Text("Simulation Action Successful") },
                    text = {
                        Text("You pressed high-fidelity button trigger: \"$actionStr\". This triggers standard client navigations and state checking in standard deployments.")
                    },
                    confirmButton = {
                        Button(onClick = { interactivePresetMsg = null }) {
                            Text("Continue")
                        }
                    }
                )
            }

            // Tapping dynamic items generated by AI shaper triggers this dialog
            generatedActionMsg?.let { actionStr ->
                AlertDialog(
                    onDismissRequest = { generatedActionMsg = null },
                    title = { Text("Custom Layout Action Triggered") },
                    text = {
                        Text("Your layout successfully emitted click telemetry event: \"$actionStr\". Standard callback handlers parse this string payload to coordinate transitions or forms.")
                    },
                    confirmButton = {
                        Button(onClick = { generatedActionMsg = null }) {
                            Text("Understood")
                        }
                    }
                )
            }

            // Zoom Dialog for Hotlinked images
            selectedImageForZoom?.let { imgUrl ->
                Dialog(onDismissRequest = { selectedImageForZoom = null }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            AsyncImage(
                                model = imgUrl,
                                contentDescription = "Zoomed Media",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = imgUrl,
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 2,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { selectedImageForZoom = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Close Zoom")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ShaperTabScreen(
    viewModel: StudioViewModel,
    history: List<MockupGeneration>,
    onLayoutSelected: (MockupGeneration) -> Unit
) {
    var promptInput by remember { mutableStateOf("") }
    
    val quickPrompts = listOf(
        "Double-Breasted Italian Tuxedo",
        "Richmond Handdyed Oxford Shoes",
        "Savile Row Imperial Velvet Blazer",
        "Goodyear Welted Chelsea Dress Boots"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Screen Generator Prompt",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = promptInput,
                    onValueChange = { promptInput = it },
                    placeholder = { Text("Describe what screen layout to build...") },
                    modifier = Modifier.fillMaxWidth().testTag("prompt_input_field"),
                    shape = RoundedCornerShape(12.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                
                // FlowRow for prompt pills
                Text(
                    text = "Suggestions:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickPrompts.forEach { qp ->
                        SuggestionChip(
                            onClick = { promptInput = qp },
                            label = { Text(text = qp, style = MaterialTheme.typography.bodySmall) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (promptInput.isNotBlank()) {
                            viewModel.generateAIStudioLayout(promptInput)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("generate_layout_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Custom Layout")
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History of Generative Layouts",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "${history.size} Generated",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "No custom layout generations saved yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { gen ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLayoutSelected(gen) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = gen.prompt,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Click to inspect in sandbox shaper",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                            IconButton(onClick = { viewModel.removeLayout(gen.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete Past Generation",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HotlinkerTabScreen(
    viewModel: StudioViewModel,
    history: List<ParsedGallery>,
    onGallerySelected: (ParsedGallery) -> Unit
) {
    var titleInput by remember { mutableStateOf("") }
    var htmlInput by remember { mutableStateOf("") }

    val suggestedTitles = listOf("E-Commerce Mockups", "Travel Images", "Abstract Icons")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "HTML Image Media Hotlinker",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )

                TextField(
                    value = titleInput,
                    onValueChange = { titleInput = it },
                    placeholder = { Text("Input Gallery Group Name...") },
                    modifier = Modifier.fillMaxWidth().testTag("gallery_title_field"),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Examples:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                    )
                    suggestedTitles.forEach { st ->
                        SuggestionChip(
                            onClick = { titleInput = st },
                            label = { Text(text = st, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                TextField(
                    value = htmlInput,
                    onValueChange = { htmlInput = it },
                    placeholder = { Text("Paste raw HTML, tags (<img src=\"...\">), or copy paste multiple URLs directly...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .testTag("html_input_field"),
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Button(
                    onClick = {
                        if (htmlInput.isNotBlank() && titleInput.isNotBlank()) {
                            viewModel.extractGalleryFromHtml(htmlInput, titleInput)
                            htmlInput = ""
                            titleInput = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(44.dp).testTag("extract_hotlinks_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Parse and Render Extract")
                }
            }
        }

        Text(
            text = "Persistent Parsed Galleries",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                    )
                    Text(
                        text = "No media hotlinks parsed yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { gal ->
                    val listImages = parseGalleryImages(gal.imageUrlArrayJson)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onGallerySelected(gal) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = gal.title,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Hotlinks Found: ${listImages.size} images",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                
                                // Mini visual row representing thumbnail images
                                if (listImages.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LazyRow(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        items(listImages.take(5)) { url ->
                                            AsyncImage(
                                                model = url,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(Color.Gray.copy(alpha = 0.2f))
                                            )
                                        }
                                        if (listImages.size > 5) {
                                            item {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(RoundedCornerShape(4.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "+${listImages.size - 5}",
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            IconButton(onClick = { viewModel.removeGallery(gal.id) }) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete Past Gallery",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GalleryViewScreen(
    gallery: ParsedGallery,
    onImageClicked: (String) -> Unit,
    onBack: () -> Unit
) {
    val listImages = parseGalleryImages(gallery.imageUrlArrayJson)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Return")
            }
            Text(
                text = gallery.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${listImages.size} Source Media",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(
            text = "Here are the parsed images hotlinks extracted successfully. Tap any element to trigger high-fidelity immersive zooms.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(listImages) { url ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clickable { onImageClicked(url) },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = url,
                            contentDescription = url,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.6f))
                                    )
                                )
                        )
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Zoom",
                            tint = Color.White,
                            modifier = Modifier.align(Alignment.Center).size(32.dp).background(Color.Black.copy(alpha = 0.4f), CircleShape).padding(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoTabScreen() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Dynamic Native Layout Protocol",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Google AI Studio interprets your natural language prompts and generates a layout instruction schema. The mockups rendered on screen use 100% native Compose components, styled dynamically with custom colors, linear/radial gradients, and active responsive item click-triggers.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.9f)
                )
            }
        }

        Text(
            text = "Supported Elements Schema",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )

        val elementsInfo = listOf(
            Triple("header", "Displays asymmetric displays, horizontal visual bars and bold colors.", Icons.Default.Settings),
            Triple("stats_row", "Plots quantitative figures side by side with customizable visual metrics indicators.", Icons.Default.Check),
            Triple("feature_card", "Renders elegant cards with Unsplash image loading support, progress slider, and text descriptions.", Icons.Default.Home),
            Triple("action_banner", "Highlights promotions utilizing modern colorful gradients and active click triggers.", Icons.Default.PlayArrow),
            Triple("list_view", "Builds lists supporting leading action bullets and navigation details.", Icons.AutoMirrored.Filled.List),
            Triple("progress_card", "Displays cyclic progress arcs and gauges to display system metrics tracking.", Icons.Default.Star)
        )

        elementsInfo.forEach { (type, desc, icon) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                    Column {
                        Text(
                            text = type,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                        )
                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

fun parseGalleryImages(imageUrlArrayJson: String): List<String> {
    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val listAdapter = moshi.adapter(List::class.java)
    return try {
        val parsed = listAdapter.fromJson(imageUrlArrayJson)
        if (parsed is List<*>) {
            parsed.mapNotNull { it?.toString() }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        emptyList()
    }
}
