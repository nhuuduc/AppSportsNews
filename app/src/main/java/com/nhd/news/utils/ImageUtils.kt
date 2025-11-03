package com.nhd.news.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.min

/**
 * Utility class để xử lý crop và compress ảnh
 */
object ImageUtils {
    
    /**
     * Compress ảnh để giảm kích thước file trước khi upload
     * @param context Context
     * @param uri URI của ảnh gốc
     * @param maxWidth Chiều rộng tối đa (default: 1024px)
     * @param maxHeight Chiều cao tối đa (default: 1024px)
     * @param quality Chất lượng nén (0-100, default: 85)
     * @return URI của ảnh đã nén
     */
    fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1024,
        maxHeight: Int = 1024,
        quality: Int = 85
    ): Uri? {
        try {
            // Đọc ảnh từ URI
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            // Tính toán kích thước mới
            val ratio = min(
                maxWidth.toFloat() / originalBitmap.width,
                maxHeight.toFloat() / originalBitmap.height
            )
            
            val newWidth = (originalBitmap.width * ratio).toInt()
            val newHeight = (originalBitmap.height * ratio).toInt()
            
            // Resize bitmap
            val resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, newWidth, newHeight, true)
            
            // Lưu vào file tạm
            val tempFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
            }
            
            // Cleanup
            originalBitmap.recycle()
            resizedBitmap.recycle()
            
            return Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Crop ảnh thành hình vuông từ giữa
     * @param context Context
     * @param uri URI của ảnh gốc
     * @return URI của ảnh đã crop
     */
    fun cropSquare(
        context: Context,
        uri: Uri
    ): Uri? {
        try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return null
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            val width = originalBitmap.width
            val height = originalBitmap.height
            val size = min(width, height)
            
            val x = (width - size) / 2
            val y = (height - size) / 2
            
            val croppedBitmap = Bitmap.createBitmap(originalBitmap, x, y, size, size)
            
            val tempFile = File(context.cacheDir, "cropped_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out ->
                croppedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            originalBitmap.recycle()
            croppedBitmap.recycle()
            
            return Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Crop ảnh thành hình tròn
     * @param context Context
     * @param uri URI của ảnh gốc
     * @param size Kích thước đầu ra (default: 500px)
     * @return URI của ảnh đã crop thành hình tròn
     */
    fun cropCircular(
        context: Context,
        uri: Uri,
        size: Int = 500
    ): Uri? {
        try {
            // Đầu tiên crop thành hình vuông
            val squareUri = cropSquare(context, uri) ?: return null
            
            val inputStream = context.contentResolver.openInputStream(squareUri) ?: return null
            val squareBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            // Resize to target size
            val resizedBitmap = Bitmap.createScaledBitmap(squareBitmap, size, size, true)
            
            // Tạo bitmap tròn
            val circularBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(circularBitmap)
            
            val paint = Paint()
            paint.isAntiAlias = true
            
            val rect = Rect(0, 0, size, size)
            val rectF = RectF(rect)
            
            canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint)
            
            paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
            canvas.drawBitmap(resizedBitmap, rect, rect, paint)
            
            // Lưu file
            val tempFile = File(context.cacheDir, "circular_${System.currentTimeMillis()}.png")
            FileOutputStream(tempFile).use { out ->
                circularBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            
            // Cleanup
            squareBitmap.recycle()
            resizedBitmap.recycle()
            circularBitmap.recycle()
            
            // Xóa file tạm square
            File(squareUri.path!!).delete()
            
            return Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Crop và compress ảnh cho avatar (tối ưu cho upload)
     * - Nếu ảnh đã được crop từ AvatarEditorDialog (PNG tròn), trả về luôn
     * - Ngược lại: Crop thành hình vuông, resize về 512x512, compress với quality 85
     */
    fun prepareAvatarForUpload(
        context: Context,
        uri: Uri
    ): Uri? {
        try {
            // Kiểm tra xem có phải ảnh đã crop từ AvatarEditorDialog không
            // (file có prefix "avatar_cropped_" và là PNG)
            val path = uri.path
            if (path != null && path.contains("avatar_cropped_") && path.endsWith(".png")) {
                // Đây là ảnh đã được crop và xử lý đúng từ AvatarEditorDialog
                // Không cần xử lý thêm, return luôn
                return uri
            }
            
            // Crop square
            val squareUri = cropSquare(context, uri) ?: return null
            
            // Resize và compress
            val inputStream = context.contentResolver.openInputStream(squareUri) ?: return null
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            val targetSize = 512
            val resizedBitmap = Bitmap.createScaledBitmap(bitmap, targetSize, targetSize, true)
            
            val tempFile = File(context.cacheDir, "avatar_${System.currentTimeMillis()}.jpg")
            FileOutputStream(tempFile).use { out ->
                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
            }
            
            bitmap.recycle()
            resizedBitmap.recycle()
            
            // Cleanup
            File(squareUri.path!!).delete()
            
            return Uri.fromFile(tempFile)
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }
    
    /**
     * Xóa file cache tạm
     */
    fun cleanupTempFiles(context: Context) {
        val cacheDir = context.cacheDir
        cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("compressed_") ||
                file.name.startsWith("cropped_") ||
                file.name.startsWith("circular_") ||
                file.name.startsWith("avatar_cropped_") ||
                file.name.startsWith("avatar_")
            ) {
                file.delete()
            }
        }
    }
}

