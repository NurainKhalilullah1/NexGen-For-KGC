package com.example.ui.components

import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.Lesson

fun extractYouTubeVideoId(url: String): String {
    if (url.isBlank()) return "dQw4w9WgXcQ"
    return when {
        url.contains("v=") -> url.substringAfter("v=").substringBefore("&").substringBefore("?")
        url.contains("youtu.be/") -> url.substringAfter("youtu.be/").substringBefore("?")
        url.contains("embed/") -> url.substringAfter("embed/").substringBefore("?")
        else -> url.trim()
    }
}

@Composable
fun LessonVideoPlayer(
    lesson: Lesson,
    isCompleted: Boolean,
    onMarkCompleted: () -> Unit,
    onNextLesson: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isPlayerActive by remember(lesson.id) { mutableStateOf(false) }
    val videoId = remember(lesson.youtubeUrl) { extractYouTubeVideoId(lesson.youtubeUrl) }

    val htmlContent = remember(videoId) {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
            <style>
                * { margin: 0; padding: 0; box-sizing: border-box; }
                body, html { width: 100%; height: 100%; background-color: #000000; overflow: hidden; }
                .iframe-container { position: relative; width: 100%; height: 100%; }
                iframe { position: absolute; top: 0; left: 0; width: 100%; height: 100%; border: 0; }
            </style>
        </head>
        <body>
            <div class="iframe-container">
                <iframe src="https://www.youtube-nocookie.com/embed/$videoId?autoplay=1&modestbranding=1&rel=0&playsinline=1" 
                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                        allowfullscreen></iframe>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        modifier = modifier
            .fillMaxWidth()
            .testTag("video_player_card")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Video Frame Screen
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Color(0xFF0F172A)),
                contentAlignment = Alignment.Center
            ) {
                if (isPlayerActive) {
                    AndroidView(
                        factory = { context ->
                            WebView(context).apply {
                                layoutParams = ViewGroup.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.mediaPlaybackRequiresUserGesture = false
                                settings.loadWithOverviewMode = true
                                settings.useWideViewPort = true
                                webViewClient = WebViewClient()
                                webChromeClient = WebChromeClient()
                                loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "UTF-8", null)
                            }
                        },
                        update = { webView ->
                            webView.loadDataWithBaseURL("https://www.youtube.com", htmlContent, "text/html", "UTF-8", null)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Preview Frame with Tap to Play trigger
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { isPlayerActive = true }
                            .padding(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayCircleFilled,
                            contentDescription = "Start Streaming Video",
                            tint = Color.White,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Tap to Stream YouTube Video",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
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
                                    text = "YouTube Unlisted Stream ($videoId)",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Overlay Banner displaying Lesson Title
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.7f))
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

            // Controls Bar below Video
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E293B))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { isPlayerActive = !isPlayerActive }) {
                        Icon(
                            imageVector = if (isPlayerActive) Icons.Default.Refresh else Icons.Default.PlayArrow,
                            contentDescription = "Reload Player",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = if (isPlayerActive) "Streaming Active" else "Tap Play to Stream",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
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
