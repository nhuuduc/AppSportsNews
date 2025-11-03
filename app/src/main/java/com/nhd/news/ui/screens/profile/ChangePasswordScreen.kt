package com.nhd.news.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.nhd.news.R
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    
    var oldPasswordVisible by remember { mutableStateOf(false) }
    var newPasswordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    var validationError by remember { mutableStateOf<String?>(null) }
    
    // Show success snackbar and navigate back
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            snackbarHostState.showSnackbar(
                message = uiState.successMessage ?: "Đổi mật khẩu thành công",
                duration = SnackbarDuration.Short
            )
            kotlinx.coroutines.delay(1000)
            onNavigateBack()
        }
    }
    
    // Show error snackbar
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            snackbarHostState.showSnackbar(
                message = uiState.error ?: "Có lỗi xảy ra",
                duration = SnackbarDuration.Short
            )
            viewModel.clearError()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đổi mật khẩu") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            // Old password
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { 
                    oldPassword = it
                    validationError = null
                },
                label = { Text("Mật khẩu cũ") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Mật khẩu cũ")
                },
                trailingIcon = {
                    IconButton(onClick = { oldPasswordVisible = !oldPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (oldPasswordVisible) 
                                    R.drawable.ic_visibility 
                                else 
                                    R.drawable.ic_visibility_off
                            ),
                            contentDescription = if (oldPasswordVisible) 
                                "Ẩn mật khẩu" 
                            else 
                                "Hiện mật khẩu"
                        )
                    }
                },
                visualTransformation = if (oldPasswordVisible) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // New password
            OutlinedTextField(
                value = newPassword,
                onValueChange = { 
                    newPassword = it
                    validationError = null
                },
                label = { Text("Mật khẩu mới") },
                placeholder = { Text("Tối thiểu 6 ký tự") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Mật khẩu mới")
                },
                trailingIcon = {
                    IconButton(onClick = { newPasswordVisible = !newPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (newPasswordVisible) 
                                    R.drawable.ic_visibility 
                                else 
                                    R.drawable.ic_visibility_off
                            ),
                            contentDescription = if (newPasswordVisible) 
                                "Ẩn mật khẩu" 
                            else 
                                "Hiện mật khẩu"
                        )
                    }
                },
                visualTransformation = if (newPasswordVisible) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true,
                supportingText = {
                    Text("Mật khẩu phải có ít nhất 6 ký tự")
                },
                isError = newPassword.isNotBlank() && newPassword.length < 6
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Confirm password
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { 
                    confirmPassword = it
                    validationError = null
                },
                label = { Text("Xác nhận mật khẩu mới") },
                placeholder = { Text("Nhập lại mật khẩu mới") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = "Xác nhận mật khẩu")
                },
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (confirmPasswordVisible) 
                                    R.drawable.ic_visibility 
                                else 
                                    R.drawable.ic_visibility_off
                            ),
                            contentDescription = if (confirmPasswordVisible) 
                                "Ẩn mật khẩu" 
                            else 
                                "Hiện mật khẩu"
                        )
                    }
                },
                visualTransformation = if (confirmPasswordVisible) 
                    VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                singleLine = true,
                isError = validationError != null,
                supportingText = if (validationError != null) {
                    { Text(validationError!!, color = MaterialTheme.colorScheme.error) }
                } else null
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Change password button
            Button(
                onClick = {
                    when {
                        oldPassword.isEmpty() -> {
                            validationError = "Vui lòng nhập mật khẩu cũ"
                        }
                        newPassword.isEmpty() -> {
                            validationError = "Vui lòng nhập mật khẩu mới"
                        }
                        newPassword.length < 6 -> {
                            validationError = "Mật khẩu mới phải có ít nhất 6 ký tự"
                        }
                        confirmPassword.isEmpty() -> {
                            validationError = "Vui lòng xác nhận mật khẩu mới"
                        }
                        newPassword != confirmPassword -> {
                            validationError = "Mật khẩu xác nhận không khớp"
                        }
                        oldPassword == newPassword -> {
                            validationError = "Mật khẩu mới phải khác mật khẩu cũ"
                        }
                        else -> {
                            validationError = null
                            viewModel.changePassword(oldPassword, newPassword)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Đổi mật khẩu")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Information card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Lưu ý khi đổi mật khẩu",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Mật khẩu mới phải có ít nhất 6 ký tự\n" +
                               "• Mật khẩu mới phải khác mật khẩu cũ\n" +
                               "• Sau khi đổi mật khẩu thành công, bạn vẫn giữ nguyên phiên đăng nhập hiện tại",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

