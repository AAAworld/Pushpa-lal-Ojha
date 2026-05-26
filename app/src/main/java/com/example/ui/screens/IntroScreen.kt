package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AstrologyViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun IntroScreen(
    viewModel: AstrologyViewModel,
    onLoginSuccess: () -> Unit
) {
    val authError by viewModel.authError.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    var isSignUpMode by remember { mutableStateOf(false) }

    // Onboarding form state variables
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("1998-05-15") } // YYYY-MM-DD template
    var tob by remember { mutableStateOf("08:30") }       // HH:MM template
    var pob by remember { mutableStateOf("London, UK") }
    var hasPasswordVisible by remember { mutableStateOf(false) }

    // Automatic Zodiac Calculator
    val calculatedZodiac = remember(dob) {
        calculateZodiacSign(dob)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize().testTag("intro_screen_container")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            colors = listOf(BackgroundDark, Color(0xFF130730), Color(0xFF070211))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(BackgroundLight, Color(0xFFFDEFD5))
                        )
                    }
                )
        ) {
            // Nebula Background highlights
            if (isDark) {
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(SecondaryNebula.copy(alpha = 0.15f), Color.Transparent)
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(320.dp)
                        .align(Alignment.BottomStart)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(TertiaryTeal.copy(alpha = 0.12f), Color.Transparent)
                            )
                        )
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(20.dp))

                // App Branding Icon Symbol
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(
                            if (isDark) SurfaceDark else Color(0x1FBC85FF)
                        )
                        .testTag("app_logo_badge"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Brightness5,
                        contentDescription = "Cosmic Sun Logo",
                        tint = if (isDark) PrimaryGold else PrimaryGoldVariant,
                        modifier = Modifier.size(54.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "CosmicAI",
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) PrimaryGold else OnBackgroundLight
                    )
                )

                Text(
                    text = "Your personalized AI astrology, tarot, and karmic reading companion.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = if (isDark) OnBackgroundDark.copy(alpha = 0.7f) else OnBackgroundLight.copy(alpha = 0.8f)
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("auth_form_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) SurfaceDark.copy(alpha = 0.9f) else SurfaceLight
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(
                        width = 1.dp,
                        color = (if (isDark) SecondaryNebula else PrimaryGoldVariant).copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(22.dp)
                    ) {
                        // Title selector tab
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            TextButton(
                                onClick = { isSignUpMode = false },
                                modifier = Modifier.weight(1f).testTag("select_login_tab")
                            ) {
                                Text(
                                    "SIGN IN",
                                    fontWeight = if (!isSignUpMode) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 16.sp,
                                    color = if (!isSignUpMode) (if (isDark) PrimaryGold else PrimaryGoldVariant) else OnBackgroundDark.copy(alpha = 0.4f)
                                )
                            }
                            TextButton(
                                onClick = { isSignUpMode = true },
                                modifier = Modifier.weight(1f).testTag("select_signup_tab")
                            ) {
                                Text(
                                    "ONBOARD",
                                    fontWeight = if (isSignUpMode) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 16.sp,
                                    color = if (isSignUpMode) (if (isDark) PrimaryGold else PrimaryGoldVariant) else OnBackgroundDark.copy(alpha = 0.4f)
                                )
                            }
                        }

                        Divider(
                            color = (if (isDark) SecondaryNebula else PrimaryGoldVariant).copy(alpha = 0.15f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )

                        if (authError != null) {
                            Text(
                                text = authError!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .background(CardHighlight.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                    .testTag("auth_error_text")
                            )
                        }

                        // Email Field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Celestial Email (Account)") },
                            leadingIcon = { Icon(Icons.Default.Email, "Email Icon", tint = if (isDark) SecondaryNebula else PrimaryGoldVariant) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("email_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isDark) PrimaryGold else PrimaryGoldVariant,
                                unfocusedBorderColor = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.2f)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Password Field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Spiritual Password") },
                            leadingIcon = { Icon(Icons.Default.Lock, "Lock Icon", tint = if (isDark) SecondaryNebula else PrimaryGoldVariant) },
                            trailingIcon = {
                                IconButton(onClick = { hasPasswordVisible = !hasPasswordVisible }) {
                                    Icon(
                                        imageVector = if (hasPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "Password eye modifier"
                                    )
                                }
                            },
                            visualTransformation = if (hasPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("password_input_field"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (isDark) PrimaryGold else PrimaryGoldVariant,
                                unfocusedBorderColor = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.2f)
                            )
                        )

                        // Extended Profile Wizards fields for SignUp MODE
                        AnimatedVisibility(
                            visible = isSignUpMode,
                            enter = fadeIn(animationSpec = tween(300)) + expandVertically(),
                            exit = fadeOut(animationSpec = tween(200)) + shrinkVertically()
                        ) {
                            Column {
                                Spacer(modifier = Modifier.height(12.dp))

                                // Spiritual Name
                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Spiritual Name") },
                                    leadingIcon = { Icon(Icons.Default.Person, "Name Icon", tint = if (isDark) SecondaryNebula else PrimaryGoldVariant) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("name_input_field"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isDark) PrimaryGold else PrimaryGoldVariant
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Birth Date
                                OutlinedTextField(
                                    value = dob,
                                    onValueChange = { dob = it },
                                    label = { Text("Date of Birth (YYYY-MM-DD)") },
                                    leadingIcon = { Icon(Icons.Default.CalendarToday, "Birth Date Icon", tint = if (isDark) SecondaryNebula else PrimaryGoldVariant) },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth().testTag("dob_input_field"),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = if (isDark) PrimaryGold else PrimaryGoldVariant
                                    )
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Birth Time and Birth Place row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = tob,
                                        onValueChange = { tob = it },
                                        label = { Text("Time (HH:MM)") },
                                        leadingIcon = { Icon(Icons.Default.Schedule, "Time Icon", tint = if (isDark) SecondaryNebula else PrimaryGoldVariant) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.1f).testTag("tob_input_field"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = if (isDark) PrimaryGold else PrimaryGoldVariant
                                        )
                                    )

                                    OutlinedTextField(
                                        value = pob,
                                        onValueChange = { pob = it },
                                        label = { Text("Place of Birth") },
                                        leadingIcon = { Icon(Icons.Default.Place, "Place Icon", tint = if (isDark) SecondaryNebula else PrimaryGoldVariant) },
                                        singleLine = true,
                                        modifier = Modifier.weight(1.3f).testTag("pob_input_field"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = if (isDark) PrimaryGold else PrimaryGoldVariant
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Calculated Zodiac preview
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (isDark) CardHighlight else Color(0x1FBC85FF),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = "Magic Star Sign",
                                            tint = if (isDark) PrimaryGold else PrimaryGoldVariant,
                                            modifier = Modifier.size(20.dp).padding(end = 4.dp)
                                        )
                                        Text(
                                            text = "Estimated Zodiac: $calculatedZodiac",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = if (isDark) OnBackgroundDark else OnBackgroundLight
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Trigger Login / SignUp Button
                        Button(
                            onClick = {
                                if (isSignUpMode) {
                                    viewModel.signUp(
                                        email = email,
                                        passwordRaw = password,
                                        name = name,
                                        dob = dob,
                                        tob = tob,
                                        pob = pob,
                                        zodiac = calculatedZodiac,
                                        onSuccess = onLoginSuccess
                                    )
                                } else {
                                    viewModel.login(
                                        email = email,
                                        passwordRaw = password,
                                        onSuccess = onLoginSuccess
                                    )
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("auth_submit_button"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isDark) PrimaryGold else PrimaryGoldVariant,
                                contentColor = if (isDark) OnPrimaryDark else OnPrimaryLight
                            )
                        ) {
                            Text(
                                text = if (isSignUpMode) "ACTIVATE ENERGY PROFILE" else "ENTER SPIRITUAL SANCTUARY",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Toggle Dark/Light Mode Theme preview
                OutlinedIconButton(
                    onClick = { viewModel.toggleDarkMode() },
                    modifier = Modifier.testTag("auth_theme_toggle_btn"),
                    border = BorderStroke(1.dp, (if (isDark) PrimaryGold else PrimaryGoldVariant).copy(alpha = 0.5f))
                ) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.WbSunny else Icons.Default.NightsStay,
                        contentDescription = "Theme selection toggler",
                        tint = if (isDark) PrimaryGold else PrimaryGoldVariant
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

// Helper Zodiac automatic predictor from Date string YYYY-MM-DD
fun calculateZodiacSign(dobString: String): String {
    try {
        val parts = dobString.split("-")
        if (parts.size >= 3) {
            val month = parts[1].toIntOrNull() ?: 1
            val day = parts[2].toIntOrNull() ?: 1

            return when (month) {
                1 -> if (day < 20) "Capricorn" else "Aquarius"
                2 -> if (day < 19) "Aquarius" else "Pisces"
                3 -> if (day < 21) "Pisces" else "Aries"
                4 -> if (day < 20) "Aries" else "Taurus"
                5 -> if (day < 21) "Taurus" else "Gemini"
                6 -> if (day < 21) "Gemini" else "Cancer"
                7 -> if (day < 23) "Cancer" else "Leo"
                8 -> if (day < 23) "Leo" else "Virgo"
                9 -> if (day < 23) "Virgo" else "Libra"
                10 -> if (day < 23) "Libra" else "Scorpio"
                11 -> if (day < 22) "Scorpio" else "Sagittarius"
                12 -> if (day < 22) "Sagittarius" else "Capricorn"
                else -> "Aries"
            }
        }
    } catch (e: Exception) {
        // Fallback
    }
    return "Aries"
}
