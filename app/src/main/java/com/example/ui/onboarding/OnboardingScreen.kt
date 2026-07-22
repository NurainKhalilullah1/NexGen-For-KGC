package com.example.ui.onboarding

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TechHorizonColors

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val badgeLabel: String,
    val gradientColors: List<Color>
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Learn Future-Ready Tech Skills",
                description = "Master coding, web development, Python, AI, and robotics through interactive hands-on modules designed for young minds.",
                icon = Icons.Default.Terminal,
                badgeLabel = "Interactive Curriculum",
                gradientColors = listOf(Color(0xFF4F46E5), Color(0xFF06B6D4))
            ),
            OnboardingPageData(
                title = "Stream Live & Unlisted Lessons",
                description = "Seamless YouTube video streaming with instant code snippets, step-by-step video guides, and student note taking.",
                icon = Icons.Default.OndemandVideo,
                badgeLabel = "HD Video Streaming",
                gradientColors = listOf(Color(0xFF06B6D4), Color(0xFF10B981))
            ),
            OnboardingPageData(
                title = "Track Progress & Tuition Checkout",
                description = "Monitor lesson completion rates, view student rosters, and pay tuition fees securely using Remita API.",
                icon = Icons.Default.ReceiptLong,
                badgeLabel = "Remita Payments & Stats",
                gradientColors = listOf(Color(0xFFF97316), Color(0xFF4F46E5))
            )
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.School,
                        contentDescription = null,
                        tint = TechHorizonColors.Light.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NexGen LMS",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (currentPage < pages.size - 1) {
                    TextButton(
                        onClick = onFinishOnboarding,
                        modifier = Modifier.testTag("onboarding_skip_btn")
                    ) {
                        Text(
                            text = "Skip",
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = index == currentPage
                        Box(
                            modifier = Modifier
                                .height(8.dp)
                                .width(if (isSelected) 28.dp else 8.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                Button(
                    onClick = {
                        if (currentPage < pages.size - 1) {
                            currentPage++
                        } else {
                            onFinishOnboarding()
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("onboarding_next_btn")
                ) {
                    Text(
                        text = if (currentPage == pages.size - 1) "Get Started" else "Next",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = if (currentPage == pages.size - 1) Icons.Default.RocketLaunch else Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        AnimatedContent(
            targetState = currentPage,
            transitionSpec = {
                slideInHorizontally { width -> width } + fadeIn() with
                        slideOutHorizontally { width -> -width } + fadeOut()
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) { pageIndex ->
            val page = pages[pageIndex]

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Hero Graphic Card
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .background(
                            brush = Brush.linearGradient(page.gradientColors),
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.25f),
                            shape = CircleShape,
                            modifier = Modifier.size(96.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = page.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(52.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Surface(
                            color = Color.White.copy(alpha = 0.9f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = page.badgeLabel,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = page.gradientColors[0],
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(36.dp))

                Text(
                    text = page.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = page.description,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        }
    }
}
