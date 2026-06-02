package com.example.productsalessupportclient.presentation.role

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.Hyphens
import androidx.compose.ui.text.style.LineBreak
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.productsalessupportclient.data.repository.AuthSession

data class RoleMenuItem(
    val title: String,
    val route: String,
    val screen: @Composable () -> Unit
)

@Composable
fun RoleShell(
    session: AuthSession,
    menuItems: List<RoleMenuItem>,
    onLogout: () -> Unit
) {
    val navController = rememberNavController()
    var drawerOpen by rememberSaveable { mutableStateOf(false) }
    var profileExpanded by rememberSaveable { mutableStateOf(false) }

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
        ?: menuItems.first().route

    val currentTitle = menuItems.firstOrNull { it.route == currentRoute }?.title
        ?: menuItems.first().title

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val drawerTargetWidth = maxWidth * 0.25f
        val drawerWidth by animateDpAsState(
            targetValue = if (drawerOpen) drawerTargetWidth else 0.dp,
            label = "drawerWidth"
        )

        Row(modifier = Modifier.fillMaxSize()) {

            if (drawerWidth > 0.dp) {
                Column(
                    modifier = Modifier
                        .width(drawerWidth)
                        .fillMaxHeight()
                        .background(Color(0xFFF8F5FF))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { profileExpanded = !profileExpanded },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(25.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val fullName = session.profile.fullName?.trim()
                        ?.takeIf { it.isNotBlank() }
                        ?: "Пользователь"

                    val fullNameLines =
                        fullName.split(" ").filter { it.isNotBlank() }.size.coerceIn(1, 3)

                    BasicText(
                        text = fullName,
                        maxLines = fullNameLines,
                        autoSize = TextAutoSize.StepBased(
                            minFontSize = 6.sp,
                            maxFontSize = 14.sp,
                            stepSize = 1.sp
                        ),
                        softWrap = true
                    )

                    if (profileExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))

                        BasicText(
                            text = session.profile.email,
                            maxLines = 2,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 6.sp,
                                maxFontSize = 12.sp,
                                stepSize = 1.sp
                            ),
                            softWrap = true
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        BasicText(
                            text = session.profile.phone ?: "Телефон не указан",
                            maxLines = 1,
                            autoSize = TextAutoSize.StepBased(
                                minFontSize = 6.sp,
                                maxFontSize = 12.sp,
                                stepSize = 1.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    menuItems.forEach { item ->
                        val selected = currentRoute == item.route

                        Button(
                            onClick = {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(
                                horizontal = 1.dp,
                                vertical = 2.dp
                            ),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else Color(0xFFBCAAF9),
                                contentColor = Color.White
                            )
                        ) {
                            val title = item.title.trim()
                            val wordsCount = title.split(Regex("\\s+")).filter { it.isNotBlank() }.size
                            val linesCount = wordsCount.coerceIn(1, 5)

                            Text(
                                text = title,
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = linesCount,
                                softWrap = true,
                                overflow = TextOverflow.Ellipsis,
                                autoSize = TextAutoSize.StepBased(
                                    minFontSize = 4.sp,
                                    maxFontSize = 8.sp,
                                    stepSize = 1.sp
                                ),
                                style = TextStyle(
                                    textAlign = TextAlign.Center,
                                    lineBreak = LineBreak.Paragraph,
                                    hyphens = Hyphens.None
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Button(
                            onClick = {
                                profileExpanded = false
                                drawerOpen = false
                                onLogout()
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(
                                horizontal = 10.dp,
                                vertical = 4.dp
                            ),
                            modifier = Modifier
                                .padding(start = 2.dp, bottom = 2.dp)
                                .wrapContentWidth()
                                .width(48.dp)
                                .height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                                contentDescription = "Выйти",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .fillMaxHeight()
                        .background(Color(0xFFE3DDFE))
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.White)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, top = 8.dp, end = 16.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { drawerOpen = !drawerOpen },
                        modifier = Modifier.testTag("menu_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Menu,
                            contentDescription = "Меню"
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = currentTitle,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Medium
                    )
                }

                NavHost(
                    navController = navController,
                    startDestination = menuItems.first().route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    menuItems.forEach { item ->
                        composable(item.route) {
                            item.screen()
                        }
                    }
                }
            }
        }
    }
}