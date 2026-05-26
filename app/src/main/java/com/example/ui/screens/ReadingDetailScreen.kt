package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ReadingEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AstrologyViewModel
import com.example.ui.viewmodel.ReadingUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadingDetailScreen(
    viewModel: AstrologyViewModel,
    onNavigateBack: () -> Unit
) {
    val readingState by viewModel.readingState.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    // Rotation cycle quotes for spiritual loading state
    val loadingStages = listOf(
        "Directing spiritual consciousness to Gemini Flash models...",
        "Aligning lunar constellations in your Lagna house...",
        "Evaluating ancestral dashas & synastry charts...",
        "Translating cosmic Sanskrit into human language..."
    )
    var currentStageIdx by remember { mutableStateOf(0) }

    LaunchedEffect(readingState) {
        if (readingState is ReadingUiState.Loading) {
            while (true) {
                kotlinx.coroutines.delay(2000)
                currentStageIdx = (currentStageIdx + 1) % loadingStages.size
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Celestial Report", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearReadingState()
                        onNavigateBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, "Back Arrow")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = if (isDark) BackgroundDark else BackgroundLight
                )
            )
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (isDark) BackgroundDark else BackgroundLight)
                .padding(16.dp)
        ) {
            when (val state = readingState) {
                is ReadingUiState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No active reading being calculated.", color = OnBackgroundDark.copy(alpha = 0.5f))
                    }
                }

                is ReadingUiState.Loading -> {
                    Column(
                        modifier = Modifier.fillMaxSize().testTag("reading_detail_loading_view"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(30.dp))
                                .background(CardHighlight),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PrimaryGold,
                                strokeWidth = 5.dp,
                                modifier = Modifier.size(72.dp)
                            )
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Active scanning Logo",
                                tint = PrimaryGold,
                                modifier = Modifier.size(32.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Text(
                            text = "CONSULTING CONSTALLATIONS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = SecondaryNebula,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        AnimatedContent(
                            targetState = loadingStages[currentStageIdx],
                            transitionSpec = { fadeIn() togetherWith fadeOut() }
                        ) { txt ->
                            Text(
                                text = txt,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.8f)
                                ),
                                modifier = Modifier.padding(horizontal = 30.dp)
                            )
                        }
                    }
                }

                is ReadingUiState.Success -> {
                    val reading = state.reading
                    var isFavorited by remember(reading.isFavorite) { mutableStateOf(reading.isFavorite) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth().testTag("reading_success_card"),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else SurfaceLight),
                            border = BorderStroke(1.dp, SecondaryNebula.copy(alpha = 0.15f))
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(SecondaryNebula.copy(alpha = 0.2f))
                                            .padding(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = reading.type,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SecondaryNebula
                                        )
                                    }

                                    Row {
                                        // Bookmark / Favorite trigger button
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleFavoriteReading(reading)
                                                isFavorited = !isFavorited
                                            },
                                            modifier = Modifier.testTag("btn_favorite_reading")
                                        ) {
                                            Icon(
                                                imageVector = if (isFavorited) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Bookmark button",
                                                tint = PrimaryGold
                                            )
                                        }

                                        // Share Report Button
                                        IconButton(
                                            onClick = {
                                                // Stylized share triggers via clipboard copying
                                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                val styledText = """
                                                    ⭐⭐ COSMICAI CELESTIAL STUDY ⭐⭐
                                                    Report Type: ${reading.type}
                                                    Title: ${reading.title}
                                                    Metrics: ${reading.keyMetrics}
                                                    ---------------------------------
                                                    ${reading.mainContent}
                                                    ---------------------------------
                                                    Shared via CosmicAI spiritual app. May light guide your paths.
                                                """.trimIndent()
                                                val clip = ClipData.newPlainText("CosmicAI Reading", styledText)
                                                clipboard.setPrimaryClip(clip)
                                                Toast.makeText(context, "Celestial Report copied to spiritual clipboard!", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.testTag("btn_share_reading")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Share,
                                                contentDescription = "Share reading",
                                                tint = TertiaryTeal
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = reading.title,
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontFamily = FontFamily.Serif,
                                        color = if (isDark) PrimaryGold else OnBackgroundLight
                                    )
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Daily Quick suggestions/metrics line
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(CardHighlight, RoundedCornerShape(8.dp))
                                        .padding(8.dp)
                                ) {
                                    Text(
                                        text = "🎯 ${reading.keyMetrics}",
                                        fontSize = 12.sp,
                                        color = TertiaryTeal,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Main Content Report Description
                                Text(
                                    text = reading.mainContent,
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        lineHeight = 24.sp,
                                        fontSize = 15.sp,
                                        color = if (isDark) OnBackgroundDark else OnBackgroundLight
                                    )
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Special Highlight: Karmic AI Remedies block
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    border = BorderStroke(1.5.dp, PrimaryGold.copy(alpha = 0.5f)),
                                    colors = CardDefaults.cardColors(containerColor = CardHighlight.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Remedy Star",
                                            tint = PrimaryGold,
                                            modifier = Modifier.size(24.dp).padding(end = 6.dp)
                                        )
                                        Column {
                                            Text(
                                                "✨ PERSONALIZED PLANETARY REMEDIES",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = PrimaryGold
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Listen or chant your suggested mantras, wear recommended crystals, and implement charitable acts specified in the text above to align retrograde shifts positively.",
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                color = OnBackgroundDark.copy(alpha = 0.8f)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                viewModel.clearReadingState()
                                onNavigateBack()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryNebula)
                        ) {
                            Text("GO BACK TO TEMPLES", fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.height(40.dp))
                    }
                }

                is ReadingUiState.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize().testTag("reading_error_view"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Cosmic Error Icon",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "PLANETARY DEFLECTION",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = state.message,
                            textAlign = TextAlign.Center,
                            color = OnBackgroundDark.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.clearReadingState() },
                            colors = ButtonDefaults.buttonColors(containerColor = SecondaryNebula)
                        ) {
                            Text("RETRY ALIGNMENT")
                        }
                    }
                }
            }
        }
    }
}
