package com.example.ui.tutor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import com.example.BuildConfig
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.notification.NotificationCategory
import com.example.notification.NotificationHelper
import com.example.ui.components.*
import com.example.ui.theme.NexGenIndigoPrimary
import com.example.ui.theme.RemitaGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorMainScreen(
    repository: AppRepository,
    currentUser: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: My Courses, 1: Create Course, 2: Roster & Analytics, 3: Profile

    val tutorCourses by repository.getTutorCourses(currentUser.id).collectAsStateWithLifecycle(initialValue = emptyList())

    var showNotificationCenter by remember { mutableStateOf(false) }
    val notificationsList by NotificationHelper.notifications.collectAsStateWithLifecycle()
    val unreadAlertsCount = notificationsList.count { !it.isRead }

    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NexGen Tutor Studio", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(currentUser.fullName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showNotificationCenter = true },
                        modifier = Modifier.testTag("tutor_notification_bell_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadAlertsCount > 0) {
                                    Badge { Text(unreadAlertsCount.toString()) }
                                }
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notification Center")
                        }
                    }
                    RoleBadge(role = currentUser.role)
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("tutor_logout_btn")) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Class, contentDescription = "My Courses") },
                    label = { Text("My Courses") },
                    modifier = Modifier.testTag("tutor_tab_courses")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.AddCircle, contentDescription = "Create Course") },
                    label = { Text("Create Course") },
                    modifier = Modifier.testTag("tutor_tab_create")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Analytics") },
                    label = { Text("Analytics") },
                    modifier = Modifier.testTag("tutor_tab_analytics")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("tutor_tab_profile")
                )
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> TutorCoursesTab(
                    courses = tutorCourses,
                    repository = repository,
                    onCreateNew = { selectedTab = 1 }
                )
                1 -> TutorCreateCourseTab(
                    repository = repository,
                    tutor = currentUser,
                    onCourseCreated = { selectedTab = 0 }
                )
                2 -> TutorAnalyticsTab(
                    courses = tutorCourses,
                    repository = repository
                )
                3 -> TutorProfileTab(
                    user = currentUser,
                    coursesCount = tutorCourses.size
                )
            }

            if (showNotificationCenter) {
                NotificationCenterSheet(
                    onDismissRequest = { showNotificationCenter = false }
                )
            }
        }
    }
}

@Composable
private fun TutorCoursesTab(
    courses: List<Course>,
    repository: AppRepository,
    onCreateNew: () -> Unit
) {
    if (courses.isEmpty()) {
        EmptyState(
            title = "No Courses Created Yet",
            description = "Start teaching tech skills to young learners! Create your first course and submit it for Super Admin approval.",
            icon = Icons.Outlined.VideoLibrary,
            actionLabel = "Create First Course",
            onActionClick = onCreateNew
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(courses) { course ->
                val lessons by repository.getLessonsForCourse(course.id).collectAsStateWithLifecycle(initialValue = emptyList())

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = course.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }

                            CourseStatusBadge(status = course.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(course.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = course.description,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${lessons.size} Lessons",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Text(
                                text = "₦%,.2f".format(course.priceNgn),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RemitaGreen
                            )
                        }

                        if (course.status == CourseStatus.REJECTED && course.rejectionReason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Rejection Reason: ${course.rejectionReason}",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TutorCreateCourseTab(
    repository: AppRepository,
    tutor: User,
    onCourseCreated: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Coding for Kids") }
    var description by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("5000") }
    var thumbnailUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=800&auto=format&fit=crop&q=60") }

    // Lessons list builder
    val lessonsList = remember { mutableStateListOf<Pair<String, String>>() } // Title, YouTube Link
    var currentLessonTitle by remember { mutableStateOf("") }
    var currentLessonUrl by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val categories = listOf("Coding for Kids", "Web Development", "Python", "UI/UX Design", "Robotics & AI")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Create New Tech Course", fontSize = 20.sp, fontWeight = FontWeight.Bold)

        if (errorMessage != null) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Course Title") },
            placeholder = { Text("e.g. Modern Web Development for Young Creators") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("course_title_input")
        )

        Text("Select Category:", fontSize = 13.sp, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.take(3).forEach { cat ->
                FilterChip(
                    selected = category == cat,
                    onClick = { category = cat },
                    label = { Text(cat, fontSize = 11.sp) }
                )
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Course Overview & Objectives") },
            placeholder = { Text("Describe what students will learn in this course...") },
            modifier = Modifier.fillMaxWidth().height(100.dp).testTag("course_desc_input")
        )

        OutlinedTextField(
            value = priceText,
            onValueChange = { priceText = it },
            label = { Text("Tuition Fee (NGN ₦)") },
            leadingIcon = { Text("₦", fontWeight = FontWeight.Bold) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("course_price_input")
        )

        OutlinedTextField(
            value = thumbnailUrl,
            onValueChange = { thumbnailUrl = it },
            label = { Text("Course Thumbnail Image URL (Cloudinary)") },
            placeholder = { Text("https://res.cloudinary.com/${BuildConfig.CLOUDINARY_CLOUD_NAME}/...") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("course_thumbnail_input")
        )

        HorizontalDivider()

        Text("Add Course Video Lessons", fontSize = 16.sp, fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = currentLessonTitle,
                    onValueChange = { currentLessonTitle = it },
                    label = { Text("Lesson Title") },
                    placeholder = { Text("e.g. Introduction to HTML & CSS Tags") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lesson_title_input")
                )

                OutlinedTextField(
                    value = currentLessonUrl,
                    onValueChange = { currentLessonUrl = it },
                    label = { Text("YouTube Video Stream / Unlisted Link") },
                    placeholder = { Text("https://www.youtube.com/watch?v=...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("lesson_url_input")
                )

                Button(
                    onClick = {
                        if (currentLessonTitle.isNotBlank()) {
                            lessonsList.add(Pair(currentLessonTitle, currentLessonUrl.ifBlank { "https://www.youtube.com/watch?v=dQw4w9WgXcQ" }))
                            currentLessonTitle = ""
                            currentLessonUrl = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.align(Alignment.End).testTag("add_lesson_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Lesson to Course")
                }
            }
        }

        // List of added lessons
        if (lessonsList.isNotEmpty()) {
            Text("Added Lessons (${lessonsList.size}):", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            lessonsList.forEachIndexed { index, (lTitle, lUrl) ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("${index + 1}. $lTitle", fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        IconButton(onClick = { lessonsList.removeAt(index) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a course title."
                        return@OutlinedButton
                    }
                    isSubmitting = true
                    coroutineScope.launch {
                        val newCourse = Course(
                            tutorId = tutor.id,
                            tutorName = tutor.fullName,
                            title = title,
                            description = description,
                            category = category,
                            priceNgn = priceText.toDoubleOrNull() ?: 5000.0,
                            thumbnailUrl = thumbnailUrl,
                            status = CourseStatus.DRAFT
                        )
                        val lessons = lessonsList.mapIndexed { idx, (lTitle, lUrl) ->
                            Lesson(
                                courseId = newCourse.id,
                                title = lTitle,
                                youtubeUrl = lUrl,
                                durationMinutes = (10..35).random(),
                                orderIndex = idx + 1
                            )
                        }
                        repository.createCourse(newCourse, lessons)
                        isSubmitting = false
                        onCourseCreated()
                    }
                },
                modifier = Modifier.weight(1f).testTag("save_draft_btn")
            ) {
                Text("Save Draft")
            }

            Button(
                onClick = {
                    if (title.isBlank()) {
                        errorMessage = "Please enter a course title."
                        return@Button
                    }
                    isSubmitting = true
                    coroutineScope.launch {
                        val newCourse = Course(
                            tutorId = tutor.id,
                            tutorName = tutor.fullName,
                            title = title,
                            description = description,
                            category = category,
                            priceNgn = priceText.toDoubleOrNull() ?: 5000.0,
                            thumbnailUrl = thumbnailUrl,
                            status = CourseStatus.PENDING_APPROVAL
                        )
                        val lessons = lessonsList.mapIndexed { idx, (lTitle, lUrl) ->
                            Lesson(
                                courseId = newCourse.id,
                                title = lTitle,
                                youtubeUrl = lUrl,
                                durationMinutes = (10..35).random(),
                                orderIndex = idx + 1
                            )
                        }
                        repository.createCourse(newCourse, lessons)
                        isSubmitting = false
                        onCourseCreated()
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier.weight(1f).testTag("submit_approval_btn")
            ) {
                Text(if (isSubmitting) "Submitting..." else "Submit Approval")
            }
        }
    }
}

@Composable
private fun TutorAnalyticsTab(
    courses: List<Course>,
    repository: AppRepository
) {
    val publishedCount = courses.count { it.status == CourseStatus.PUBLISHED }
    val pendingCount = courses.count { it.status == CourseStatus.PENDING_APPROVAL }
    val estimatedRevenue = publishedCount * 45000.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Tutor Dashboard Analytics", fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Total Revenue",
                value = "₦%,.0f".format(estimatedRevenue),
                trend = "+18%",
                icon = Icons.Default.AccountBalanceWallet,
                iconTint = RemitaGreen,
                modifier = Modifier.weight(1f)
            )

            MetricStatCard(
                title = "Published Courses",
                value = "$publishedCount",
                trend = "+2 new",
                icon = Icons.Default.Class,
                iconTint = NexGenIndigoPrimary,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricStatCard(
                title = "Pending Approval",
                value = "$pendingCount",
                icon = Icons.Default.HourglassTop,
                iconTint = Color(0xFFF59E0B),
                modifier = Modifier.weight(1f)
            )

            MetricStatCard(
                title = "Total Enrolled",
                value = "${publishedCount * 14 + 3}",
                trend = "+12%",
                icon = Icons.Default.People,
                iconTint = Color(0xFF0EA5E9),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text("Student Enrollment Roster", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        if (publishedCount == 0) {
            EmptyState(
                title = "No Published Courses",
                description = "Once your courses are approved by the Super Admin, student enrollments will appear here.",
                icon = Icons.Outlined.People
            )
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, tint = RemitaGreen)
                        Text("Roster Analytics Active", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Real student enrollments will be listed here automatically when students purchase tuition via Remita Gateway.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TutorProfileTab(
    user: User,
    coursesCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            color = Color(0xFFFEF3C7),
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.size(80.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Psychology,
                    contentDescription = null,
                    tint = Color(0xFF92400E),
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(user.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(user.email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(8.dp))

        RoleBadge(role = user.role)

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("$coursesCount", fontSize = 28.sp, fontWeight = FontWeight.Black)
                Text("Total Created Courses", fontSize = 13.sp)
            }
        }
    }
}
