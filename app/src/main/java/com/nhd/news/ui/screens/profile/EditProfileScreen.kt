package com.nhd.news.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.nhd.news.data.api.ApiConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var expandedGender by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var showAvatarEditor by remember { mutableStateOf(false) }
    var tempImageUri by remember { mutableStateOf<Uri?>(null) }
    
    // Load thông tin cũ khi profile thay đổi
    LaunchedEffect(uiState.profile) {
        uiState.profile?.let { profile ->
            fullName = profile.fullName ?: ""
            phone = profile.phone ?: ""
            gender = profile.gender ?: ""
        }
    }
    
    // Image picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // Lưu URI tạm và mở editor
            tempImageUri = it
            showAvatarEditor = true
        }
    }
    
    // Hiển thị Avatar Editor Dialog
    if (showAvatarEditor && tempImageUri != null) {
        AvatarEditorDialog(
            imageUri = tempImageUri!!,
            onDismiss = {
                showAvatarEditor = false
                tempImageUri = null
            },
            onConfirm = { confirmedUri ->
                selectedImageUri = confirmedUri
                showAvatarEditor = false
                viewModel.uploadAvatar(confirmedUri)
                tempImageUri = null
            }
        )
    }
    
    val genderOptions = listOf(
        "male" to "Nam",
        "female" to "Nữ",
        "other" to "Lẩu Gà "
    )
    
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            onNavigateBack()
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chỉnh sửa thông tin") },
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
            // Avatar section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                // Avatar image
                val avatarUrl = if (selectedImageUri != null) {
                    selectedImageUri
                } else {
                    ApiConfig.getAbsoluteImageUrl(uiState.profile?.avatarUrl) ?: "https://via.placeholder.com/120"
                }
                
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar",
                    modifier = Modifier
                        .size(120.dp)
                        .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentScale = ContentScale.Crop,
                    alignment = androidx.compose.ui.Alignment.Center
                )
                
                // Camera icon overlay
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .align(Alignment.BottomEnd),
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Chọn ảnh",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
            
            Text(
                text = "Chạm để thay đổi ảnh đại diện",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Full name
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Họ và tên") },
                leadingIcon = {
                    Icon(Icons.Default.Person, "Họ và tên")
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Phone
            OutlinedTextField(
                value = phone,
                onValueChange = { newValue ->
                    // Chỉ cho phép nhập số và tối đa 10 ký tự
                    if (newValue.length <= 10 && newValue.all { it.isDigit() }) {
                        phone = newValue
                    }
                },
                label = { Text("Số điện thoại") },
                leadingIcon = {
                    Icon(Icons.Default.Phone, "Số điện thoại")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading,
                supportingText = {
                    Text("${phone.length}/10 ký tự")
                },
                isError = phone.isNotEmpty() && phone.length != 10
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Gender
            ExposedDropdownMenuBox(
                expanded = expandedGender,
                onExpandedChange = { expandedGender = !expandedGender }
            ) {
                OutlinedTextField(
                    value = genderOptions.find { it.first == gender }?.second ?: "Chọn giới tính",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Giới tính") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, "Giới tính")
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGender)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    enabled = !uiState.isLoading
                )
                
                ExposedDropdownMenu(
                    expanded = expandedGender,
                    onDismissRequest = { expandedGender = false }
                ) {
                    genderOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                gender = value
                                expandedGender = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Save button
            Button(
                onClick = {
                    viewModel.updateProfile(
                        fullName = fullName,
                        phone = phone,
                        gender = gender.ifEmpty { null }
                    )
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
                    Text("Lưu thay đổi")
                }
            }
        }
    }
}

