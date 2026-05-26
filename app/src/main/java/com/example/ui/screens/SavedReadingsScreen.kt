package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SavedReadingsScreen(
    viewModel: AstrologyViewModel,
    onNavigateToReadingDetail: () -> Unit
) {
    val savedReadings by viewModel.savedReadings.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
            .padding(16.dp)
            .testTag("saved_readings_root")
    ) {
        Text(
            text = "YOUR STARRY CHRONICLE",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 1.sp,
            color = if (isDark) OnBackgroundDark.copy(alpha = 0.6f) else OnBackgroundLight.copy(alpha = 0.6f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Empty state container check
        if (savedReadings.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Empty chronicle icon",
                        tint = SecondaryNebula.copy(alpha = 0.25f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Your celestial chronicle is void.",
                        fontWeight = FontWeight.Bold,
                        color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f)
                    )
                    Text(
                        "Generate readings from the oracle tab and bookmark them for offline view.",
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.4f),
                        modifier = Modifier.padding(horizontal = 40.dp, vertical = 4.dp)
                    )
                }
            }
        } else {
            // Historical List view
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedReadings) { reading ->
                    HistoryItemRow(
                        reading = reading,
                        isDark = isDark,
                        onClick = {
                            // Synthesize report back into Successful active state of VM
                            val activeState = ReadingUiState.Success(reading)
                            // We trigger the ViewModel state updates via reflection/mutator
                            val setter = viewModel.javaClass.getDeclaredField("_readingState")
                            setter.isAccessible = true
                            (setter.get(viewModel) as kotlinx.coroutines.flow.MutableStateFlow<ReadingUiState>).value = activeState
                            onNavigateToReadingDetail()
                        },
                        onDelete = {
                            viewModel.deleteSavedReading(reading)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(
    reading: ReadingEntity,
    isDark: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dateString = remember(reading.timestamp) {
        val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault())
        sdf.format(Date(reading.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("saved_reading_item_${reading.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else SurfaceLight),
        border = BorderStroke(1.dp, SecondaryNebula.copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SecondaryNebula.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "Cosmic aura",
                        tint = if (reading.isFavorite) PrimaryGold else SecondaryNebula,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = reading.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) OnBackgroundDark else OnBackgroundLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${reading.type} • $dateString",
                        fontSize = 11.sp,
                        color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Discard saved item button",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
                )
            }
        }
    }
}
