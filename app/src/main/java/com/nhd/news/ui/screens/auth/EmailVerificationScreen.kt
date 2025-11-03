package com.nhd.news.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmailVerificationScreen(
    modifier: Modifier = Modifier,
    email: String = "",
    onNavigateBack: () -> Unit = {},
    onVerificationSuccess: () -> Unit = {},
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    
    var verificationCode by remember { mutableStateOf("") }
    
    // Handle verification success
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage?.contains("xác thực thành công", ignoreCase = true) == true) {
            onVerificationSuccess()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Xác thực Email") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
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
            val successMsg = uiState.successMessage
            if (successMsg != null && !successMsg.contains("xác thực thành công", ignoreCase = true)) {
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    action = {
                        TextButton(onClick = { viewModel.clearSuccessMessage() }) {
                            Text("Đóng")
                        }
                    }
                ) {
                    Text(successMsg)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Email icon
            Icon(
                imageVector = Icons.Default.Email,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Title
            Text(
                text = "Xác thực Email",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Chúng tôi đã gửi email xác thực đến",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (email.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = email,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Vui lòng kiểm tra email và nhập mã xác thực 6 số bên dưới.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Verification code field - 6 digits
            OutlinedTextField(
                value = verificationCode,
                onValueChange = { newValue ->
                    // Only allow digits and max 6 characters
                    if (newValue.length <= 6 && newValue.all { it.isDigit() }) {
                        verificationCode = newValue
                        // Auto-verify when 6 digits entered
                        if (newValue.length == 6) {
                            focusManager.clearFocus()
                            viewModel.verifyEmail(newValue)
                        }
                    }
                },
                label = { Text("Mã xác thực 6 số") },
                placeholder = { Text("000000") },
                leadingIcon = {
                    Icon(Icons.Default.Check, "Mã xác thực")
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (verificationCode.length == 6) {
                            viewModel.verifyEmail(verificationCode)
                        }
                    }
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                supportingText = {
                    Text("${verificationCode.length}/6 số")
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Verify button
            Button(
                onClick = {
                    viewModel.verifyEmail(verificationCode)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = verificationCode.length == 6 && !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Xác thực", style = MaterialTheme.typography.titleMedium)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resend email
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Không nhận được email?",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = {
                        if (email.isNotBlank()) {
                            viewModel.resendVerification(email)
                        }
                    },
                    enabled = email.isNotBlank() && !uiState.isLoading
                ) {
                    Text("Gửi lại")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "💡 Lưu ý",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Kiểm tra cả hộp thư spam/junk\n" +
                              "• Mã xác thực có hiệu lực trong 15 phút\n" +
                              "• Bạn cần xác thực email để thích và bình luận bài viết",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

