package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Mic
import androidx.compose.material.icons.outlined.MicOff
import androidx.compose.material.icons.outlined.GraphicEq
import androidx.compose.material.icons.outlined.ScreenShare
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.CallState
import com.example.ui.TelegramViewModel
import com.example.ui.theme.OnlineGreen
import com.example.ui.theme.TelegramDarkBackground

@Composable
fun CallScreen(
    viewModel: TelegramViewModel,
    callState: CallState
) {
    val chat = callState.activeChat ?: return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TelegramDarkBackground)
            .testTag("call_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 32.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Lock,
                        contentDescription = "Зашифровано",
                        tint = OnlineGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Зашифрованный звонок (E2EE)",
                        color = OnlineGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = chat.title,
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                val minutes = callState.durationSeconds / 60
                val seconds = callState.durationSeconds % 60
                val timeFormatted = String.format("%02d:%02d", minutes, seconds)

                Text(
                    text = if (callState.isVideoCall) "Видеозвонок HD • $timeFormatted" else "Аудиозвонок • $timeFormatted",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )

                // Recording Notice Banner
                AnimatedVisibility(visible = callState.isRecording) {
                    Surface(
                        color = Color.Red.copy(alpha = 0.8f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Идёт запись звонка (Согласие получено)",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Center Avatar / Video Box
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.title.take(1).uppercase(),
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Noise Suppression Pill
            Surface(
                color = if (callState.isNoiseSuppressed) OnlineGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (callState.isNoiseSuppressed) OnlineGreen else Color.White.copy(alpha = 0.3f)
                ),
                modifier = Modifier.clickable { viewModel.toggleNoiseSuppression() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.GraphicEq,
                        contentDescription = "Шумоподавление",
                        tint = if (callState.isNoiseSuppressed) OnlineGreen else Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (callState.isNoiseSuppressed) "ИИ Шумоподавление ВКЛ" else "Шумоподавление ВЫКЛ",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom Action Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { viewModel.toggleMuteCall() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (callState.isMuted) Color.White else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        imageVector = if (callState.isMuted) Icons.Outlined.MicOff else Icons.Outlined.Mic,
                        contentDescription = "Mute",
                        tint = if (callState.isMuted) Color.Black else Color.White
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleScreenSharing() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (callState.isScreenSharing) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Outlined.ScreenShare,
                        contentDescription = "Screen Share",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { viewModel.toggleRecording() },
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(if (callState.isRecording) Color.Red else Color.White.copy(alpha = 0.2f))
                ) {
                    Icon(
                        Icons.Default.FiberManualRecord,
                        contentDescription = "Record",
                        tint = Color.White
                    )
                }

                FloatingActionButton(
                    onClick = { viewModel.endCall() },
                    containerColor = Color.Red,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.size(64.dp)
                ) {
                    Icon(Icons.Default.CallEnd, contentDescription = "End Call")
                }
            }
        }
    }
}
