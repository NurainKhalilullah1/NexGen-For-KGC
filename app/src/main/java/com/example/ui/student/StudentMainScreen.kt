package com.example.ui.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.ui.components.*
import com.example.ui.theme.NexGenIndigoPrimary
import com.example.ui.theme.RemitaGreen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentMainScreen(
    repository: AppRepository,
    currentUser: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Catalog, 1: My Learning, 2: Remita Invoices, 3: Profile

    // Data streams
    val publishedCourses by repository.getPublishedCourses().collectAsStateWithLifecycle(initialValue = emptyList())
    val myEnrollments by repository.getStudentEnrollments(currentUser.id).collectAsStateWithLifecycle(initialValue = emptyList())
    val myTransactions by repository.getStudentTransactions(currentUser.id).collectAsStateWithLifecycle(initialValue = emptyList())

    // Selected Course for Checkout or Detail
    var selectedCourseForDetail by remember { mutableStateOf<Course?>(null) }
    var selectedCourseForCheckout by remember { mutableStateOf<Course?>(null) }
    var checkoutRrr by remember { mutableStateOf("") }
    var isProcessingPayment by remember { mutableStateOf(false) }

    // Active Lesson Player
    var activeCourseForPlayer by remember { mutableStateOf<Course?>(null) }

    // Selected Transaction for Receipt View
    var selectedTransactionForReceipt by remember { mutableStateOf<Transaction?>(null) }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "NexGen Student",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser.fullName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    RoleBadge(role = currentUser.role)
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("logout_btn")) {
                        Icon(imageVector = Icons.Default.Logout, contentDescription = "Logout")
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
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "Catalog") },
                    label = { Text("Catalog") },
                    modifier = Modifier.testTag("student_tab_catalog")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.School, contentDescription = "My Learning") },
                    label = { Text("My Learning") },
                    modifier = Modifier.testTag("student_tab_mylearning")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = "Remita Payments") },
                    label = { Text("Invoices") },
                    modifier = Modifier.testTag("student_tab_invoices")
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    modifier = Modifier.testTag("student_tab_profile")
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
                0 -> StudentCatalogTab(
                    publishedCourses = publishedCourses,
                    enrolledCourseIds = myEnrollments.map { it.courseId },
                    onSelectCourse = { course -> selectedCourseForDetail = course }
                )
                1 -> StudentMyLearningTab(
                    enrollments = myEnrollments,
                    repository = repository,
                    onOpenPlayer = { course -> activeCourseForPlayer = course }
                )
                2 -> StudentInvoicesTab(
                    transactions = myTransactions,
                    onViewReceipt = { tx -> selectedTransactionForReceipt = tx }
                )
                3 -> StudentProfileTab(
                    user = currentUser,
                    enrolledCount = myEnrollments.size,
                    transactionsCount = myTransactions.size,
                    onUploadAvatar = { imageBytes ->
                        coroutineScope.launch {
                            repository.uploadUserProfileAvatar(currentUser, imageBytes)
                        }
                    }
                )
            }

            // COURSE DETAIL DIALOG
            if (selectedCourseForDetail != null) {
                val course = selectedCourseForDetail!!
                val isEnrolled = myEnrollments.any { it.courseId == course.id }
                val lessons by repository.getLessonsForCourse(course.id).collectAsStateWithLifecycle(initialValue = emptyList())

                AlertDialog(
                    onDismissRequest = { selectedCourseForDetail = null },
                    title = {
                        Column {
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = course.category,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(course.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(course.description, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Tutor: ${course.tutorName}", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "₦%,.2f".format(course.priceNgn),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = RemitaGreen
                                )
                            }

                            HorizontalDivider()

                            Text("Curriculum Lessons (${lessons.size}):", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                            if (lessons.isEmpty()) {
                                Text("No lessons uploaded yet by tutor.", fontSize = 12.sp, color = Color.Gray)
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.heightIn(max = 180.dp)
                                ) {
                                    items(lessons) { lesson ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(10.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text("${lesson.orderIndex}. ${lesson.title}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                            }
                                            Text("${lesson.durationMinutes}m", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        if (isEnrolled) {
                            Button(
                                onClick = {
                                    selectedCourseForDetail = null
                                    activeCourseForPlayer = course
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("open_enrolled_course_btn")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Open Lesson Player")
                            }
                        } else {
                            Button(
                                onClick = {
                                    val rrr = repository.generateRemitaRrr()
                                    checkoutRrr = rrr
                                    selectedCourseForCheckout = course
                                    selectedCourseForDetail = null
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = RemitaGreen),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("enroll_remita_checkout_btn")
                            ) {
                                Icon(Icons.Outlined.ReceiptLong, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Enroll with Remita Gateway")
                            }
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { selectedCourseForDetail = null }) {
                            Text("Close")
                        }
                    }
                )
            }

            // REMITA CHECKOUT DIALOG
            if (selectedCourseForCheckout != null) {
                RemitaCheckoutDialog(
                    course = selectedCourseForCheckout!!,
                    rrr = checkoutRrr,
                    isProcessing = isProcessingPayment,
                    onDismiss = { selectedCourseForCheckout = null },
                    onConfirmPayment = { method ->
                        isProcessingPayment = true
                        coroutineScope.launch {
                            val result = repository.processRemitaPayment(
                                student = currentUser,
                                course = selectedCourseForCheckout!!,
                                paymentMethod = method
                            )
                            isProcessingPayment = false
                            if (result.isSuccess) {
                                val tx = result.getOrNull()
                                selectedCourseForCheckout = null
                                if (tx != null) {
                                    selectedTransactionForReceipt = tx
                                }
                            }
                        }
                    }
                )
            }

            // REMITA RECEIPT MODAL
            if (selectedTransactionForReceipt != null) {
                RemitaReceiptDialog(
                    transaction = selectedTransactionForReceipt!!,
                    onDismiss = { selectedTransactionForReceipt = null }
                )
            }

            // LESSON PLAYER FULLVIEW
            if (activeCourseForPlayer != null) {
                StudentLessonPlayerView(
                    course = activeCourseForPlayer!!,
                    student = currentUser,
                    repository = repository,
                    onClose = { activeCourseForPlayer = null }
                )
            }
        }
    }
}

@Composable
private fun StudentCatalogTab(
    publishedCourses: List<Course>,
    enrolledCourseIds: List<String>,
    onSelectCourse: (Course) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }

    val categories = listOf("All", "Coding", "Web Dev", "Python", "UI/UX", "Robotics & AI")

    val filtered = publishedCourses.filter { course ->
        val matchesSearch = course.title.contains(searchQuery, ignoreCase = true) || course.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || course.category.contains(selectedCategory, ignoreCase = true)
        matchesSearch && matchesCategory
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search tech courses...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("catalog_search_input")
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Filter Pills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.take(4).forEach { cat ->
                FilterChip(
                    selected = selectedCategory == cat,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat, fontSize = 12.sp) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (publishedCourses.isEmpty()) {
            EmptyState(
                title = "No Tech Courses Published Yet",
                description = "Tutors have not published any tech courses yet. Check back soon or contact your tutor!",
                icon = Icons.Outlined.School
            )
        } else if (filtered.isEmpty()) {
            EmptyState(
                title = "No Matching Courses",
                description = "No courses match your search '$searchQuery'. Try clearing your filter.",
                icon = Icons.Default.SearchOff
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { course ->
                    CourseCard(
                        course = course,
                        onClick = { onSelectCourse(course) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentMyLearningTab(
    enrollments: List<Enrollment>,
    repository: AppRepository,
    onOpenPlayer: (Course) -> Unit
) {
    if (enrollments.isEmpty()) {
        EmptyState(
            title = "No Enrolled Courses Yet",
            description = "Browse the course catalog to enroll in tech courses using the Remita gateway.",
            icon = Icons.Outlined.BookmarkBorder
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(enrollments) { enrollment ->
                val courseState = repository.getCourseById(enrollment.courseId).collectAsStateWithLifecycle(initialValue = null)
                val course = courseState.value

                if (course != null) {
                    Card(
                        onClick = { onOpenPlayer(course) },
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
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = course.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }

                                Text(
                                    text = "${enrollment.progressPercentage}% Completed",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RemitaGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = course.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Tutor: ${course.tutorName}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            LinearProgressIndicator(
                                progress = { enrollment.progressPercentage / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = RemitaGreen,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { onOpenPlayer(course) },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().testTag("resume_course_btn_${course.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (enrollment.progressPercentage > 0) "Continue Learning" else "Start Course")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentInvoicesTab(
    transactions: List<Transaction>,
    onViewReceipt: (Transaction) -> Unit
) {
    if (transactions.isEmpty()) {
        EmptyState(
            title = "No Remita Transactions Yet",
            description = "You have not completed any course tuition payments via Remita yet.",
            icon = Icons.Outlined.ReceiptLong
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(transactions) { tx ->
                Card(
                    onClick = { onViewReceipt(tx) },
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
                            Text(
                                text = "RRR: ${tx.remitaRrr}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = NexGenIndigoPrimary
                            )

                            Surface(
                                color = Color(0xFFD1FAE5),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = tx.status,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(tx.courseTitle, fontSize = 16.sp, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paid: ₦%,.2f".format(tx.totalAmountNgn),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = RemitaGreen
                            )

                            TextButton(onClick = { onViewReceipt(tx) }) {
                                Text("View Receipt")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentProfileTab(
    user: User,
    enrolledCount: Int,
    transactionsCount: Int,
    onUploadAvatar: (ByteArray) -> Unit
) {
    var showAvatarDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            contentAlignment = Alignment.BottomEnd,
            modifier = Modifier.clickable { showAvatarDialog = true }
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(40.dp),
                modifier = Modifier.size(96.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (user.avatarUrl.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "User Avatar",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(56.dp)
                        )
                    }
                }
            }

            Surface(
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Upload Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(user.fullName, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(user.email, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(6.dp))

        TextButton(onClick = { showAvatarDialog = true }) {
            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Upload Profile Image (InsForge Storage)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(4.dp))

        RoleBadge(role = user.role)

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$enrolledCount", fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("Enrolled Courses", fontSize = 12.sp)
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.weight(1f)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("$transactionsCount", fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Text("Remita Invoices", fontSize = 12.sp)
                }
            }
        }
    }

    if (showAvatarDialog) {
        ProfileImageUploadDialog(
            onDismiss = { showAvatarDialog = false },
            onConfirmUpload = { imageBytes ->
                showAvatarDialog = false
                onUploadAvatar(imageBytes)
            }
        )
    }
}

@Composable
fun ProfileImageUploadDialog(
    onDismiss: () -> Unit,
    onConfirmUpload: (ByteArray) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CloudUpload, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Upload Profile Image", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "Upload a new avatar picture to your InsForge Storage bucket ('avatars').",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Simulate Gallery Image Selection", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                // Generate demo JPEG payload bytes for bucket upload test
                val dummyBytes = "DEMO_PROFILE_AVATAR_IMAGE_PAYLOAD".toByteArray()
                onConfirmUpload(dummyBytes)
            }) {
                Text("Upload to InsForge Storage")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
