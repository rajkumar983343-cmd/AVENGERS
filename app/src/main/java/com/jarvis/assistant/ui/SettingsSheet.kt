package com.jarvis.assistant.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSheet(
    initialUrl: String,
    initialToken: String,
    initialSpeak: Boolean,
    initialRate: Float,
    onSave: (String, String, Boolean, Float) -> Unit,
    onClearChat: () -> Unit,
    onDismiss: () -> Unit
) {
    var url by remember { mutableStateOf(initialUrl) }
    var token by remember { mutableStateOf(initialToken) }
    var speak by remember { mutableStateOf(initialSpeak) }
    var rate by remember { mutableFloatStateOf(initialRate) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = JarvisColors.Navy,
        contentColor = JarvisColors.TextPrimary
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                "SETTINGS",
                color = JarvisColors.Cyan,
                fontSize = 14.sp,
                letterSpacing = 4.sp
            )

            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Backend URL (https://...)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                colors = jarvisFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = token,
                onValueChange = { token = it },
                label = { Text("App token (shared password for your backend)") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = jarvisFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Speak replies aloud", fontSize = 15.sp)
                Switch(
                    checked = speak,
                    onCheckedChange = { speak = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JarvisColors.Black,
                        checkedTrackColor = JarvisColors.Cyan,
                        uncheckedThumbColor = JarvisColors.TextDim,
                        uncheckedTrackColor = JarvisColors.NavyLight
                    )
                )
            }

            Column {
                Text(
                    "Speech speed: ${"%.1f".format(rate)}x",
                    fontSize = 14.sp,
                    color = JarvisColors.TextDim
                )
                Slider(
                    value = rate,
                    onValueChange = { rate = it },
                    valueRange = 0.6f..1.6f,
                    colors = SliderDefaults.colors(
                        thumbColor = JarvisColors.Cyan,
                        activeTrackColor = JarvisColors.Cyan,
                        inactiveTrackColor = JarvisColors.NavyLight
                    )
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = {
                        onClearChat()
                        onDismiss()
                    }
                ) {
                    Text("Clear chat")
                }
                Button(
                    onClick = {
                        onSave(url, token, speak, rate)
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JarvisColors.Cyan,
                        contentColor = JarvisColors.Black
                    )
                ) {
                    Text("Save")
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
