package com.example.ui.components

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notification.NotificationCategory
import com.example.notification.NotificationHelper
import com.example.notification.PushNotificationItem
import com.example.ui.theme.NexGenIndigoPrimary
import com.example.ui.theme.RemitaGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val notifications by NotificationHelper.notifications.collectAsStateWithLifecycle()
    var selectedCategoryFilter by remember { mutableStateOf<NotificationCategory?>(null) }
    
    var hasPermission by remember { mutableStateOf(NotificationHelper.hasNotificationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            NotificationHelper.sendPushNotification(
                context = context,
                title = "Push Notifications Enabled! 🎉",
                message = "You will now receive instant push alerts for course updates, assignments, and study reminders.",
                category = NotificationCategory.SYSTEM
            )
        }
    }

    val unreadCount = notifications.count { !it.isRead }
    val filteredNotifications = remember(notifications, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) notifications
        else notifications.filter { it.category == selectedCategoryFilter }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier.testTag("notification_center_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notifications",
                        tint = NexGenIndigoPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Push Notification Center",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    if (unreadCount > 0) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.error,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "$unreadCount new",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = onDismissRequest) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Permission Banner (If Android 13+ and permission not granted)
            if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Push Notifications Disabled",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Grant permission to receive instant alerts for courses & assignments.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                        Button(
                            onClick = {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("enable_push_permission_btn")
                        ) {
                            Text("Enable", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Quick Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        NotificationHelper.sendPushNotification(
                            context = context,
                            title = "Test Push Notification 🚀",
                            message = "NexGen LMS system push notifications are working perfectly on your device!",
                            category = NotificationCategory.SYSTEM
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("send_test_push_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = NexGenIndigoPrimary),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test Push", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = {
                        NotificationHelper.sendPushNotification(
                            context = context,
                            title = "Study Reminder 📚",
                            message = "Time for your daily learning session! Don't forget to complete your active modules.",
                            category = NotificationCategory.REMINDER,
                            channelId = NotificationHelper.CHANNEL_ID_REMINDERS
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("trigger_reminder_btn"),
                    contentPadding = PaddingValues(vertical = 10.dp)
                ) {
                    Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reminder", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FilterChip(
                        selected = selectedCategoryFilter == null,
                        onClick = { selectedCategoryFilter = null },
                        label = { Text("All (${notifications.size})", fontSize = 12.sp) }
                    )
                }
                items(NotificationCategory.values()) { category ->
                    val count = notifications.count { it.category == category }
                    FilterChip(
                        selected = selectedCategoryFilter == category,
                        onClick = { selectedCategoryFilter = category },
                        label = { Text("${category.label} ($count)", fontSize = 12.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Mark all as read / Clear
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Alerts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    if (unreadCount > 0) {
                        TextButton(onClick = { NotificationHelper.markAllAsRead() }) {
                            Text("Mark all read", fontSize = 12.sp)
                        }
                    }
                    if (notifications.isNotEmpty()) {
                        TextButton(onClick = { NotificationHelper.clearAll() }) {
                            Text("Clear all", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List of Notifications
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No push notifications yet",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.heightIn(max = 380.dp)
                ) {
                    items(filteredNotifications, key = { it.id }) { item ->
                        PushNotificationCard(
                            item = item,
                            onClick = { NotificationHelper.markAsRead(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PushNotificationCard(
    item: PushNotificationItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryIcon = when (item.category) {
        NotificationCategory.COURSE -> Icons.Default.Book
        NotificationCategory.ASSIGNMENT -> Icons.Default.Assignment
        NotificationCategory.ANNOUNCEMENT -> Icons.Default.Campaign
        NotificationCategory.SYSTEM -> Icons.Default.Verified
        NotificationCategory.REMINDER -> Icons.Default.Schedule
    }

    val categoryColor = when (item.category) {
        NotificationCategory.COURSE -> NexGenIndigoPrimary
        NotificationCategory.ASSIGNMENT -> Color(0xFFE65100)
        NotificationCategory.ANNOUNCEMENT -> RemitaGreen
        NotificationCategory.SYSTEM -> Color(0xFF1976D2)
        NotificationCategory.REMINDER -> Color(0xFF7B1FA2)
    }

    val timeString = remember(item.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(item.timestamp))
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (!item.isRead) categoryColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = categoryIcon,
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.title,
                        fontSize = 14.sp,
                        fontWeight = if (!item.isRead) FontWeight.Bold else FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!item.isRead) {
                        Box(
                            modifier = Modifier
                                .padding(start = 6.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(categoryColor)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.message,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = timeString,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}
