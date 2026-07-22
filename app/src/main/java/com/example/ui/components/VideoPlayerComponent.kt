package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.PlayCircle
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
import com.example.data.model.Lesson

@Composable
fun LessonVideoPlayer(
    lesson: Lesson,
    isCompleted: Boolean,
    onMarkCompleted: () -> Unit,
    onNextLesson: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember(lesson.id) { mutableStateOf(false) }
    var currentProgress by remember(lesson.id) { mutableFloatStateOf(0.15f) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_player_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Video Screen Frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                // Background visual
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircleFilled,
                        contentDescription = "Play/Pause",
                        tint = Color.White,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .clickable { isPlaying = !isPlaying }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (isPlaying) "Streaming Lesson Video..." else "Tap to Play Lesson",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Surface(
                        color = Color(0xFFCC0000), // YouTube Red
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlayCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "YouTube Stream",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Top Bar overlay with YouTube URL info
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lesson ${lesson.orderIndex}: ${lesson.title}",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        modifier = Modifier.weight(1f)
                    )

                    Surface(
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${lesson.durationMinutes} mins",
                            color = Color.White,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Scrubbing Progress Bar
            LinearProgressIndicator(
                progress = { currentProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = Color.DarkGray
            )

            // Video Controls Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isPlaying = !isPlaying }) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = if (isPlaying) "02:15 / ${lesson.durationMinutes}:00" else "00:00 / ${lesson.durationMinutes}:00",
                        color = Color.LightGray,
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onMarkCompleted,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isCompleted) Color(0xFF059669) else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("mark_completed_btn")
                    ) {
                        Icon(
                            imageVector = if (isCompleted) Icons.Outlined.CheckCircle else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isCompleted) "Completed" else "Mark Complete",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (onNextLesson != null) {
                        IconButton(onClick = onNextLesson) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Lesson",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
