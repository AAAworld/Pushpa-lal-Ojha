package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AstrologyViewModel

@Composable
fun DashboardScreen(
    viewModel: AstrologyViewModel,
    onNavigateToReadingDetail: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val context = LocalContext.current

    val user = currentUser ?: return

    var showLoveDialog by remember { mutableStateOf(false) }
    var showTarotDialog by remember { mutableStateOf(false) }
    var showPalmDialog by remember { mutableStateOf(false) }

    // Love fields
    var partnerName by remember { mutableStateOf("Ananya Iyer") }
    var partnerDob by remember { mutableStateOf("1996-08-20") }
    var partnerZodiac by remember { mutableStateOf("Leo") }

    // Tarot cards state
    val selectedTarotCards = remember { mutableStateListOf<String>() }

    // Fast Astro-Chat quick consultation box state
    var showQuickChatDialog by remember { mutableStateOf(false) }
    var quickChatPrompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("dashboard_root_container")
    ) {
        // --- AstroAI Bento Header ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AstroAI",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp,
                    color = if (isDark) SecondaryNebula else OnBackgroundLight
                )
                Text(
                    text = "COSMIC INTELLIGENCE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Notifications icon mockup
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isDark) SurfaceDark else Color.LightGray.copy(alpha = 0.3f))
                        .border(1.dp, (if (isDark) Color(0xFF49454F) else Color.LightGray).copy(alpha = 0.5f), CircleShape)
                        .clickable {
                            viewModel.simulateAlertTrigger("Jupiter transition is entering alignment with your natal chart.")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = "Notifications",
                        tint = if (isDark) SecondaryNebula else Color.Gray,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(SecondaryNebula, PrimaryGoldVariant)
                            )
                        )
                        .border(1.5.dp, SecondaryNebula.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    val initials = if (user.name.length >= 2) user.name.take(2).uppercase() else "JD"
                    Text(
                        text = initials,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // --- BENTO GRID LAYOUT ---

        // 1. Bento Card: Featured Daily Horoscope (Full-Width / Grid Row 1 & 2)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("btn_daily_horoscope")
                .clickable {
                    viewModel.generateDailyHoroscope()
                    onNavigateToReadingDetail()
                },
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isDark) SurfaceDark else SurfaceLight
            ),
            border = BorderStroke(1.dp, (if (isDark) Color(0xFF49454F) else Color.LightGray).copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (isDark) {
                            Brush.verticalGradient(listOf(SurfaceDark, CardHighlight))
                        } else {
                            Brush.verticalGradient(listOf(SurfaceLight, Color(0xFFF9F6FF)))
                        }
                    )
                    .padding(20.dp)
            ) {
                // Glow mesh background spot
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .align(Alignment.TopEnd)
                        .background(SecondaryNebula.copy(alpha = 0.08f), CircleShape)
                )

                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isDark) PrimaryGoldVariant.copy(alpha = 0.4f) else Color(0x22381E72),
                            contentColor = if (isDark) SecondaryNebula else PrimaryGoldVariant
                        ) {
                            Text(
                                text = "DAILY HOROSCOPE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                letterSpacing = 1.sp
                            )
                        }

                        Text(
                            text = "${user.zodiac} • Lagna Chart",
                            fontSize = 11.sp,
                            color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.6f),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Golden Opportunities",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) OnBackgroundDark else OnBackgroundLight
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "\"Your alignment with Jupiter suggests a windfall in creative endeavors today. Trust your intuition and take action.\"",
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Cosmic link",
                            tint = SecondaryNebula,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Tap to cast daily celestial alignment report",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryNebula
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 2. Bento Row 2: Two Columns Asymmetrical
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Left Card: Ask Gemini Astro-Chat (High Highlight Lavender Background)
            Card(
                modifier = Modifier
                    .weight(1.1f)
                    .height(200.dp)
                    .testTag("btn_quick_consult_chat")
                    .clickable {
                        showQuickChatDialog = true
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) SecondaryNebula else Color(0xFFE9E5F3)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isDark) PrimaryGoldVariant else Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Gemini",
                            tint = if (isDark) Color.White else PrimaryGoldVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "Ask Gemini\nAstro-Chat",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDark) PrimaryGoldVariant else OnBackgroundLight,
                            lineHeight = 22.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "AI GUIDANCE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = (if (isDark) PrimaryGoldVariant else OnBackgroundLight).copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Right Stack Column: Kundli & Love Match
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(200.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Kundli Study Bento Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("btn_kundli_analysis")
                        .clickable {
                            viewModel.generateKundliAnalysis()
                            onNavigateToReadingDetail()
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) SurfaceDark else SurfaceLight
                    ),
                    border = BorderStroke(1.dp, (if (isDark) Color(0xFF49454F) else Color.LightGray).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(SecondaryNebula.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.GridView,
                                contentDescription = "Kundli icon",
                                tint = SecondaryNebula,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Kundli Study",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) OnBackgroundDark else OnBackgroundLight
                            )
                            Text(
                                text = "Vedic rashi report",
                                fontSize = 10.sp,
                                color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Love Match Bento Box
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("btn_love_compatibility")
                        .clickable {
                            showLoveDialog = true
                        },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) SurfaceDark else SurfaceLight
                    ),
                    border = BorderStroke(1.dp, (if (isDark) Color(0xFF49454F) else Color.LightGray).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Red.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Love icon",
                                tint = Color.Red,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Love Match",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) OnBackgroundDark else OnBackgroundLight
                            )
                            Text(
                                text = "Compatibility soul",
                                fontSize = 10.sp,
                                color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Bento Row 3: Tarot & Lucky Numbers (Asymmetrical Row)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Tarot Draw Bento (Col span 1 / Width 1/3)
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(180.dp)
                    .testTag("btn_tarot")
                    .clickable {
                        selectedTarotCards.clear()
                        showTarotDialog = true
                    },
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) Color(0xFF49454F) else Color(0xFFEBEAEF)
                ),
                border = BorderStroke(1.dp, (if (isDark) Color(0xFF938F99) else Color.LightGray).copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Style,
                        contentDescription = "Tarot deck icon",
                        tint = if (isDark) SecondaryNebula else Color.Gray,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "TAROT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp,
                        color = if (isDark) OnBackgroundDark else OnBackgroundLight
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "3-Card Draw",
                        fontSize = 11.sp,
                        color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.6f)
                    )
                }
            }

            // Lucky Numbers Statistics Bento Row (Width 2.2/3)
            Card(
                modifier = Modifier
                    .weight(2.2f)
                    .height(180.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight
                ),
                border = BorderStroke(1.dp, (if (isDark) Color(0xFF49454F) else Color.LightGray).copy(alpha = 0.4f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lucky Numbers",
                            fontSize = 11.sp,
                            color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF00FF00), CircleShape))
                            Box(modifier = Modifier.size(6.dp).background(SecondaryNebula, CircleShape))
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "07",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Light,
                                color = SecondaryNebula
                            )
                            Text("Main", fontSize = 8.sp, color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "22",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                color = if (isDark) OnBackgroundDark else OnBackgroundLight
                            )
                            Text("Spirit", fontSize = 8.sp, color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "45",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Light,
                                color = if (isDark) OnBackgroundDark else OnBackgroundLight
                            )
                            Text("Wealth", fontSize = 8.sp, color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Lucky color tag bar
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = if (isDark) PrimaryGoldVariant.copy(alpha = 0.15f) else Color(0x1F381E72),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LUCKY COLOR:",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) SecondaryNebula else PrimaryGoldVariant
                            )
                            Text(
                                text = "Mystic Amber",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else PrimaryGoldVariant
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 4. Bento Row 4: Palmistry & Career
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Palmistry Bento
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .testTag("btn_palmistry")
                    .clickable {
                        showPalmDialog = true
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight
                ),
                border = BorderStroke(1.dp, (if (isDark) Color(0xFF49454F) else Color.LightGray).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(TertiaryTeal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FrontHand,
                            contentDescription = "Palmistry scan",
                            tint = TertiaryTeal,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Palm Scan",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) OnBackgroundDark else OnBackgroundLight
                        )
                        Text(
                            text = "Aura lining mapping",
                            fontSize = 10.sp,
                            color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Career predict Bento
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(100.dp)
                    .testTag("btn_career_prediction")
                    .clickable {
                        viewModel.generateCareerPrediction()
                        onNavigateToReadingDetail()
                    },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight
                ),
                border = BorderStroke(1.dp, (if (isDark) Color(0xFF49454F) else Color.LightGray).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SecondaryNebula.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Career prediction",
                            tint = SecondaryNebula,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Career Paths",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) OnBackgroundDark else OnBackgroundLight
                        )
                        Text(
                            text = "Professional cycles",
                            fontSize = 10.sp,
                            color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 5. Bento Row 5: Numerology & Custom More Card
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Numerology Paths
            Card(
                modifier = Modifier
                    .weight(1.2f)
                    .height(90.dp)
                    .testTag("btn_numerology")
                    .clickable {
                        viewModel.generateNumerologyReport()
                        onNavigateToReadingDetail()
                    },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) SurfaceDark else SurfaceLight
                ),
                border = BorderStroke(1.dp, (if (isDark) Color(0xFF49454F) else Color.LightGray).copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentPurple.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Numbers,
                            contentDescription = "Numerology",
                            tint = AccentPurple,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Numerology",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDark) OnBackgroundDark else OnBackgroundLight
                        )
                        Text(
                            text = "Destiny digits",
                            fontSize = 10.sp,
                            color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // More Space decoration
            Card(
                modifier = Modifier
                    .weight(1f)
                    .height(90.dp),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDark) CardHighlight.copy(alpha = 0.4f) else Color.LightGray.copy(alpha = 0.15f)
                )
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "✨ More coming",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.4f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // --- All Dialog Overlays (Love, Tarot, Palm, and Quick Chat) ---

        // Love Synastry Dialog
        if (showLoveDialog) {
            AlertDialog(
                onDismissRequest = { showLoveDialog = false },
                title = { Text("Love compatibility Synastry", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Check planetary synchronicity with another soul:")
                        OutlinedTextField(
                            value = partnerName,
                            onValueChange = { partnerName = it },
                            label = { Text("Partner Name") },
                            modifier = Modifier.fillMaxWidth().testTag("partner_name_field")
                        )
                        OutlinedTextField(
                            value = partnerDob,
                            onValueChange = { partnerDob = it },
                            label = { Text("Date of Birth (YYYY-MM-DD)") },
                            modifier = Modifier.fillMaxWidth().testTag("partner_dob_field")
                        )
                        OutlinedTextField(
                            value = partnerZodiac,
                            onValueChange = { partnerZodiac = it },
                            label = { Text("Partner Zodiac Sign") },
                            modifier = Modifier.fillMaxWidth().testTag("partner_zodiac_field")
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showLoveDialog = false
                            viewModel.generateLoveCompatibility(partnerName, partnerDob, partnerZodiac)
                            onNavigateToReadingDetail()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryNebula)
                    ) {
                        Text("CALCULATE AFFINITY", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLoveDialog = false }) {
                        Text("CLOSE")
                    }
                }
            )
        }

        // Tarot Spread Dialog
        if (showTarotDialog) {
            val cardsList = listOf(
                "The Fool", "The Magician", "The High Priestess", "The Empress", "The Emperor",
                "The Hierophant", "The Lovers", "The Chariot", "Strength", "The Hermit",
                "Wheel of Fortune", "Justice", "The Hanged Man", "Death", "Temperance",
                "The Devil", "The Tower", "The Star", "The Moon", "The Sun", "Judgement", "The World"
            )

            AlertDialog(
                onDismissRequest = { showTarotDialog = false },
                modifier = Modifier.fillMaxWidth().height(500.dp).testTag("tarot_drawing_dialog"),
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Draw 3 Tarot Cards (${selectedTarotCards.size}/3)", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        IconButton(onClick = { showTarotDialog = false }) {
                            Icon(Icons.Default.Close, "Close")
                        }
                    }
                },
                text = {
                    Column {
                        Text("Tap cards under the cosmic layout to align your energy:", modifier = Modifier.padding(bottom = 12.dp))
                        
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(cardsList) { card ->
                                val isSelected = selectedTarotCards.contains(card)
                                Card(
                                    modifier = Modifier
                                        .height(100.dp)
                                        .clickable {
                                            if (isSelected) {
                                                selectedTarotCards.remove(card)
                                            } else if (selectedTarotCards.size < 3) {
                                                selectedTarotCards.add(card)
                                            }
                                        },
                                    border = BorderStroke(
                                        width = 1.5.dp,
                                        color = if (isSelected) PrimaryGold else SecondaryNebula.copy(alpha = 0.3f)
                                    ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) CardHighlight else SurfaceDark
                                    )
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.padding(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSelected) Icons.Default.AutoAwesome else Icons.Default.FilterList,
                                                contentDescription = "Card layout",
                                                tint = if (isSelected) PrimaryGold else OnBackgroundDark.copy(alpha = 0.3f)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = card,
                                                fontSize = 10.sp,
                                                textAlign = TextAlign.Center,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) PrimaryGold else OnBackgroundDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (selectedTarotCards.size == 3) {
                                showTarotDialog = false
                                viewModel.generateTarotSpread(selectedTarotCards.toList())
                                onNavigateToReadingDetail()
                            }
                        },
                        enabled = selectedTarotCards.size == 3,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGoldVariant)
                    ) {
                        Text("CONSULT TAROT", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            )
        }

        // Palmistry Scanner Dialog
        if (showPalmDialog) {
            AlertDialog(
                onDismissRequest = { showPalmDialog = false },
                modifier = Modifier.fillMaxWidth().testTag("palmistry_scanner_dialog"),
                title = { Text("Celestial Palm Scanner", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
                text = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "To examine your major lines (Heart, Head, Life, Fate), choose a preset hand configuration below:",
                            textAlign = TextAlign.Center,
                            fontSize = 13.sp
                        )

                        // Outer Glowing Scan Circle
                        Box(
                            modifier = Modifier
                                .size(140.dp)
                                .clip(CircleShape)
                                .background(CardHighlight)
                                .border(2.dp, TertiaryTeal, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FrontHand,
                                contentDescription = "Simulated palm image outline",
                                tint = TertiaryTeal,
                                modifier = Modifier.size(72.dp)
                              )
                        }

                        // Presets Grid
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Fast Presets Options:", fontSize = 11.sp, color = SecondaryNebula)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PresetPalmButton("The Heart Line Map", modifier = Modifier.weight(1f)) {
                                    showPalmDialog = false
                                    viewModel.generatePalmReading(MOCK_PALM_BASE64_HEART)
                                    onNavigateToReadingDetail()
                                }
                                PresetPalmButton("Jupiter Mount Map", modifier = Modifier.weight(1f)) {
                                    showPalmDialog = false
                                    viewModel.generatePalmReading(MOCK_PALM_BASE64_JUPITER)
                                    onNavigateToReadingDetail()
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPalmDialog = false }) {
                        Text("CANCEL")
                    }
                }
            )
        }

        // Gemini Quick Astro-Chat Dialogue Popup
        if (showQuickChatDialog) {
            AlertDialog(
                onDismissRequest = { showQuickChatDialog = false },
                title = { Text("Channel Cosmic Guidance", fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Enter any prompt regarding your destiny, relationship, or planetary transits:")
                        OutlinedTextField(
                            value = quickChatPrompt,
                            onValueChange = { quickChatPrompt = it },
                            placeholder = { Text("What planetary alignment governs my professional growth?") },
                            modifier = Modifier.fillMaxWidth().testTag("quick_chat_prompt_field"),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SecondaryNebula)
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (quickChatPrompt.isNotBlank()) {
                                showQuickChatDialog = false
                                val currentText = quickChatPrompt.trim()
                                quickChatPrompt = ""
                                // Submit immediately. When the reading is successful, navigate to see it.
                                viewModel.sendChatMessage(currentText)
                                // We also trigger a generic horoscope query based on the text
                                viewModel.generateCustomPromptHoroscope(currentText)
                                onNavigateToReadingDetail()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryNebula)
                    ) {
                        Text("CHANNEL ORACLE", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showQuickChatDialog = false }) {
                        Text("CLOSE")
                    }
                }
            )
        }
    }
}

@Composable
fun PresetPalmButton(
    title: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        border = BorderStroke(1.dp, TertiaryTeal.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.InsertPhoto, "Palm icon icon", tint = TertiaryTeal, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(2.dp))
            Text(title, fontSize = 10.sp, color = OnBackgroundDark)
        }
    }
}

// 1x1 transparent mock pixel standard base64 strings to pass inside multimodal Gemini checks safely
const val MOCK_PALM_BASE64_HEART = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA="
const val MOCK_PALM_BASE64_JUPITER = "/9j/4AAQSkZJRgABAQEASABIAAD/2wBDAP//////////////////////////////////////////////////////////////////////////////////////wgALCAABAAEBAREA/8QAFBABAAAAAAAAAAAAAAAAAAAAAP/aAAgBAQABPxA="
