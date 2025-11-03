package com.nhd.news.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhd.news.data.api.ApiConfig
import com.nhd.news.ui.screens.auth.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    scrollToTopTrigger: Int = 0,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToVerification: () -> Unit = {},
    onNavigateToEditProfile: () -> Unit = {},
    onNavigateToChangePassword: () -> Unit = {},
    onNavigateToMyPosts: () -> Unit = {},
    onNavigateToSavedArticles: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()
    
    // Scroll to top khi double-tap
    LaunchedEffect(scrollToTopTrigger) {
        if (scrollToTopTrigger > 0) {
            coroutineScope.launch {
                scrollState.animateScrollTo(0)
            }
        }
    }
    
    // Auto refresh user info when screen is opened
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn) {
            viewModel.refreshUser()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tài khoản") },
                actions = {
                    if (uiState.isLoggedIn) {
                        IconButton(onClick = { /* Settings */ }) {
                            Icon(Icons.Default.Settings, "Cài đặt")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoggedIn && uiState.user != null) {
                // Logged in view
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(16.dp)
                ) {
                // Profile header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(100.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            val avatarUrl = ApiConfig.getAbsoluteImageUrl(uiState.user?.avatarUrl)
                            if (avatarUrl != null) {
                                AsyncImage(
                                    model = avatarUrl,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Default avatar",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Name
                        Text(
                            text = uiState.user?.fullName ?: uiState.user?.username ?: "",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Username
                        if (uiState.user?.fullName != null) {
                            Text(
                                text = "@${uiState.user?.username}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Email with verification status
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.user?.email ?: "",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            if (uiState.user?.emailVerified == true) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                AssistChip(
                                    onClick = onNavigateToVerification,
                                    label = { Text("Xác thực email") },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.Warning,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Edit profile button
                        OutlinedButton(
                            onClick = onNavigateToEditProfile,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, "Chỉnh sửa", Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Chỉnh sửa thông tin")
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Menu items
                ProfileMenuItem(
                    icon = Icons.Default.Create,
                    title = "Bài viết của tôi",
                    subtitle = "Quản lý bài viết",
                    onClick = onNavigateToMyPosts
                )
                
                ProfileMenuItem(
                    icon = Icons.Default.Favorite,
                    title = "Bài viết đã like",
                    subtitle = "Các bài viết yêu thích",
                    onClick = onNavigateToSavedArticles
                )
                
                ProfileMenuItem(
                    icon = Icons.Default.Notifications,
                    title = "Thông báo",
                    subtitle = "Cài đặt thông báo",
                    onClick = { /* TODO */ }
                )
                
                ProfileMenuItem(
                    icon = Icons.Default.Lock,
                    title = "Đổi mật khẩu",
                    subtitle = "Thay đổi mật khẩu",
                    onClick = onNavigateToChangePassword
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Logout button
                OutlinedButton(
                    onClick = { viewModel.logout() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.ExitToApp, "Đăng xuất")
                    Spacer(Modifier.width(8.dp))
                        Text("Đăng xuất")
                    }
                }
            } else {
                // Not logged in view
                Column(
                    modifier = modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.size(120.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Chưa đăng nhập",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Đăng nhập để sử dụng đầy đủ tính năng",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Button(
                    onClick = onNavigateToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                    ) {
                        Text("Đăng nhập", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Go",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
