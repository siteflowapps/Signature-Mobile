package com.siteflow.signature.outlet.invoices.presentation

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.math.max
import kotlin.math.min

/**
 * Full-screen ROI scan with pinch-to-zoom, pan, and a compact bottom bar.
 *
 * Design principles:
 *   • Image fills the entire screen edge-to-edge behind the status bar
 *   • Pinch-to-zoom (1x..5x) + two-finger pan to navigate the invoice
 *   • Draggable/resizable selection rectangle with L-bracket corner handles
 *   • Compact floating bottom pill shows extract + use-value buttons
 *   • Extracted text preview in a small chip, not a full card
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun RegionScanScreen(
    imagePath: String,
    fieldLabel: String,
    onResult: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    // ── Zoom & pan state ──
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        offset = Offset(
            x = offset.x + panChange.x,
            y = offset.y + panChange.y
        )
    }

    // ── Image / display tracking ──
    var displaySize by remember { mutableStateOf(IntSize.Zero) }

    // ── Selection rect (screen-space, before zoom transforms) ──
    var rectStart by remember { mutableStateOf(Offset.Zero) }
    var rectEnd by remember { mutableStateOf(Offset.Zero) }
    var initialized by remember { mutableStateOf(false) }

    // ── Drag state ──
    // 0=none, 1=TL, 2=TR, 3=BL, 4=BR, 5=body
    var dragging by remember { mutableIntStateOf(0) }
    var dragStartPos by remember { mutableStateOf(Offset.Zero) }
    var dragRectStartSaved by remember { mutableStateOf(Offset.Zero) }
    var dragRectEndSaved by remember { mutableStateOf(Offset.Zero) }

    var isExtracting by remember { mutableStateOf(false) }
    var extractedText by remember { mutableStateOf<String?>(null) }

    // ── Design tokens ──
    val accentBlue = Color(0xFF3B82F6)
    val dimColor = Color(0x44000000)
    val handleWhite = Color.White
    val cornerLen = 20.dp
    val handleTouchRadius = 28.dp  // touch target (invisible), larger for easy drag

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111))
            .systemBarsPadding()
    ) {
        // ═══════════ ZOOMABLE IMAGE + OVERLAY ═══════════
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.x
                    translationY = offset.y
                }
                .transformable(state = transformState)
        ) {
            // Invoice image — fills entire space
            AsyncImage(
                model = imagePath,
                contentDescription = "Invoice",
                modifier = Modifier
                    .fillMaxSize()
                    .onSizeChanged { size ->
                        displaySize = size
                        if (!initialized && size.width > 0 && size.height > 0) {
                            // Default selection: center band, 80% wide, 20% tall
                            val cx = size.width / 2f
                            val cy = size.height / 2f
                            val hw = size.width * 0.4f
                            val hh = size.height * 0.1f
                            rectStart = Offset(cx - hw, cy - hh)
                            rectEnd = Offset(cx + hw, cy + hh)
                            initialized = true
                        }
                    },
                contentScale = ContentScale.Fit
            )

            // ── Selection overlay + drag handling ──
            if (initialized) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(displaySize) {
                            detectDragGestures(
                                onDragStart = { pos ->
                                    dragStartPos = pos
                                    dragRectStartSaved = rectStart
                                    dragRectEndSaved = rectEnd

                                    val l = min(rectStart.x, rectEnd.x)
                                    val t = min(rectStart.y, rectEnd.y)
                                    val r = max(rectStart.x, rectEnd.x)
                                    val b = max(rectStart.y, rectEnd.y)
                                    val hr = handleTouchRadius.toPx()

                                    dragging = when {
                                        (pos - Offset(l, t)).getDistance() < hr -> 1
                                        (pos - Offset(r, t)).getDistance() < hr -> 2
                                        (pos - Offset(l, b)).getDistance() < hr -> 3
                                        (pos - Offset(r, b)).getDistance() < hr -> 4
                                        pos.x in l..r && pos.y in t..b -> 5
                                        else -> 0
                                    }
                                },
                                onDrag = { change, amount ->
                                    change.consume()
                                    val w = displaySize.width.toFloat()
                                    val h = displaySize.height.toFloat()
                                    val minDim = 30f

                                    when (dragging) {
                                        1 -> rectStart = Offset(
                                            (rectStart.x + amount.x).coerceIn(0f, rectEnd.x - minDim),
                                            (rectStart.y + amount.y).coerceIn(0f, rectEnd.y - minDim)
                                        )
                                        2 -> {
                                            rectEnd = Offset((rectEnd.x + amount.x).coerceIn(rectStart.x + minDim, w), rectEnd.y)
                                            rectStart = Offset(rectStart.x, (rectStart.y + amount.y).coerceIn(0f, rectEnd.y - minDim))
                                        }
                                        3 -> {
                                            rectStart = Offset((rectStart.x + amount.x).coerceIn(0f, rectEnd.x - minDim), rectStart.y)
                                            rectEnd = Offset(rectEnd.x, (rectEnd.y + amount.y).coerceIn(rectStart.y + minDim, h))
                                        }
                                        4 -> rectEnd = Offset(
                                            (rectEnd.x + amount.x).coerceIn(rectStart.x + minDim, w),
                                            (rectEnd.y + amount.y).coerceIn(rectStart.y + minDim, h)
                                        )
                                        5 -> {
                                            val rw = (rectEnd.x - rectStart.x)
                                            val rh = (rectEnd.y - rectStart.y)
                                            val newSX = (dragRectStartSaved.x + (change.position.x - dragStartPos.x)).coerceIn(0f, w - rw)
                                            val newSY = (dragRectStartSaved.y + (change.position.y - dragStartPos.y)).coerceIn(0f, h - rh)
                                            rectStart = Offset(newSX, newSY)
                                            rectEnd = Offset(newSX + rw, newSY + rh)
                                        }
                                    }
                                },
                                onDragEnd = { dragging = 0 }
                            )
                        }
                ) {
                    val l = min(rectStart.x, rectEnd.x)
                    val t = min(rectStart.y, rectEnd.y)
                    val r = max(rectStart.x, rectEnd.x)
                    val b = max(rectStart.y, rectEnd.y)
                    val rw = r - l
                    val rh = b - t
                    val cLen = cornerLen.toPx()
                    val borderW = 2.dp.toPx()
                    val handleW = 3.dp.toPx()

                    // Dim overlay: 4 strips around selection
                    drawRect(dimColor, Offset.Zero, Size(size.width, t))                          // top
                    drawRect(dimColor, Offset(0f, b), Size(size.width, size.height - b))           // bottom
                    drawRect(dimColor, Offset(0f, t), Size(l, rh))                                 // left
                    drawRect(dimColor, Offset(r, t), Size(size.width - r, rh))                     // right

                    // Selection border
                    drawRect(accentBlue.copy(alpha = 0.6f), Offset(l, t), Size(rw, rh), style = Stroke(borderW))

                    // Corner L-brackets (white)
                    val crnrs = listOf(
                        Triple(Offset(l, t), Offset(l + cLen, t), Offset(l, t + cLen)),
                        Triple(Offset(r, t), Offset(r - cLen, t), Offset(r, t + cLen)),
                        Triple(Offset(l, b), Offset(l + cLen, b), Offset(l, b - cLen)),
                        Triple(Offset(r, b), Offset(r - cLen, b), Offset(r, b - cLen))
                    )
                    crnrs.forEach { (c, h, v) ->
                        drawLine(handleWhite, c, h, strokeWidth = handleW)
                        drawLine(handleWhite, c, v, strokeWidth = handleW)
                    }
                }
            }
        }

        // ═══════════ TOP BAR — minimal, floating ═══════════
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            // Zoom level indicator
            Surface(
                color = Color(0x66000000),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "${String.format("%.1f", scale)}×",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // ═══════════ FIELD LABEL CHIP — top center ═══════════
        Surface(
            color = Color(0xCC000000),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 48.dp)
        ) {
            Text(
                text = "📍 Scanning: $fieldLabel",
                color = Color.White,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        // ═══════════ BOTTOM BAR — compact floating pill ═══════════
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Extracted text preview chip
            AnimatedVisibility(
                visible = extractedText != null,
                enter = fadeIn() + slideInVertically { it }
            ) {
                Surface(
                    color = Color(0xE6FFFFFF),
                    shape = RoundedCornerShape(12.dp),
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CropFree, null, tint = accentBlue, modifier = Modifier.size(16.dp))
                        Text(
                            text = extractedText?.ifBlank { "—" } ?: "",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF111827),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Action buttons row
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Extract button
                Button(
                    onClick = { isExtracting = true },
                    enabled = !isExtracting && displaySize != IntSize.Zero,
                    colors = ButtonDefaults.buttonColors(containerColor = accentBlue),
                    shape = RoundedCornerShape(24.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    if (isExtracting) {
                        CircularProgressIndicator(Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.Search, null, Modifier.size(16.dp))
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(if (isExtracting) "Scanning…" else "Extract", fontSize = 14.sp)
                }

                // Use value — only after extraction
                if (extractedText != null) {
                    Button(
                        onClick = {
                            extractedText?.let { onResult(it) }
                            onBack()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(24.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Check, null, Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Use", fontSize = 14.sp)
                    }
                }
            }

            // Hint text — single line, subtle
            Text(
                text = "Pinch to zoom • Drag corners to resize",
                fontSize = 11.sp,
                color = Color(0x99FFFFFF),
                textAlign = TextAlign.Center
            )
        }
    }

    // ── Trigger OCR extraction ──
    LaunchedEffect(isExtracting) {
        if (!isExtracting || displaySize == IntSize.Zero) return@LaunchedEffect
        val result = withContext(Dispatchers.IO) {
            cropAndOcr(context, imagePath, displaySize, rectStart, rectEnd)
        }
        extractedText = result
        isExtracting = false
    }
}

/** Crops the image to the selected screen-space rect, then runs ML Kit OCR on the crop. */
private suspend fun cropAndOcr(
    context: android.content.Context,
    imagePath: String,
    displaySize: IntSize,
    rectStart: Offset,
    rectEnd: Offset
): String {
    return suspendCancellableCoroutine { cont ->
        try {
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(imagePath, opts)
            val imgW = opts.outWidth.toFloat()
            val imgH = opts.outHeight.toFloat()

            val scaleX = displaySize.width / imgW
            val scaleY = displaySize.height / imgH
            val fitScale = minOf(scaleX, scaleY)
            val renderedW = imgW * fitScale
            val renderedH = imgH * fitScale
            val offsetX = (displaySize.width - renderedW) / 2f
            val offsetY = (displaySize.height - renderedH) / 2f

            val left = min(rectStart.x, rectEnd.x)
            val top = min(rectStart.y, rectEnd.y)
            val right = max(rectStart.x, rectEnd.x)
            val bottom = max(rectStart.y, rectEnd.y)

            val imgLeft = ((left - offsetX) / fitScale).coerceIn(0f, imgW).toInt()
            val imgTop = ((top - offsetY) / fitScale).coerceIn(0f, imgH).toInt()
            val imgRight = ((right - offsetX) / fitScale).coerceIn(0f, imgW).toInt()
            val imgBottom = ((bottom - offsetY) / fitScale).coerceIn(0f, imgH).toInt()

            val cropW = (imgRight - imgLeft).coerceAtLeast(1)
            val cropH = (imgBottom - imgTop).coerceAtLeast(1)

            val bitmap = BitmapFactory.decodeFile(imagePath)
            val crop = Bitmap.createBitmap(bitmap, imgLeft, imgTop, cropW, cropH)
            bitmap.recycle()

            val inputImage = InputImage.fromBitmap(crop, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

            recognizer.process(inputImage)
                .addOnSuccessListener { visionText ->
                    crop.recycle()
                    cont.resume(visionText.text.trim())
                }
                .addOnFailureListener {
                    crop.recycle()
                    cont.resume("")
                }
        } catch (e: Exception) {
            cont.resume("")
        }
    }
}
