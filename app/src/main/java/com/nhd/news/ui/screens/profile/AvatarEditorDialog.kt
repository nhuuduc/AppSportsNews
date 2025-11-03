package com.nhd.news.ui.screens.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

/**
 * Dialog để chỉnh sửa avatar với khả năng pan và zoom
 * Hiển thị khung tròn overlay để người dùng điều chỉnh vị trí và kích thước ảnh
 */
@Composable
fun AvatarEditorDialog(
    imageUri: Uri,
    onDismiss: () -> Unit,
    onConfirm: (Uri) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    var isProcessing by remember { mutableStateOf(false) }
    
    // Track image and container dimensions for boundary checking
    var imageSizePx by remember { mutableStateOf<androidx.compose.ui.geometry.Size?>(null) }
    var containerSizePx by remember { mutableStateOf(0f) }
    
    // Hàm để áp dụng boundary constraints
    fun applyBoundaryConstraints(
        newScale: Float,
        newOffsetX: Float,
        newOffsetY: Float
    ): Pair<Float, Float> {
        var constrainedOffsetX = newOffsetX
        var constrainedOffsetY = newOffsetY
        
        imageSizePx?.let { imageSize ->
            if (containerSizePx > 0f) {
                val circleRadius = containerSizePx * 0.4f
                
                // Calculate scaled image dimensions (Fit mode - before scale transform)
                val imageAspect = imageSize.width / imageSize.height
                
                val baseImageWidth: Float
                val baseImageHeight: Float
                
                if (imageAspect > 1f) {
                    // Image is wider - fit to height
                    baseImageHeight = containerSizePx
                    baseImageWidth = containerSizePx * imageAspect
                } else {
                    // Image is taller - fit to width
                    baseImageWidth = containerSizePx
                    baseImageHeight = containerSizePx / imageAspect
                }
                
                // Calculate base image center position (before any transform)
                val baseImageCenterX = containerSizePx / 2f
                val baseImageCenterY = containerSizePx / 2f
                
                // Apply scale and translation to get final dimensions and center
                val finalImageWidth = baseImageWidth * newScale
                val finalImageHeight = baseImageHeight * newScale
                
                // Final center after translation (graphicsLayer applies translation to center)
                val finalImageCenterX = baseImageCenterX + newOffsetX
                val finalImageCenterY = baseImageCenterY + newOffsetY
                
                // Calculate circle bounds
                val circleCenterX = containerSizePx / 2f
                val circleCenterY = containerSizePx / 2f
                val circleLeft = circleCenterX - circleRadius
                val circleRight = circleCenterX + circleRadius
                val circleTop = circleCenterY - circleRadius
                val circleBottom = circleCenterY + circleRadius
                
                // Calculate the edges of the transformed image (from center)
                val imageLeft = finalImageCenterX - (finalImageWidth / 2f)
                val imageRight = finalImageCenterX + (finalImageWidth / 2f)
                val imageTop = finalImageCenterY - (finalImageHeight / 2f)
                val imageBottom = finalImageCenterY + (finalImageHeight / 2f)
                
                // Constrain offsets to ensure image covers the entire circle
                // Image left edge must be at or before circle left edge
                if (imageLeft > circleLeft) {
                    constrainedOffsetX = newOffsetX - (imageLeft - circleLeft)
                }
                // Image right edge must be at or after circle right edge
                if (imageRight < circleRight) {
                    constrainedOffsetX = newOffsetX + (circleRight - imageRight)
                }
                // Image top edge must be at or before circle top edge
                if (imageTop > circleTop) {
                    constrainedOffsetY = newOffsetY - (imageTop - circleTop)
                }
                // Image bottom edge must be at or after circle bottom edge
                if (imageBottom < circleBottom) {
                    constrainedOffsetY = newOffsetY + (circleBottom - imageBottom)
                }
            }
        }
        
        return Pair(constrainedOffsetX, constrainedOffsetY)
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Hủy",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Text(
                            text = "Chỉnh sửa ảnh đại diện",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        IconButton(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    try {
                                        val croppedUri = cropCircularAvatar(
                                            context = context,
                                            imageUri = imageUri,
                                            scale = scale,
                                            offsetX = offsetX,
                                            offsetY = offsetY,
                                            containerSizePx = containerSizePx
                                        )
                                        onConfirm(croppedUri)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                        // Nếu lỗi, trả về URI gốc
                                        onConfirm(imageUri)
                                    } finally {
                                        isProcessing = false
                                    }
                                }
                            },
                            enabled = !isProcessing
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = "Xác nhận",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                // Image editor area
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    // Container để clip ảnh thành hình tròn
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(CircleShape)
                            .onGloballyPositioned { coordinates ->
                                containerSizePx = coordinates.size.width.toFloat()
                            }
                    ) {
                        // Background image with pan and zoom
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Avatar để chỉnh sửa",
                            onSuccess = { state ->
                                // Get original image dimensions
                                imageSizePx = androidx.compose.ui.geometry.Size(
                                    width = state.painter.intrinsicSize.width,
                                    height = state.painter.intrinsicSize.height
                                )
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                ),
                            contentScale = ContentScale.Fit
                        )
                    }
                    
                    // Circular overlay with gesture detection
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    val newScale = (scale * zoom).coerceIn(1f, 4f)
                                    
                                    // Calculate new offsets
                                    val newOffsetX = offsetX + pan.x
                                    val newOffsetY = offsetY + pan.y
                                    
                                    // Apply boundary constraints
                                    val (constrainedX, constrainedY) = applyBoundaryConstraints(
                                        newScale, newOffsetX, newOffsetY
                                    )
                                    
                                    scale = newScale
                                    offsetX = constrainedX
                                    offsetY = constrainedY
                                }
                            }
                    ) {
                        CircularCropOverlay(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                
                // Controls
                Surface(
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Instructions
                        Text(
                            text = "Kéo để di chuyển • Véo để thu phóng",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Zoom slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Zoom",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(50.dp)
                            )
                            
                            Slider(
                                value = scale,
                                onValueChange = { newScale ->
                                    // Apply boundary constraints when scale changes
                                    val (constrainedX, constrainedY) = applyBoundaryConstraints(
                                        newScale, offsetX, offsetY
                                    )
                                    scale = newScale
                                    offsetX = constrainedX
                                    offsetY = constrainedY
                                },
                                valueRange = 1f..4f,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Text(
                                text = "${(scale * 100).toInt()}%",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(50.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Reset button
                        OutlinedButton(
                            onClick = {
                                scale = 1f
                                offsetX = 0f
                                offsetY = 0f
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Đặt lại vị trí")
                        }
                    }
                }
            }
        }
    }
}

/**
 * Composable vẽ overlay với khung tròn ở giữa
 * Phần bên ngoài khung tròn sẽ bị làm tối
 */
@Composable
fun CircularCropOverlay(
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .graphicsLayer(alpha = 0.99f)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val circleRadius = minOf(canvasWidth, canvasHeight) * 0.4f
        val center = Offset(canvasWidth / 2f, canvasHeight / 2f)
        
        // Draw dark overlay everywhere
        drawRect(
            color = Color.Black.copy(alpha = 0.7f),
            size = Size(canvasWidth, canvasHeight)
        )
        
        // Clear circle in the middle to show image
        drawCircle(
            color = Color.Transparent,
            radius = circleRadius,
            center = center,
            blendMode = androidx.compose.ui.graphics.BlendMode.Clear
        )
        
        // Draw white circle border
        drawCircle(
            color = Color.White,
            radius = circleRadius,
            center = center,
            style = Stroke(width = 3f)
        )
        
        // Draw grid lines for better alignment
        val gridColor = Color.White.copy(alpha = 0.3f)
        
        // Vertical lines
        drawLine(
            color = gridColor,
            start = Offset(center.x - circleRadius / 3, center.y - circleRadius),
            end = Offset(center.x - circleRadius / 3, center.y + circleRadius),
            strokeWidth = 1f
        )
        drawLine(
            color = gridColor,
            start = Offset(center.x + circleRadius / 3, center.y - circleRadius),
            end = Offset(center.x + circleRadius / 3, center.y + circleRadius),
            strokeWidth = 1f
        )
        
        // Horizontal lines
        drawLine(
            color = gridColor,
            start = Offset(center.x - circleRadius, center.y - circleRadius / 3),
            end = Offset(center.x + circleRadius, center.y - circleRadius / 3),
            strokeWidth = 1f
        )
        drawLine(
            color = gridColor,
            start = Offset(center.x - circleRadius, center.y + circleRadius / 3),
            end = Offset(center.x + circleRadius, center.y + circleRadius / 3),
            strokeWidth = 1f
        )
    }
}

/**
 * Crop ảnh thành hình tròn dựa trên các thông số zoom và pan
 */
private suspend fun cropCircularAvatar(
    context: Context,
    imageUri: Uri,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    containerSizePx: Float
): Uri = withContext(Dispatchers.IO) {
    // 1. Load bitmap gốc
    val inputStream = context.contentResolver.openInputStream(imageUri)
        ?: throw IllegalArgumentException("Cannot open image URI")
    
    val originalBitmap = BitmapFactory.decodeStream(inputStream)
    inputStream.close()
    
    val originalWidth = originalBitmap.width.toFloat()
    val originalHeight = originalBitmap.height.toFloat()
    
    // 2. Tính kích thước display của ảnh trong preview (ContentScale.Fit)
    val imageAspect = originalWidth / originalHeight
    val displayWidth: Float
    val displayHeight: Float
    
    if (imageAspect > 1f) {
        // Image is wider - fit to height
        displayHeight = containerSizePx
        displayWidth = containerSizePx * imageAspect
    } else {
        // Image is taller - fit to width
        displayWidth = containerSizePx
        displayHeight = containerSizePx / imageAspect
    }
    
    // 3. Tính scale factor để convert từ display sang original
    val scaleX = originalWidth / displayWidth
    val scaleY = originalHeight / displayHeight
    
    // 4. Tính circle radius trong container coordinates (80% của container)
    val circleRadiusDisplay = containerSizePx * 0.4f
    
    // 5. Circle center trong container coordinates
    val circleCenterInContainerX = containerSizePx / 2f
    val circleCenterInContainerY = containerSizePx / 2f
    
    // 6. Image được đặt ở center của container (ContentScale.Fit)
    // Tính padding của image trong container
    val imagePaddingLeft = (containerSizePx - displayWidth) / 2f
    val imagePaddingTop = (containerSizePx - displayHeight) / 2f
    
    // 7. Circle center trong IMAGE coordinates (không phải container!)
    // graphicsLayer transform với origin = center của IMAGE
    val circleCenterInImageX = circleCenterInContainerX - imagePaddingLeft
    val circleCenterInImageY = circleCenterInContainerY - imagePaddingTop
    
    // 8. Image center trong IMAGE coordinates
    val imageCenterX = displayWidth / 2f
    val imageCenterY = displayHeight / 2f
    
    // 9. Reverse transform: tìm điểm trong image (trước transform) tương ứng với circle center
    // Transform công thức: newPos = (pos - imageCenter) * scale + offset + imageCenter
    // Reverse: pos = (newPos - imageCenter - offset) / scale + imageCenter
    val cropCenterXDisplay = (circleCenterInImageX - imageCenterX - offsetX) / scale + imageCenterX
    val cropCenterYDisplay = (circleCenterInImageY - imageCenterY - offsetY) / scale + imageCenterY
    
    // 10. Tính crop radius trong display coordinates (trước transform)
    val cropRadiusDisplay = circleRadiusDisplay / scale
    
    // 11. Convert sang original image coordinates
    val cropCenterX = cropCenterXDisplay * scaleX
    val cropCenterY = cropCenterYDisplay * scaleY
    val cropRadiusX = cropRadiusDisplay * scaleX
    val cropRadiusY = cropRadiusDisplay * scaleY
    val cropRadius = (cropRadiusX + cropRadiusY) / 2f // Average để đảm bảo hình tròn
    
    // 12. Tính crop bounds trong original image
    val left = (cropCenterX - cropRadius).coerceIn(0f, originalWidth)
    val top = (cropCenterY - cropRadius).coerceIn(0f, originalHeight)
    val right = (cropCenterX + cropRadius).coerceIn(0f, originalWidth)
    val bottom = (cropCenterY + cropRadius).coerceIn(0f, originalHeight)
    
    // 3. Crop ảnh vuông
    val croppedWidth = (right - left).toInt()
    val croppedHeight = (bottom - top).toInt()
    val size = min(croppedWidth, croppedHeight)
    
    val croppedBitmap = Bitmap.createBitmap(
        originalBitmap,
        left.toInt(),
        top.toInt(),
        size,
        size
    )
    
    // 4. Tạo bitmap tròn với nền trong suốt
    val outputSize = 512 // Kích thước output cố định
    
    // Scale ảnh đã crop thành vuông
    val scaledCropped = Bitmap.createScaledBitmap(croppedBitmap, outputSize, outputSize, true)
    
    // Tạo bitmap output final với hình tròn hoàn hảo
    val finalBitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888)
    val finalCanvas = android.graphics.Canvas(finalBitmap)
    
    // Tạo paint với anti-aliasing cao
    val paint = Paint().apply {
        isAntiAlias = true
        isFilterBitmap = true
        isDither = true
    }
    
    // Vẽ hình tròn làm mask TRƯỚC
    finalCanvas.drawCircle(outputSize / 2f, outputSize / 2f, outputSize / 2f, paint)
    
    // Áp dụng SRC_IN để chỉ giữ phần ảnh trong vùng tròn
    paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
    finalCanvas.drawBitmap(scaledCropped, 0f, 0f, paint)
    
    // 5. Lưu vào file tạm
    val cacheDir = context.cacheDir
    val outputFile = File(cacheDir, "avatar_cropped_${System.currentTimeMillis()}.png")
    
    FileOutputStream(outputFile).use { out ->
        finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
    
    // 6. Giải phóng bộ nhớ
    originalBitmap.recycle()
    croppedBitmap.recycle()
    scaledCropped.recycle()
    finalBitmap.recycle()
    
    // 7. Trả về URI của file đã crop
    Uri.fromFile(outputFile)
}

