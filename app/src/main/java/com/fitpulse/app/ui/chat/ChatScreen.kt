package com.fitpulse.app.ui.chat

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.fitpulse.app.data.local.AppDatabase
import com.fitpulse.app.ui.components.DarkInputField
import com.fitpulse.app.ui.viewmodels.ChatViewModel
import com.fitpulse.app.ui.viewmodels.ChatViewModelFactory
import com.fitpulse.app.data.local.entities.MessageEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(navController: NavController, peerId: String = "", peerRole: String = "expert") {
    val ctx = LocalContext.current
    val database = remember { AppDatabase.getInstance(ctx) }
    
    val myId = "current_user" 
    val factory = remember(peerId) { ChatViewModelFactory(database.messageDao(), myId, peerId) }
    val viewModel: ChatViewModel = viewModel(factory = factory)
    
    val chatHistory by viewModel.chatHistory.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        if (uiState is com.fitpulse.app.ui.viewmodels.ChatUiState.Error) {
            snackbarHostState.showSnackbar((uiState as com.fitpulse.app.ui.viewmodels.ChatUiState.Error).message)
        }
    }
    
    ChatContent(
        peerId = peerId,
        peerRole = peerRole,
        chatHistory = chatHistory,
        messageText = messageText,
        onMessageChange = { messageText = it },
        onSendClick = {
            if (messageText.isNotBlank()) {
                viewModel.sendMessage(messageText)
                messageText = ""
            }
        },
        onBackClick = { navController.popBackStack() },
        snackbarHostState = snackbarHostState,
        myId = myId
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatContent(
    peerId: String,
    peerRole: String,
    chatHistory: List<MessageEntity>,
    messageText: String,
    onMessageChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onBackClick: () -> Unit,
    snackbarHostState: SnackbarHostState,
    myId: String
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(peerId, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onBackground)
                        Text(peerRole.replaceFirstChar { if (it.isLowerCase()) it.titlecase(java.util.Locale.getDefault()) else it.toString() }, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onBackground)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            Column {
                // AI Shield Warning Banner (SRS 2.7)
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("AI Shield™ Protected: Personal contact and off-platform payments are restricted.", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 8.dp) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            DarkInputField(
                                value = messageText,
                                onValueChange = onMessageChange,
                                label = "Type a message...",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        IconButton(
                            onClick = onSendClick,
                            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(50)).background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
            reverseLayout = true,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }
            items(chatHistory) { msg ->
                val isMe = msg.senderId == myId
                ChatBubble(
                    content = msg.content, 
                    isMe = isMe, 
                    status = msg.status, 
                    blockedReason = msg.blockedReason
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
fun ChatBubble(content: String, isMe: Boolean, status: String, blockedReason: String?) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
            if (status == "AI_BLOCKED") {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(blockedReason ?: "Message Blocked", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
                Spacer(Modifier.height(4.dp))
            }

            Surface(
                color = when {
                    status == "AI_BLOCKED" -> MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                    isMe -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.surfaceVariant
                },
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isMe) 16.dp else 4.dp,
                    bottomEnd = if (isMe) 4.dp else 16.dp
                ),
                border = if(status == "AI_BLOCKED") androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error) else null
            ) {
                Text(
                    text = if (status == "AI_BLOCKED") "[REDACTED BY AI SHIELD]" else content,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        status == "AI_BLOCKED" -> MaterialTheme.colorScheme.error
                        isMe -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun ChatScreenPreview() {
    val mockHistory = listOf(
        MessageEntity(senderId = "other", receiverId = "me", content = "Hey! How is your diet going?", timestamp = 0),
        MessageEntity(senderId = "me", receiverId = "other", content = "It's going great! Logged all meals today.", timestamp = 0, status = "DELIVERED"),
        MessageEntity(senderId = "me", receiverId = "other", content = "Call me at 03211234567", timestamp = 0, status = "AI_BLOCKED", blockedReason = "CONTACT_SHARING_DETECTED")
    )
    com.fitpulse.app.AppTheme {
        ChatContent(
            peerId = "Dr. Sarah Ahmed",
            peerRole = "Nutritionist",
            chatHistory = mockHistory,
            messageText = "Actually, I have a question...",
            onMessageChange = {},
            onSendClick = {},
            onBackClick = {},
            snackbarHostState = SnackbarHostState(),
            myId = "me"
        )
    }
}
