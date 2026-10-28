package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AIMessage
import com.example.ui.components.KiteShapeIcon
import com.example.ui.theme.MonoBlack
import com.example.ui.theme.MonoBorder
import com.example.ui.theme.MonoBorderBright
import com.example.ui.theme.MonoGrey
import com.example.ui.theme.MonoLightGrey
import com.example.ui.theme.MonoSurfaceCard
import com.example.ui.theme.MonoSurfaceElevated
import com.example.ui.theme.MonoWhite

@Composable
fun AIScreen(
    messages: List<AIMessage>,
    onSendMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val quickQuestions = listOf(
        "When can I skip classes?",
        "Can I skip classes today?",
        "Which course has my best bunk buffer?",
        "Borderline courses check",
        "How to reach 80% attendance?"
    )

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MonoBlack)
            .testTag("ai_screen")
    ) {
        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KiteShapeIcon(
                    size = 22.dp,
                    tint = MonoWhite
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ASK TALLY",
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp,
                        color = MonoWhite
                    )
                )
            }
            Text(
                text = "Ask when you can skip classes, bunk buffers & attendance math",
                style = MaterialTheme.typography.bodySmall.copy(color = MonoGrey)
            )
        }

        // Quick Suggestion Chips
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quickQuestions) { question ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MonoSurfaceCard)
                        .border(1.dp, MonoBorder, RoundedCornerShape(12.dp))
                        .clickable { onSendMessage(question) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .testTag("quick_question_${question.take(10)}")
                ) {
                    Text(
                        text = question,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            color = MonoLightGrey
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Message List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                MessageBubble(message = message)
            }
        }

        // Symmetrical typing bar placed down above the bottom nav bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 12.dp)
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = {
                    Text(
                        "Ask when you can skip classes...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MonoGrey)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_input_field"),
                shape = RoundedCornerShape(26.dp),
                trailingIcon = {
                    Box(
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank()) MonoWhite else MonoSurfaceElevated)
                            .clickable(enabled = inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                            .testTag("ai_send_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) MonoBlack else MonoGrey,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MonoWhite,
                    unfocusedBorderColor = MonoBorderBright,
                    focusedTextColor = MonoWhite,
                    unfocusedTextColor = MonoWhite,
                    focusedContainerColor = MonoSurfaceCard,
                    unfocusedContainerColor = MonoSurfaceCard
                ),
                maxLines = 3
            )
        }
    }
}

@Composable
fun MessageBubble(message: AIMessage) {
    val isUser = message.isUser

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(if (isUser) 0.8f else 0.95f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) MonoSurfaceElevated else MonoSurfaceCard)
                .border(
                    width = 1.dp,
                    color = if (isUser) MonoBorderBright else MonoBorder,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Column {
                if (!isUser) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        KiteShapeIcon(
                            size = 14.dp,
                            tint = MonoWhite
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ask Tally",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MonoWhite
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MonoWhite,
                        lineHeight = 20.sp
                    )
                )
            }
        }
    }
}
