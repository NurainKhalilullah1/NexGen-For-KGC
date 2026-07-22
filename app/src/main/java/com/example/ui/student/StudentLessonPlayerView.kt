package com.example.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Course
import com.example.data.model.Lesson
import com.example.data.model.User
import com.example.data.repository.AppRepository
import com.example.ui.components.LessonVideoPlayer
import com.example.ui.theme.RemitaGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentLessonPlayerView(
    course: Course,
    student: User,
    repository: AppRepository,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lessons by repository.getLessonsForCourse(course.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val enrollmentState = repository.getEnrollment(student.id, course.id).collectAsStateWithLifecycle(initialValue = null)
    val enrollment = enrollmentState.value

    var selectedLessonIndex by remember { mutableIntStateOf(0) }
    val currentLesson = lessons.getOrNull(selectedLessonIndex)

    val completedList = remember(enrollment?.completedLessonsJson) {
        enrollment?.completedLessonsJson?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
    }

    var notesText by remember(enrollment?.notes) { mutableStateOf(enrollment?.notes ?: "") }
    var isSavingNotes by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopAppBar(
                title = {
                    Column {
                        Text(course.title, fontSize = 16.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("${lessons.size} Lessons • Progress: ${enrollment?.progressPercentage ?: 0}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Close Player")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )

            if (lessons.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No video lessons available in this course yet.", color = Color.Gray)
                }
            } else if (currentLesson != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Visual Progress Tracker Card
                    val completedCount = completedList.size
                    val totalLessons = lessons.size
                    val calculatedProgress = if (totalLessons > 0) (completedCount.toFloat() / totalLessons) else 0f
                    val progressPercentInt = (calculatedProgress * 100).toInt()

                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lesson_progress_tracker_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Analytics,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Lesson Progress Tracker",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                Surface(
                                    color = if (progressPercentInt == 100) RemitaGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = if (progressPercentInt == 100) "🎉 100% Completed" else "$progressPercentInt% Done",
                                        color = if (progressPercentInt == 100) RemitaGreen else MaterialTheme.colorScheme.primary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { calculatedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                    .testTag("lesson_progress_bar"),
                                color = if (progressPercentInt == 100) RemitaGreen else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surface
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "$completedCount of $totalLessons lessons finished",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.testTag("lesson_progress_text")
                                )
                                Text(
                                    text = "Saved to Database",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Video Player
                    val isCurrentCompleted = completedList.contains(currentLesson.id)

                    LessonVideoPlayer(
                        lesson = currentLesson,
                        isCompleted = isCurrentCompleted,
                        onMarkCompleted = {
                            if (enrollment != null) {
                                coroutineScope.launch {
                                    repository.toggleLessonProgress(
                                        enrollmentId = enrollment.id,
                                        lessonId = currentLesson.id,
                                        allLessonsCount = lessons.size,
                                        currentCompletedList = completedList
                                    )
                                }
                            }
                        },
                        onNextLesson = if (selectedLessonIndex < lessons.size - 1) {
                            { selectedLessonIndex++ }
                        } else null
                    )

                    // Tabs: Lesson Playlist vs Personal Notes
                    var playerSubTab by remember { mutableIntStateOf(0) }

                    TabRow(selectedTabIndex = playerSubTab) {
                        Tab(
                            selected = playerSubTab == 0,
                            onClick = { playerSubTab = 0 },
                            text = { Text("Course Playlist (${lessons.size})") }
                        )
                        Tab(
                            selected = playerSubTab == 1,
                            onClick = { playerSubTab = 1 },
                            text = { Text("My Notes") }
                        )
                    }

                    if (playerSubTab == 0) {
                        // Playlist List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            items(lessons) { lesson ->
                                val isSelected = lesson.id == currentLesson.id
                                val isDone = completedList.contains(lesson.id)

                                Card(
                                    onClick = { selectedLessonIndex = lessons.indexOf(lesson) },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            IconButton(
                                                onClick = {
                                                    if (enrollment != null) {
                                                        coroutineScope.launch {
                                                            repository.toggleLessonProgress(
                                                                enrollmentId = enrollment.id,
                                                                lessonId = lesson.id,
                                                                allLessonsCount = lessons.size,
                                                                currentCompletedList = completedList
                                                            )
                                                        }
                                                    }
                                                },
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .testTag("playlist_toggle_${lesson.id}")
                                            ) {
                                                Icon(
                                                    imageVector = if (isDone) Icons.Outlined.CheckCircle else Icons.Default.PlayCircle,
                                                    contentDescription = "Toggle completion",
                                                    tint = if (isDone) RemitaGreen else MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "${lesson.orderIndex}. ${lesson.title}",
                                                    fontSize = 14.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                )
                                                Text(
                                                    text = "${lesson.durationMinutes} minutes",
                                                    fontSize = 12.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }

                                        if (isSelected) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primary,
                                                shape = RoundedCornerShape(6.dp)
                                            ) {
                                                Text("Playing", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        // Notes Editor
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        ) {
                            OutlinedTextField(
                                value = notesText,
                                onValueChange = { notesText = it },
                                label = { Text("Personal Learning Notes") },
                                placeholder = { Text("Write key insights, code snippets, or questions here...") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                                    .testTag("student_notes_input")
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    if (enrollment != null) {
                                        isSavingNotes = true
                                        coroutineScope.launch {
                                            repository.updateStudentNotes(enrollment.id, notesText)
                                            isSavingNotes = false
                                        }
                                    }
                                },
                                enabled = !isSavingNotes,
                                modifier = Modifier.fillMaxWidth().testTag("save_notes_btn")
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isSavingNotes) "Saving..." else "Save Notes")
                            }
                        }
                    }
                }
            }
        }
    }
}
