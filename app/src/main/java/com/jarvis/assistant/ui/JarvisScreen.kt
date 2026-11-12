package com.jarvis.assistant.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.jarvis.assistant.CoreState
import com.jarvis.assistant.JarvisViewModel
import com.jarvis.assistant.ai.ChatMessage
import com.jarvis.assistant.ai.Role

@Composable
fun JarvisScreen(vm: JarvisViewModel) {
    val context = LocalContext.current
    var showSettings by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            vm.startListening()
        } else {
            vm.reportError(
                "Microphone permission was denied. You can still type, or enable it in " +
                    "Android Settings > Apps > JARVIS > Permissions."
            )
        }
    }

    fun onMicClick() {
        when {
            vm.coreState == CoreState.LISTENING -> vm.stopListening()
            ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED -> vm.startListening()
            else -> permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    fun submit() {
        if (input.isNotBlank() && vm.coreState != CoreState.THINKING) {
            vm.sendText(input)
            input = ""
        }
    }

    Box(Modifier.fillMaxSize()) {
        HudBackground()

        Column(
            Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp)
        ) {
            TopBar(state = vm.coreState, onSettings = { showSettings = true })

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(0.9f)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                AiCore(state = vm.coreState, modifier = Modifier.aspectRatio(1f))
            }

            StatusLine(vm)

            ChatList(
                messages = vm.messages,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            )

            AnimatedVisibility(visible = vm.errorMessage != null) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(JarvisColors.Danger.copy(alpha = 0.12f))
                        .border(1.dp, JarvisColors.Danger.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
                        .clickable { vm.dismissError() }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = vm.errorMessage ?: "",
                        color = JarvisColors.Danger,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Text(
                        text = "TAP TO DISMISS",
                        color = JarvisColors.TextDim,
                        fontSize = 9.sp,
                        letterSpacing = 1.5.sp
                    )
                }
            }

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Message JARVIS") },
                    maxLines = 3,
                    shape = RoundedCornerShape(24.dp),
                    colors = jarvisFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() })
                )
                AnimatedVisibility(visible = input.isNotBlank()) {
                    IconButton(onClick = { submit() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = JarvisColors.Cyan
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
                MicButton(state = vm.coreState, onClick = { onMicClick() })
            }
        }
    }

    if (showSettings) {
        SettingsSheet(
            initialUrl = vm.backendUrl,
            initialToken = vm.appToken,
            initialSpeak = vm.speakReplies,
            initialRate = vm.speechRate,
            onSave = { url, token, speak, rate -> vm.saveSettings(url, token, speak, rate) },
            onClearChat = { vm.clearChat() },
            onDismiss = { showSettings = false }
        )
    }
}

private fun statusLabel(state: CoreState): String = when (state) {
    CoreState.IDLE -> "SYSTEMS ONLINE"
    CoreState.LISTENING -> "LISTENING"
    CoreState.THINKING -> "PROCESSING"
    CoreState.SPEAKING -> "SPEAKING"
}

@Composable
private fun TopBar(state: CoreState, onSettings: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(top = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = "J.A.R.V.I.S",
                color = JarvisColors.Cyan,
                fontSize = 17.sp,
                fontWeight = FontWeight.Light,
                letterSpacing = 4.sp
            )
            Crossfade(targetState = state, label = "status") { s ->
                Text(
                    text = statusLabel(s),
                    color = JarvisColors.TextDim,
                    fontSize = 10.sp,
                    letterSpacing = 2.sp
                )
            }
        }
        HudClock()
        IconButton(onClick = onSettings) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = JarvisColors.Cyan
            )
        }
    }
}

@Composable
private fun StatusLine(vm: JarvisViewModel) {
    Box(
        Modifier
            .fillMaxWidth()
            .heightIn(min = 38.dp),
        contentAlignment = Alignment.Center
    ) {
        if (vm.isSpeaking) {
            OutlinedButton(
                onClick = { vm.stopSpeaking() },
                border = BorderStroke(1.dp, JarvisColors.Cyan),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = JarvisColors.Cyan),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("STOP SPEAKING", fontSize = 12.sp, letterSpacing = 2.sp)
            }
        } else if (vm.partialText.isNotBlank()) {
            Text(
                text = vm.partialText,
                color = JarvisColors.TextDim,
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChatList(messages: List<ChatMessage>, modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }

    Box(
        modifier
            .hudBrackets()
            .padding(horizontal = 6.dp, vertical = 6.dp)
    ) {
        if (messages.isEmpty()) {
            Text(
                text = "Tap the microphone and speak,\nor type a message below.",
                color = JarvisColors.TextDim,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(messages) { message -> Bubble(message) }
            }
        }
    }
}

@Composable
private fun Bubble(message: ChatMessage) {
    val isUser = message.role == Role.USER
    val shape = RoundedCornerShape(14.dp)
    val borderColor = when {
        message.isError -> JarvisColors.Danger.copy(alpha = 0.7f)
        isUser -> JarvisColors.Cyan.copy(alpha = 0.7f)
        else -> JarvisColors.Blue.copy(alpha = 0.5f)
    }
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(
            Modifier
                .widthIn(max = 300.dp)
                .clip(shape)
                .background(
                    if (isUser) JarvisColors.Cyan.copy(alpha = 0.10f)
                    else Color.White.copy(alpha = 0.04f)
                )
                .border(1.dp, borderColor, shape)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = if (isUser) "YOU" else "JARVIS",
                color = JarvisColors.TextDim,
                fontSize = 9.sp,
                letterSpacing = 2.sp
            )
            Text(
                text = message.text,
                color = if (message.isError) JarvisColors.Danger else JarvisColors.TextPrimary,
                fontSize = 15.sp,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun MicButton(state: CoreState, onClick: () -> Unit) {
    val listening = state == CoreState.LISTENING
    val glow by animateFloatAsState(if (listening) 1f else 0.4f, tween(300), label = "micGlow")
    Box(
        Modifier
            .size(60.dp)
            .alpha(if (state == CoreState.THINKING) 0.5f else 1f)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    listOf(JarvisColors.Cyan.copy(alpha = 0.10f + 0.30f * glow), JarvisColors.NavyLight)
                )
            )
            .border(
                2.dp,
                Brush.sweepGradient(listOf(JarvisColors.Cyan, JarvisColors.Blue, JarvisColors.Cyan)),
                CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (listening) Icons.Filled.Stop else Icons.Filled.Mic,
            contentDescription = if (listening) "Stop listening" else "Start listening",
            tint = JarvisColors.Cyan,
            modifier = Modifier.size(28.dp)
        )
    }
}
