package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ChatMessage
import com.example.ui.DecorViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: DecorViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.chatMessages.collectAsState()
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var typedQuery by remember { mutableStateOf("") }

    val starterPrompts = listOf(
        "Tư vấn phối phòng khách khoảng 20m² Bắc Âu",
        "Có nên mua Bàn ăn Óc Chó nguyên khối?",
        "Ưu điểm của giường nhung Velvet Cozy",
        "Ghế Ergonomic Elite đỡ cột sống thế nào?"
    )

    // Automatically scrolls down to the latest response whenever the message log shifts
    LaunchedEffect(messages.size, viewModel.isAiLoading) {
        if (messages.isNotEmpty()) {
            try {
                listState.animateScrollToItem(messages.size - 1)
            } catch (e: Exception) {
                // Ignore gracefully on cancellation
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color(0xFFEA580C).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SupportAgent,
                                contentDescription = "AI Agent",
                                tint = Color(0xFFEA580C)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                "TƯ VẤN QUY HOẠCH AI",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                if (viewModel.isAiLoading) "Trợ lý ảo đang nghĩ..." else "Thời gian tư vấn trực tuyến: 24/7",
                                fontSize = 11.sp,
                                color = if (viewModel.isAiLoading) Color(0xFFEA580C) else Color.Gray,
                                fontWeight = if (viewModel.isAiLoading) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.wipeConversationThreads() }) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Làm mới",
                            tint = Color.Gray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main conversation log column
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        }
                    }
                } else {
                    items(messages) { message ->
                        ChatMessageBubble(message = message)
                    }
                }

                if (viewModel.isAiLoading) {
                    item {
                        AiThinkingBubble()
                    }
                }
            }

            // Quick Starter suggestions (only shown if messages are minimal or when user is waiting)
            AnimatedVisibility(visible = messages.size <= 2 && !viewModel.isAiLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Gợi ý câu hỏi:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        starterPrompts.take(2).forEach { prompt ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                                    .border(0.5.dp, Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                                    .clickable { viewModel.sendUserMessage(prompt) }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = prompt,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 2,
                                    lineHeight = 14.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Text Entry row
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = typedQuery,
                        onValueChange = { typedQuery = it },
                        placeholder = { Text("Hỏi mẹ tẹo về mẫu thiết kế...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 52.dp, max = 120.dp),
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        maxLines = 4,
                        singleLine = false
                    )

                    IconButton(
                        onClick = {
                            if (typedQuery.isNotBlank() && !viewModel.isAiLoading) {
                                viewModel.sendUserMessage(typedQuery)
                                typedQuery = ""
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                if (typedQuery.isNotBlank()) Color(0xFFEA580C)
                                else Color.LightGray.copy(alpha = 0.4f)
                            ),
                        colors = IconButtonDefaults.iconButtonColors(contentColor = Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessage) {
    val isUser = message.sender == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val bgBrush = if (isUser) {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFEA580C), // Orange spice
                Color(0xFFC2410C)
            )
        )
    } else {
        Brush.linearGradient(
            colors = listOf(
                Color(0xFFF1F5F9), // Very light cool gray
                Color(0xFFE2E8F0)
            )
        )
    }

    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(shape)
                .background(bgBrush)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            MarkdownText(
                rawText = message.content,
                textColor = if (isUser) Color.White else Color(0xFF0F172A)
            )
        }
        
        // Minor time metrics
        Text(
            text = if (isUser) "Bạn" else "Kiến Trúc Sư AI",
            fontSize = 9.sp,
            color = Color.Gray,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

/**
 * A simple lightweight parser that styles basic bold segments like **this** and bullet structures.
 */
@Composable
fun MarkdownText(
    rawText: String,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val paragraphs = rawText.split("\n")
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        paragraphs.forEach { textLine ->
            if (textLine.trim().startsWith("-") || textLine.trim().startsWith("*")) {
                // Bullet points
                val bulletContent = textLine.replaceFirst("-", "").replaceFirst("*", "").trim()
                Row {
                    Text("•", color = textColor, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    TextWithBoldFormatting(text = bulletContent, textColor = textColor)
                }
            } else {
                TextWithBoldFormatting(text = textLine, textColor = textColor)
            }
        }
    }
}

@Composable
fun TextWithBoldFormatting(text: String, textColor: Color) {
    if (text.contains("**")) {
        val parts = text.split("**")
        androidx.compose.material3.Text(
            text = androidx.compose.ui.text.buildAnnotatedString {
                parts.forEachIndexed { index, part ->
                    if (index % 2 == 1) {
                        pushStyle(androidx.compose.ui.text.SpanStyle(fontWeight = FontWeight.Bold))
                        append(part)
                        pop()
                    } else {
                        append(part)
                    }
                }
            },
            color = textColor,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    } else {
        Text(
            text = text,
            color = textColor,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
fun AiThinkingBubble() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFEA580C).copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SupportAgent,
                contentDescription = "AI thinking",
                tint = Color(0xFFEA580C),
                modifier = Modifier.size(20.dp)
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 220.dp)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .background(Color(0xFFF1F5F9))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CircularProgressIndicator(
                    color = Color(0xFFEA580C),
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Đang phác thảo thiết kế...",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
