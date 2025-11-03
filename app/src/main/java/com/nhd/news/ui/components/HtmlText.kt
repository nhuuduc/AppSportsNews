package com.nhd.news.ui.components

import android.graphics.*
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ImageSpan
import android.util.TypedValue
import android.widget.TextView
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.viewinterop.AndroidView
import coil.ImageLoader
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@Composable
fun HtmlText(
    html: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    fontSize: TextUnit = MaterialTheme.typography.bodyLarge.fontSize,
    lineHeight: TextUnit = MaterialTheme.typography.bodyLarge.lineHeight
) {
    val context = LocalContext.current
    val imageLoader = remember { ImageLoader(context) }
    val scope = rememberCoroutineScope()
    val loadedImages = remember { mutableStateMapOf<String, Drawable>() }

    fun dp(value: Float): Int =
        TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, value, context.resources.displayMetrics
        ).roundToInt()

    /**
     * Placeholder nhỏ khi ĐANG TẢI: chấm tròn xám 24dp
     */
    fun makeTinyLoadingDot(): Drawable {
        val size = dp(24f)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val p = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG)
        p.color = AndroidColor.LTGRAY
        c.drawCircle(size / 2f, size / 2f, size / 3f, p)
        return BitmapDrawable(context.resources, bmp).apply { setBounds(0, 0, size, size) }
    }

    /**
     * Placeholder LỖI: icon tròn đỏ có dấu X trắng – 96dp, để thật nhỏ & gọn
     */
    fun makeTinyErrorIcon(): Drawable {
        val size = dp(96f)
        val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)

        // Nền trong suốt
        c.drawColor(AndroidColor.TRANSPARENT)

        // Vòng tròn đỏ
        val redPaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG)
        redPaint.color = AndroidColor.parseColor("#F04A4A")
        redPaint.style = AndroidPaint.Style.FILL
        c.drawCircle(size / 2f, size / 2f, size / 2.6f, redPaint)

        // Dấu X trắng
        val whitePaint = AndroidPaint(AndroidPaint.ANTI_ALIAS_FLAG)
        whitePaint.color = AndroidColor.WHITE
        whitePaint.strokeWidth = size / 14f
        whitePaint.style = AndroidPaint.Style.STROKE
        whitePaint.strokeCap = AndroidPaint.Cap.ROUND
        val r = size / 3.2f
        c.drawLine(size / 2f - r, size / 2f - r, size / 2f + r, size / 2f + r, whitePaint)
        c.drawLine(size / 2f - r, size / 2f + r, size / 2f + r, size / 2f - r, whitePaint)

        return BitmapDrawable(context.resources, bmp).apply { setBounds(0, 0, size, size) }
    }

    /**
     * ImageSpan căn giữa TextView (không ảnh hưởng text khác).
     */
    class CenteredImageSpan(private val view: TextView, d: Drawable) : ImageSpan(d, ALIGN_BASELINE) {
        override fun draw(
            canvas: Canvas,
            text: CharSequence,
            start: Int,
            end: Int,
            x: Float,
            top: Int,
            y: Int,
            bottom: Int,
            paint: AndroidPaint
        ) {
            val b = drawable
            val transY = bottom - b.bounds.bottom // căn theo baseline như mặc định
            val contentWidth = view.width - view.paddingLeft - view.paddingRight
            val drawWidth = b.bounds.width()
            val left = view.paddingLeft + (contentWidth - drawWidth) / 2f // CĂN GIỮA NGANG

            canvas.save()
            canvas.translate(left, transY.toFloat())
            b.draw(canvas)
            canvas.restore()
        }

        override fun getSize(
            paint: AndroidPaint,
            text: CharSequence,
            start: Int,
            end: Int,
            fm: AndroidPaint.FontMetricsInt?
        ): Int {
            val d = drawable
            fm?.let {
                val height = d.bounds.bottom - d.bounds.top
                val pfm = paint.fontMetricsInt
                // căn chiều cao vào line-height (đơn giản: giống ImageSpan mặc định)
                val need = height - (pfm.descent - pfm.ascent)
                if (need > 0) {
                    it.ascent = pfm.ascent - need / 2
                    it.descent = pfm.descent + need / 2
                    it.top = it.ascent
                    it.bottom = it.descent
                } else {
                    it.ascent = pfm.ascent
                    it.descent = pfm.descent
                    it.top = pfm.top
                    it.bottom = pfm.bottom
                }
            }
            return d.bounds.right
        }
    }

    /**
     * Hàm build Spanned và thay ImageSpan => CenteredImageSpan
     */
    fun makeCenteredSpanned(
        textView: TextView,
        htmlStr: String,
        imageGetter: Html.ImageGetter
    ): Spanned {
        val spanned: Spanned =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                Html.fromHtml(htmlStr, Html.FROM_HTML_MODE_LEGACY, imageGetter, null)
            } else {
                @Suppress("DEPRECATION")
                Html.fromHtml(htmlStr, imageGetter, null)
            }

        val builder = SpannableStringBuilder(spanned)
        val imgs = builder.getSpans(0, builder.length, ImageSpan::class.java)
        for (img in imgs) {
            val start = builder.getSpanStart(img)
            val end = builder.getSpanEnd(img)
            val flags = builder.getSpanFlags(img)
            builder.removeSpan(img)
            builder.setSpan(CenteredImageSpan(textView, img.drawable), start, end, flags)
        }
        return builder
    }

    AndroidView(
        modifier = modifier,
        factory = {
            TextView(context).apply {
                movementMethod = LinkMovementMethod.getInstance()
                setTextIsSelectable(true)
                textSize = fontSize.value
                setTextColor(color.toArgb())
                setLinkTextColor(linkColor.toArgb())
                setLineSpacing((lineHeight.value - fontSize.value).coerceAtLeast(1f), 1f)
                // padding nhỏ để ảnh không dính sát mép
                val vPad = dp(8f)
                setPadding(0, vPad, 0, vPad)
            }
        },
        update = { textView ->
            textView.textSize = fontSize.value
            textView.setTextColor(color.toArgb())
            textView.setLinkTextColor(linkColor.toArgb())

            // bề rộng tối đa để scale ảnh ~ full width (trừ padding và một chút margin)
            fun computeTargetWidth(): Int {
                val contentW = (textView.width - textView.paddingLeft - textView.paddingRight)
                val fallbackW = context.resources.displayMetrics.widthPixels
                val base = if (contentW > 0) contentW else fallbackW
                val sideMargin = dp(16f) // chừa 16dp mỗi bên
                return (base - sideMargin).coerceAtLeast(dp(64f))
            }

            val imageGetter = Html.ImageGetter { source ->
                // Cache
                loadedImages[source]?.let { return@ImageGetter it }

                // Placeholder đang tải: chấm tròn xám nhỏ
                val placeholder = makeTinyLoadingDot()

                // Load async
                scope.launch {
                    try {
                        val request = ImageRequest.Builder(context)
                            .data(source)
                            .allowHardware(false)
                            .build()

                        val result = imageLoader.execute(request)

                        withContext(Dispatchers.Main) {
                            when (result) {
                                is SuccessResult -> {
                                    val d = result.drawable
                                    // scale gần bằng full width
                                    val targetW = computeTargetWidth()
                                    val w = if (d.intrinsicWidth > 0) d.intrinsicWidth else targetW
                                    val h = if (d.intrinsicHeight > 0) d.intrinsicHeight else targetW / 2
                                    val ratio = targetW.toFloat() / w
                                    val finalW = targetW
                                    val finalH = (h * ratio).roundToInt()
                                    d.setBounds(0, 0, finalW, finalH)
                                    loadedImages[source] = d

                                    // Re-render và thay ImageSpan => CenteredImageSpan
                                    val updatedGetter = Html.ImageGetter { src ->
                                        loadedImages[src] ?: makeTinyErrorIcon()
                                    }
                                    textView.text = makeCenteredSpanned(textView, html, updatedGetter)
                                }

                                is ErrorResult -> {
                                    // Lỗi -> icon nhỏ
                                    val err = makeTinyErrorIcon()
                                    loadedImages[source] = err
                                    val updatedGetter = Html.ImageGetter { src ->
                                        loadedImages[src] ?: makeTinyErrorIcon()
                                    }
                                    textView.text = makeCenteredSpanned(textView, html, updatedGetter)
                                }
                            }
                        }
                    } catch (_: Exception) {
                        withContext(Dispatchers.Main) {
                            val err = makeTinyErrorIcon()
                            loadedImages[source] = err
                            val updatedGetter = Html.ImageGetter { src ->
                                loadedImages[src] ?: makeTinyErrorIcon()
                            }
                            textView.text = makeCenteredSpanned(textView, html, updatedGetter)
                        }
                    }
                }

                placeholder
            }

            // Lần render đầu
            textView.text = makeCenteredSpanned(textView, html, imageGetter)

            // Nếu width chưa tính được (chưa layout), post re-render sau layout
            if (textView.width == 0) {
                textView.post {
                    textView.text = makeCenteredSpanned(textView, html, imageGetter)
                }
            }
        }
    )
}
