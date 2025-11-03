package com.nhd.news.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nhd.news.ui.screens.articles.ArticlesScreen
import com.nhd.news.ui.screens.articledetail.ArticleDetailScreen
import com.nhd.news.ui.screens.auth.LoginScreen
import com.nhd.news.ui.screens.auth.RegisterScreen
import com.nhd.news.ui.screens.auth.EmailVerificationScreen
import com.nhd.news.ui.screens.home.HomeScreen
import com.nhd.news.ui.screens.matches.MatchesScreen
import com.nhd.news.ui.screens.profile.ProfileScreen
import com.nhd.news.ui.screens.profile.EditProfileScreen
import com.nhd.news.ui.screens.profile.ChangePasswordScreen
import com.nhd.news.ui.screens.profile.SavedArticlesScreen
import com.nhd.news.ui.screens.post.CreatePostScreen
import com.nhd.news.ui.screens.post.MyPostsScreen
import com.nhd.news.ui.screens.search.SearchScreen
import com.nhd.news.ui.screens.videos.VideosScreen
import kotlinx.coroutines.delay

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Home : Screen("home", "Trang chủ", Icons.Default.Home)
    object Articles : Screen("articles", "Tin tức", Icons.Default.Info)
    object Matches : Screen("matches", "Trận đấu", Icons.Default.Star)
    object Videos : Screen("videos", "Video", Icons.Default.PlayArrow)
    object Profile : Screen("profile", "Tài khoản", Icons.Default.AccountCircle)
    object Search : Screen("search", "Tìm kiếm", Icons.Default.Home)
    object ArticleDetail : Screen("article/{articleId}", "Chi tiết tin tức", Icons.Default.Info)
    object Login : Screen("login", "Đăng nhập", Icons.Default.AccountCircle)
    object Register : Screen("register", "Đăng ký", Icons.Default.AccountCircle)
    object EmailVerification : Screen("email-verification?email={email}", "Xác thực Email", Icons.Default.Email)
    object EditProfile : Screen("edit-profile", "Chỉnh sửa thông tin", Icons.Default.Edit)
    object ChangePassword : Screen("change-password", "Đổi mật khẩu", Icons.Default.Lock)
    object SavedArticles : Screen("saved-articles", "Bài viết đã like", Icons.Default.Favorite)
    object CreatePost : Screen("create-post", "Tạo bài viết", Icons.Default.Create)
    object MyPosts : Screen("my-posts", "Bài viết của tôi", Icons.Default.Create)
}

@Composable
fun NewsNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val items = listOf(
        Screen.Home,
        Screen.Articles,
        Screen.Matches,
        Screen.Videos,
        Screen.Profile
    )

    // State để trigger scroll to top
    var scrollToTopTrigger by remember { mutableStateOf(0) }
    var lastClickedRoute by remember { mutableStateOf<String?>(null) }
    var lastClickTime by remember { mutableStateOf(0L) }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                
                items.forEach { screen ->
                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                    
                    NavigationBarItem(
                        icon = { 
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title
                            )
                        },
                        label = { Text(screen.title) },
                        selected = isSelected,
                        onClick = {
                            val currentTime = System.currentTimeMillis()
                            val currentRoute = currentDestination?.route
                            
                            // Kiểm tra double-tap: click vào tab đang active trong vòng 500ms
                            if (isSelected && currentRoute == lastClickedRoute && 
                                (currentTime - lastClickTime) < 500) {
                                // Double-tap detected - trigger scroll to top
                                scrollToTopTrigger++
                            } else if (!isSelected) {
                                // Navigate to different screen
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                            
                            lastClickedRoute = currentRoute
                            lastClickTime = currentTime
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    scrollToTopTrigger = scrollToTopTrigger,
                    onSearchClick = {
                        navController.navigate(Screen.Search.route)
                    },
                    onArticleClick = { articleId ->
                        navController.navigate("article/$articleId")
                    },
                    onCreatePostClick = {
                        navController.navigate(Screen.CreatePost.route)
                    }
                )
            }
            composable(Screen.Articles.route) {
                ArticlesScreen(
                    scrollToTopTrigger = scrollToTopTrigger,
                    onArticleClick = { articleId ->
                        navController.navigate("article/$articleId")
                    }
                )
            }
            composable(Screen.Matches.route) {
                MatchesScreen(
                    scrollToTopTrigger = scrollToTopTrigger
                )
            }
            composable(Screen.Videos.route) {
                VideosScreen(
                    scrollToTopTrigger = scrollToTopTrigger
                )
            }
            composable(Screen.Search.route) {
                SearchScreen(
                    onBackClick = {
                        navController.popBackStack()
                    },
                    onArticleClick = { articleId ->
                        navController.navigate("article/$articleId")
                    }
                )
            }
            composable(
                route = "article/{articleId}",
                arguments = listOf(navArgument("articleId") { type = NavType.IntType })
            ) { backStackEntry ->
                val articleId = backStackEntry.arguments?.getInt("articleId") ?: 0
                ArticleDetailScreen(
                    articleId = articleId,
                    onBackClick = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    scrollToTopTrigger = scrollToTopTrigger,
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route)
                    },
                    onNavigateToVerification = {
                        val email = "" // Get from user state
                        navController.navigate("email-verification?email=$email")
                    },
                    onNavigateToEditProfile = {
                        navController.navigate(Screen.EditProfile.route)
                    },
                    onNavigateToChangePassword = {
                        navController.navigate(Screen.ChangePassword.route)
                    },
                    onNavigateToMyPosts = {
                        navController.navigate(Screen.MyPosts.route)
                    },
                    onNavigateToSavedArticles = {
                        navController.navigate(Screen.SavedArticles.route)
                    }
                )
            }
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToRegister = {
                        navController.navigate(Screen.Register.route)
                    },
                    onLoginSuccess = {
                        // Navigate to Profile and clear login from backstack
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onNavigateToLogin = {
                        navController.popBackStack()
                    },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Profile.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.ChangePassword.route) {
                ChangePasswordScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(Screen.SavedArticles.route) {
                SavedArticlesScreen(
                    navController = navController
                )
            }
            // TODO: Tạo bài viết - sẽ cập nhật sau
            // composable(Screen.CreatePost.route) {
            //     CreatePostScreen(
            //         onNavigateBack = {
            //             navController.popBackStack()
            //         }
            //     )
            // }
            composable(Screen.MyPosts.route) {
                MyPostsScreen(
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onEditPost = { articleId ->
                        // TODO: Navigate to edit post screen
                        // navController.navigate("edit-post/$articleId")
                    }
                )
            }
            composable(
                route = "email-verification?email={email}",
                arguments = listOf(navArgument("email") { 
                    type = NavType.StringType
                    defaultValue = ""
                })
            ) { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                EmailVerificationScreen(
                    email = email,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onVerificationSuccess = {
                        navController.popBackStack(Screen.Profile.route, inclusive = false)
                    }
                )
            }
        }
    }
}
