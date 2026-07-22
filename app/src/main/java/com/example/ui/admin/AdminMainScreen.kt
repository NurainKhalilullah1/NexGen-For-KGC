package com.example.ui.admin

import androidx.compose.foundation.background
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
fun AdminMainScreen(
    repository: AppRepository,
    currentUser: User,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Pending Approvals, 1: User Management, 2: Remita Audit, 3: Profile

    val pendingCourses by repository.getPendingCourses().collectAsStateWithLifecycle(initialValue = emptyList())
    val allCourses by repository.getAllCourses().collectAsStateWithLifecycle(initialValue = emptyList())
    val allUsers by repository.getAllUsers().collectAsStateWithLifecycle(initialValue = emptyList())
    val allTransactions by repository.getAllTransactions().collectAsStateWithLifecycle(initialValue = emptyList())

    val totalRemitaRevenue = remember(allTransactions) { allTransactions.sumOf { it.totalAmountNgn } }

    var selectedCourseForReview by remember { mutableStateOf<Course?>(null) }
    var rejectionReasonInput by remember { mutableStateOf("") }

    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NexGen Super Admin", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Authorized: ${currentUser.email}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                actions = {
                    RoleBadge(role = currentUser.role)
                    IconButton(onClick = onLogout, modifier = Modifier.testTag("admin_logout_btn")) {
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
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingCourses.isNotEmpty()) {
                                    Badge { Text("${pendingCourses.size}") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.FactCheck, contentDescription = "Approvals")
                        }
                    },
                    label = { Text("Approvals") },
                    modifier = Modifier.testTag("admin_tab_approvals")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = "Users") },
                    label = { Text("Users") },
                    modifier = Modifier.testTag("admin_tab_users")
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Outlined.ReceiptLong, contentDescription = "Remita Audit") },
                    label = { Text("Audit Log") },
                    modifier = Modifier.testTag("admin_tab_audit")
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
                0 -> AdminApprovalsTab(
                    pendingCourses = pendingCourses,
                    onReviewCourse = { course -> selectedCourseForReview = course }
                )
                1 -> AdminUserManagementTab(
                    users = allUsers,
                    onUpdateRole = { userId, newRole ->
                        coroutineScope.launch {
                            repository.updateUserRole(userId, newRole)
                        }
                    },
                    onDeleteUser = { userId ->
                        coroutineScope.launch {
                            repository.deleteUser(userId)
                        }
                    }
                )
                2 -> AdminRemitaAuditTab(
                    transactions = allTransactions,
                    totalRevenue = totalRemitaRevenue,
                    totalUsersCount = allUsers.size,
                    totalCoursesCount = allCourses.size
                )
            }

            // COURSE REVIEW DIALOG
            if (selectedCourseForReview != null) {
                val course = selectedCourseForReview!!
                val lessons by repository.getLessonsForCourse(course.id).collectAsStateWithLifecycle(initialValue = emptyList())

                AlertDialog(
                    onDismissRequest = { selectedCourseForReview = null },
                    title = {
                        Column {
                            Text("Review Course Submission", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            Text("Submitted by Tutor: ${course.tutorName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(course.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text(course.description, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Category: ${course.category}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                Text("Tuition: ₦%,.2f".format(course.priceNgn), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = RemitaGreen)
                            }

                            HorizontalDivider()

                            Text("Submitted Video Lessons (${lessons.size}):", fontSize = 13.sp, fontWeight = FontWeight.Bold)

                            if (lessons.isEmpty()) {
                                Text("Warning: No video lessons attached.", fontSize = 12.sp, color = Color.Red)
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.heightIn(max = 140.dp)
                                ) {
                                    items(lessons) { l ->
                                        Text("• ${l.orderIndex}. ${l.title} (${l.durationMinutes}m)", fontSize = 12.sp)
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = rejectionReasonInput,
                                onValueChange = { rejectionReasonInput = it },
                                label = { Text("Rejection Feedback (if rejecting)") },
                                placeholder = { Text("Reason for rejection...") },
                                modifier = Modifier.fillMaxWidth().testTag("rejection_reason_input")
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    repository.updateCourseStatus(course.id, CourseStatus.PUBLISHED)
                                    selectedCourseForReview = null
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = RemitaGreen),
                            modifier = Modifier.testTag("approve_publish_btn")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Approve & Publish")
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    repository.updateCourseStatus(
                                        course.id,
                                        CourseStatus.REJECTED,
                                        rejectionReasonInput.ifBlank { "Does not meet NexGen platform guidelines." }
                                    )
                                    selectedCourseForReview = null
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            modifier = Modifier.testTag("reject_course_btn")
                        ) {
                            Text("Reject Course")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AdminApprovalsTab(
    pendingCourses: List<Course>,
    onReviewCourse: (Course) -> Unit
) {
    if (pendingCourses.isEmpty()) {
        EmptyState(
            title = "All Submissions Approved!",
            description = "There are currently no pending course submissions waiting for Super Admin review.",
            icon = Icons.Outlined.CheckCircle
        )
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(pendingCourses) { course ->
                CourseCard(
                    course = course,
                    onClick = { onReviewCourse(course) },
                    showStatus = true
                )
            }
        }
    }
}

@Composable
private fun AdminUserManagementTab(
    users: List<User>,
    onUpdateRole: (userId: String, newRole: UserRole) -> Unit,
    onDeleteUser: (userId: String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredUsers = users.filter { it.fullName.contains(searchQuery, ignoreCase = true) || it.email.contains(searchQuery, ignoreCase = true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search users by name or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredUsers) { user ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.fullName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            RoleBadge(role = user.role)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modify Role:", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                FilterChip(
                                    selected = user.role == UserRole.STUDENT,
                                    onClick = { onUpdateRole(user.id, UserRole.STUDENT) },
                                    label = { Text("Student", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = user.role == UserRole.TUTOR,
                                    onClick = { onUpdateRole(user.id, UserRole.TUTOR) },
                                    label = { Text("Tutor", fontSize = 10.sp) }
                                )
                                FilterChip(
                                    selected = user.role == UserRole.SUPER_ADMIN,
                                    onClick = { onUpdateRole(user.id, UserRole.SUPER_ADMIN) },
                                    label = { Text("Admin", fontSize = 10.sp) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminRemitaAuditTab(
    transactions: List<Transaction>,
    totalRevenue: Double,
    totalUsersCount: Int,
    totalCoursesCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("NexGen Platform Revenue & Remita Audit", fontSize = 18.sp, fontWeight = FontWeight.Bold)

        Card(
            colors = CardDefaults.cardColors(containerColor = NexGenIndigoPrimary),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Total Platform Remita Volume", color = Color.LightGray, fontSize = 12.sp)
                Text(
                    text = "₦%,.2f".format(totalRevenue),
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Users: $totalUsersCount", color = Color.White, fontSize = 12.sp)
                    Text("Total Courses: $totalCoursesCount", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        Text("All Remita Transactions (${transactions.size}):", fontSize = 14.sp, fontWeight = FontWeight.Bold)

        if (transactions.isEmpty()) {
            EmptyState(
                title = "No Transactions Recorded",
                description = "Remita course tuition purchases will automatically appear here.",
                icon = Icons.Outlined.ReceiptLong
            )
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(transactions) { tx ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("RRR: ${tx.remitaRrr}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = NexGenIndigoPrimary)
                                Text("₦%,.2f".format(tx.totalAmountNgn), fontWeight = FontWeight.ExtraBold, color = RemitaGreen)
                            }
                            Text("Course: ${tx.courseTitle}", fontSize = 13.sp)
                            Text("Student: ${tx.studentName} (${tx.studentEmail})", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}
