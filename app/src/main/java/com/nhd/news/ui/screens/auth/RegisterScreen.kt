package com.nhd.news.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nhd.news.R
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    onNavigateToLogin: () -> Unit = {},
    onRegisterSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()
    
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    
    // Handle register success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null && !uiState.isLoading) {
            // Show success dialog or navigate
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đăng ký") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        snackbarHost = {
            if (uiState.error != null) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    action = {
                        TextButton(onClick = { viewModel.clearError() }) {
                            Text("Đóng")
                        }
                    }
                ) {
                    Text(uiState.error ?: "")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Title
            Text(
                text = "Tạo tài khoản mới",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Đăng ký để thích và bình luận bài viết",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Email field
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email *") },
                placeholder = { Text("example@email.com") },
                leadingIcon = { 
                    Icon(Icons.Default.Email, contentDescription = "Email") 
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Username field
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Tên đăng nhập *") },
                placeholder = { Text("Nhập tên đăng nhập") },
                leadingIcon = { 
                    Icon(Icons.Default.Person, contentDescription = "Tên đăng nhập") 
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Password field
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu *") },
                placeholder = { Text("Tối thiểu 6 ký tự") },
                leadingIcon = { 
                    Icon(Icons.Default.Lock, contentDescription = "Mật khẩu") 
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (passwordVisible) 
                                    R.drawable.ic_visibility 
                                else 
                                    R.drawable.ic_visibility_off
                            ),
                            contentDescription = if (passwordVisible) 
                                "Ẩn mật khẩu" 
                            else 
                                "Hiện mật khẩu"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                ),
                supportingText = if (password.isNotBlank() && password.length < 6) {
                    { Text("Mật khẩu phải có ít nhất 6 ký tự") }
                } else null,
                isError = password.isNotBlank() && password.length < 6,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Register button
            Button(
                onClick = {
                    viewModel.register(
                        email = email,
                        password = password,
                        username = username
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = email.isNotBlank() && 
                         username.isNotBlank() && 
                         password.isNotBlank() && 
                         password.length >= 6 &&
                         !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Đăng ký", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Login link
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Đã có tài khoản?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNavigateToLogin) {
                    Text("Đăng nhập")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Auto navigate on register success
    LaunchedEffect(uiState.isLoggedIn) {
        if (uiState.isLoggedIn && uiState.successMessage != null) {
            viewModel.clearSuccessMessage()
            onRegisterSuccess()
        }
    }
    
    // Show error dialog if auto-login failed after registration
    if (!uiState.isLoggedIn && uiState.successMessage != null && !uiState.isLoading) {
        AlertDialog(
            onDismissRequest = { viewModel.clearSuccessMessage() },
            title = { Text("Đăng ký thành công") },
            text = { Text(uiState.successMessage ?: "") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearSuccessMessage()
                        onNavigateToLogin()
                    }
                ) {
                    Text("Đăng nhập")
                }
            }
        )
    }
}

