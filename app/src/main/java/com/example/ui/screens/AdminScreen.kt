package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
fun AdminScreen(
    viewModel: AstrologyViewModel
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val allUsers by viewModel.allUsers.collectAsState()
    val allReadings by viewModel.allGlobalReadings.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    var customNotificationText by remember { mutableStateOf("") }

    val user = currentUser ?: return
    val isAdmin = user.role == "ADMIN"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
            .padding(16.dp)
            .testTag("admin_screen_root")
    ) {
        if (!isAdmin) {
            // Un-elevated user barrier shield with access bypass
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shield barrier",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "ADMIN SHIELD ACTIVE",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.error
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "You are logged in as a standard Cosmic seeker. Only authenticated spiritual administrators can enter the control sanctuary.",
                    textAlign = TextAlign.Center,
                    color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.7f),
                    modifier = Modifier.padding(horizontal = 20.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        // Promoting current user to ADMIN for prototyping test convenience
                        viewModel.adminToggleUserPremium(user.copy(role = "ADMIN"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGold),
                    modifier = Modifier.testTag("btn_bypass_admin")
                ) {
                    Text("ASCEND AS TEMPLE ADMIN (PROTOTYPE)", color = OnPrimaryDark, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // Admin sanctuary console
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Title
                item {
                    Text(
                        text = "TEMPLE CENTRAL CONSOLE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        letterSpacing = 1.sp,
                        color = if (isDark) OnBackgroundDark.copy(alpha = 0.5f) else OnBackgroundLight.copy(alpha = 0.5f)
                    )
                }

                // 1. Diagnostics Panel
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else SurfaceLight),
                        border = BorderStroke(1.dp, SecondaryNebula.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Diagnostics Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryGold)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                DiagnosticStatBox("Constellations", "${allUsers.size} souls", modifier = Modifier.weight(1.2f))
                                DiagnosticStatBox("Readings", "${allReadings.size} cast", modifier = Modifier.weight(1f))
                                DiagnosticStatBox("Status", "Balanced", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                // 2. Mock Push Notification simulated triggers
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else SurfaceLight),
                        border = BorderStroke(1.dp, SecondaryNebula.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Trigger simulated Push Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = SecondaryNebula)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = customNotificationText,
                                onValueChange = { customNotificationText = it },
                                placeholder = { Text("E.g., Solar Eclipse is commencing. Check Horoscope.") },
                                modifier = Modifier.fillMaxWidth().testTag("admin_notification_input"),
                                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = SecondaryNebula)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    if (customNotificationText.isNotBlank()) {
                                        viewModel.simulateAlertTrigger(customNotificationText)
                                        customNotificationText = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().testTag("btn_trigger_notification"),
                                colors = ButtonDefaults.buttonColors(containerColor = SecondaryNebula)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NotificationsActive, "Alarm bell")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("TRIGGER COSMIC CHANNELS", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 3. Registered Users table directory list
                item {
                    Text(
                        text = "MANAGE SEEKERS ACCOUNT (${allUsers.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (isDark) OnBackgroundDark.copy(alpha = 0.5f) else OnBackgroundLight.copy(alpha = 0.5f),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(allUsers) { userModel ->
                    SeekerAdminRow(
                        seeker = userModel,
                        isDark = isDark,
                        onTogglePremium = {
                            viewModel.adminToggleUserPremium(userModel)
                        },
                        onDeleteUser = {
                            viewModel.adminDeleteUser(userModel)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
fun DiagnosticStatBox(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(title, fontSize = 10.sp, color = OnBackgroundDark.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = TertiaryTeal)
    }
}

@Composable
fun SeekerAdminRow(
    seeker: UserEntity,
    isDark: Boolean,
    onTogglePremium: () -> Unit,
    onDeleteUser: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().testTag("seeker_admin_row_${seeker.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isDark) CardHighlight else SurfaceLight),
        border = BorderStroke(1.dp, (if (isDark) SecondaryNebula else PrimaryGoldVariant).copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = seeker.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isDark) OnBackgroundDark else OnBackgroundLight
                    )
                    Text(
                        text = "${seeker.email} • Role: ${seeker.role}",
                        fontSize = 11.sp,
                        color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.5f)
                    )
                }

                IconButton(onClick = onDeleteUser) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Expel seeker",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(if (seeker.isPremium) TertiaryTeal else Color.LightGray)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (seeker.isPremium) "Premium Member" else "Free Account",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (seeker.isPremium) TertiaryTeal else (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.7f)
                    )
                }

                Button(
                    onClick = onTogglePremium,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (seeker.isPremium) Color.Gray else PrimaryGold
                    )
                ) {
                    Text(
                        text = if (seeker.isPremium) "Downgrade Standard" else "Grant premium",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (seeker.isPremium) Color.White else OnPrimaryDark
                    )
                }
            }
        }
    }
}
