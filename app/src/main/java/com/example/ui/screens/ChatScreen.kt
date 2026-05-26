package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessageEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.AstrologyViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(
    viewModel: AstrologyViewModel
) {
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()

    var inputMessageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when new message arrives
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isDark) BackgroundDark else BackgroundLight)
            .testTag("chat_screen_root")
    ) {
        // --- Chat Header info ---
        Card(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            colors = CardDefaults.cardColors(containerColor = if (isDark) SurfaceDark else SurfaceLight),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(SecondaryNebula.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Cosmic icon",
                            tint = SecondaryNebula,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("AstroAI cosmic Guide", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Online • Channeling insights", fontSize = 11.sp, color = TertiaryTeal)
                    }
                }

                IconButton(
                    onClick = { viewModel.clearChatHistory() },
                    modifier = Modifier.testTag("btn_clear_chat")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear chat logs",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // --- Suggestion starters row ---
        if (chatMessages.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🌌 BEGIN A SACRED INQUIRY",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = SecondaryNebula,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    SuggestionChipButton("Tell me about my destiny path") {
                        inputMessageText = it
                    }
                    SuggestionChipButton("Will my career improve this year?") {
                        inputMessageText = it
                    }
                    SuggestionChipButton("What remedies clear my Sade Sati?") {
                        inputMessageText = it
                    }
                }
            }
        } else {
            // --- Message Scroll Feed list ---
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { message ->
                    ChatMessageBubble(message = message, isDark = isDark)
                }
            }
        }

        // --- Send bar footer ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = 6.dp,
            color = if (isDark) SurfaceDark else SurfaceLight
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputMessageText,
                    onValueChange = { inputMessageText = it },
                    placeholder = { Text("Ask AstroAI Guide anything...", fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_textfield"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SecondaryNebula
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FloatingActionButton(
                    onClick = {
                        if (inputMessageText.isNotBlank()) {
                            val textToSend = inputMessageText.trim()
                            inputMessageText = ""
                            viewModel.sendChatMessage(textToSend)
                        }
                    },
                    modifier = Modifier.size(50.dp).testTag("chat_send_button"),
                    containerColor = SecondaryNebula,
                    contentColor = OnPrimaryDark,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send prompt button",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SuggestionChipButton(
    text: String,
    onClick: (String) -> Unit
) {
    AssistChip(
        onClick = { onClick(text) },
        label = { Text(text, fontSize = 12.sp, fontWeight = FontWeight.Medium) },
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
fun ChatMessageBubble(
    message: ChatMessageEntity,
    isDark: Boolean
) {
    val isUser = message.isUser
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) {
        if (isDark) SecondaryNebula else SecondaryNebula.copy(alpha = 0.2f)
    } else {
        if (isDark) SurfaceDark else SurfaceLight
    }
    val contentColor = if (isUser) {
        if (isDark) OnPrimaryDark else OnBackgroundLight
    } else {
        if (isDark) OnBackgroundDark else OnBackgroundLight
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(containerColor)
                .padding(12.dp)
        ) {
            Text(
                text = message.text,
                fontSize = 14.sp,
                color = contentColor,
                lineHeight = 20.sp
            )
        }
        Text(
            text = if (isUser) "You" else "AstroAI",
            fontSize = 10.sp,
            color = (if (isDark) OnBackgroundDark else OnBackgroundLight).copy(alpha = 0.4f),
            modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp)
        )
    }
}
