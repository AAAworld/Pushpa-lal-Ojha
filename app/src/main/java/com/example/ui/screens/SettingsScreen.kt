package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AstrologyViewModel

@Composable
fun SettingsScreen(
    viewModel: AstrologyViewModel,
    onLogoutPressed: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val selectedLang by viewModel.selectedLanguage.collectAsState()
    val notificationsEnabled by viewModel.pushNotificationsEnabled.collectAsState()
    val notificationsLog by viewModel.notificationsLog.collectAsState()
    val context = LocalContext.current

    val user = currentUser ?: return

    var expandedLangDropdown by remember { mutableStateOf(false) }
    var expandedZodiacDropdown by remember { mutableStateOf(false) }

    val languages = listOf(
        "en" to "English (Channeling)",
        "hi" to "Hindi (हिन्दी)",
        "es" to "Spanish (Español)",
        "fr" to "French (Français)",
        "de" to "German (Deutsch)"
    )

    val zodiacs = listOf(
        "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
        "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
            .testTag("settings_screen_root")
    ) {
        Text(
            text = "CELESTIAL PREFERENCES",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            letterSpacing = 1.sp,
            color = if (isDark) OnBackgroundDark.copy(alpha = 0.5f) else OnBackgroundLight.copy(alpha = 0.5f),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Count 1: Profile Alignment Overrides
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else SurfaceLight),
            border = BorderStroke(1.dp, SecondaryNebula.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Soul Profile Configuration", fontWeight = FontWeight.Bold, color = PrimaryGold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(14.dp))

                Text("Your Email Address: ${user.email}", fontSize = 12.sp, color = OnBackgroundDark.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                // Zodiac Dropdown override
                Box {
                    Button(
                        onClick = { expandedZodiacDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("btn_change_zodiac_dropdown"),
                        colors = ButtonDefaults.buttonColors(containerColor = CardHighlight)
                    ) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Current Zodiac Sign: ${user.zodiac}", fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, "expand drop")
                        }
                    }

                    DropdownMenu(
                        expanded = expandedZodiacDropdown,
                        onDismissRequest = { expandedZodiacDropdown = false }
                    ) {
                        zodiacs.forEach { z ->
                            DropdownMenuItem(
                                text = { Text(z) },
                                onClick = {
                                    expandedZodiacDropdown = false
                                    viewModel.updateZodiac(z)
                                    Toast.makeText(context, "Planetary birth chart shifted to $z!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Count 2: Language Configurations (Multilingual support)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else SurfaceLight),
            border = BorderStroke(1.dp, SecondaryNebula.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Wisdom Language (Speech / REST)", fontWeight = FontWeight.Bold, color = SecondaryNebula, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(10.dp))

                Box {
                    Button(
                        onClick = { expandedLangDropdown = true },
                        modifier = Modifier.fillMaxWidth().testTag("btn_change_language"),
                        colors = ButtonDefaults.buttonColors(containerColor = CardHighlight)
                    ) {
                        val currentLangName = languages.firstOrNull { it.first == selectedLang }?.second ?: "English"
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Language: $currentLangName", fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowDropDown, "expand dropdown")
                        }
                    }

                    DropdownMenu(
                        expanded = expandedLangDropdown,
                        onDismissRequest = { expandedLangDropdown = false }
                    ) {
                        languages.forEach { pair ->
                            DropdownMenuItem(
                                text = { Text(pair.second) },
                                onClick = {
                                    expandedLangDropdown = false
                                    viewModel.updateLanguage(pair.first)
                                    Toast.makeText(context, "Spiritual channels tuned to ${pair.second}!", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    }
                }
            }
        }

        // Count 3: Cosmic Settings toggles (DarkTheme, Notifications)
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else SurfaceLight),
            border = BorderStroke(1.dp, SecondaryNebula.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Stellar App Toggles", fontWeight = FontWeight.Bold, color = TertiaryTeal, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                // Toggle Dark Theme
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Deep Space Dark Theme", fontSize = 13.sp)
                    Switch(
                        checked = isDark,
                        onCheckedChange = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("switch_dark_mode")
                    )
                }

                Divider(color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.10f), modifier = Modifier.padding(vertical = 10.dp))

                // Toggle Push Notifications Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Receive Planetary Transit Alerts", fontSize = 13.sp)
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { viewModel.togglePushNotifications() },
                        modifier = Modifier.testTag("switch_push_notifications")
                    )
                }

                // If triggers active, show custom alert notifications history
                AnimatedVisibility(visible = notificationsEnabled) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .background(CardHighlight, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text("Active Alert Channels Simulated Feed logs:", fontSize = 10.sp, color = SecondaryNebula, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        notificationsLog.forEach { log ->
                            Text("• $log", fontSize = 11.sp, lineHeight = 14.sp, color = OnBackgroundDark)
                            Spacer(modifier = Modifier.height(2.dp))
                        }
                    }
                }
            }
        }

        // Count 4: Membership pricing plans
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else SurfaceLight),
            border = BorderStroke(1.5.dp, PrimaryGold.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, "Premium crown", tint = PrimaryGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Membership Plans (Acquire premium status)", fontWeight = FontWeight.Bold, color = PrimaryGold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Unlock elite, high-vibrational features, unlimited real-time chat consultation length, palm scans, and zero-gravity readings downloads.",
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Packages Comparison Row
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PremiumPackageBox(
                        title = "Ecliptic Seeker Pack",
                        price = "${'$'}3.99 / Month",
                        benefit = "Provides complete Daily & Astro compatibility access",
                        isActive = user.isPremium,
                        onClick = {
                            viewModel.adminToggleUserPremium(user.copy(isPremium = true))
                            Toast.makeText(context, "Cosmic karma unlocked! Enjoy your Premium membership.", Toast.LENGTH_SHORT).show()
                        }
                    )

                    PremiumPackageBox(
                        title = "Guru Omnipotent Pack",
                        price = "${'$'}9.99 / Month",
                        benefit = "Full database logs download, palm scans, and priority response",
                        isActive = user.isPremium,
                        onClick = {
                            viewModel.adminToggleUserPremium(user.copy(isPremium = true))
                            Toast.makeText(context, "Divine energies unleashed! Ultimate membership active.", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Logout Trigger Action Button
        Button(
            onClick = { viewModel.logout(onLogoutPressed) },
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_logout")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Logout, "Exit door")
                Spacer(modifier = Modifier.width(6.dp))
                Text("SECURELY DISCONNECT ACCOUNT", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun PremiumPackageBox(
    title: String,
    price: String,
    benefit: String,
    isActive: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { if (!isActive) onClick() },
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = CardHighlight),
        border = BorderStroke(1.dp, if (isActive) TertiaryTeal else SecondaryNebula.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnBackgroundDark)
                Text(benefit, fontSize = 10.sp, color = OnBackgroundDark.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(2.dp))
                Text(price, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp, color = PrimaryGold)
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isActive) TertiaryTeal.copy(alpha = 0.2f) else SecondaryNebula.copy(alpha = 0.2f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (isActive) "ACTIVE SOUL" else "UPGRADE",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) TertiaryTeal else PrimaryGold
                )
            }
        }
    }
}
