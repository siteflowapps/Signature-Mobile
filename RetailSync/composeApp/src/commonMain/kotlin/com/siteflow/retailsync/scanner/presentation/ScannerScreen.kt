package com.siteflow.retailsync.scanner.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.retailsync.core.presentation.components.CdoButton
import com.siteflow.retailsync.core.presentation.design.AppColors
import com.siteflow.retailsync.core.presentation.design.AppTypography
import com.siteflow.retailsync.scanner.domain.ScannerAction
import com.siteflow.retailsync.scanner.domain.ScannerEvent
import com.siteflow.retailsync.scanner.domain.ScannerViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun ScannerScreen(
    onNavigateToOrderCreation: (outletId: String, outletName: String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ScannerViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is ScannerEvent.NavigateToOrderCreation ->
                    onNavigateToOrderCreation(event.outletId, event.outletName)
                ScannerEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // ── Live Camera Preview (full screen behind overlay) ──
        QrScannerView(
            modifier = Modifier.fillMaxSize(),
            onQrCodeScanned = { qrData ->
                println("ScannerScreen: QR data received → $qrData")
                viewModel.onAction(ScannerAction.QrScanned(qrData))
            }
        )

        // ── Overlay UI on top of camera ──
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.onAction(ScannerAction.Cancel) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Scan QR Code",
                    style = AppTypography.TitleMedium.copy(fontSize = 18.sp),
                    color = Color.White
                )
            }

            // QR Frame Overlay (centered)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition()
                val scanLineY by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Box(
                    modifier = Modifier
                        .size(280.dp)
                        .border(2.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Corner brackets
                    Box(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier.align(Alignment.TopStart).size(40.dp).padding(4.dp)
                        ) {
                            Box(Modifier.fillMaxWidth().height(3.dp).background(AppColors.BlueGradientStart))
                            Box(Modifier.fillMaxHeight().width(3.dp).background(AppColors.BlueGradientStart))
                        }
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd).size(40.dp).padding(4.dp)
                        ) {
                            Box(Modifier.fillMaxWidth().height(3.dp).background(AppColors.BlueGradientStart))
                            Box(Modifier.fillMaxHeight().width(3.dp).align(Alignment.TopEnd).background(AppColors.BlueGradientStart))
                        }
                        Box(
                            modifier = Modifier.align(Alignment.BottomStart).size(40.dp).padding(4.dp)
                        ) {
                            Box(Modifier.fillMaxWidth().height(3.dp).align(Alignment.BottomStart).background(AppColors.BlueGradientStart))
                            Box(Modifier.fillMaxHeight().width(3.dp).background(AppColors.BlueGradientStart))
                        }
                        Box(
                            modifier = Modifier.align(Alignment.BottomEnd).size(40.dp).padding(4.dp)
                        ) {
                            Box(Modifier.fillMaxWidth().height(3.dp).align(Alignment.BottomEnd).background(AppColors.BlueGradientStart))
                            Box(Modifier.fillMaxHeight().width(3.dp).align(Alignment.TopEnd).background(AppColors.BlueGradientStart))
                        }
                    }

                    // Animated scan line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .height(2.dp)
                            .offset(y = ((scanLineY - 0.5f) * 240).dp)
                            .background(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        AppColors.BlueGradientStart,
                                        AppColors.BlueGradientEnd,
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                }
            }

            // Bottom panel — result only (no manual input)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = Color.White
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (state.outletId != null && state.outletName != null) {
                        // Outlet found!
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = AppColors.Success,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Outlet Found",
                            style = AppTypography.TitleMedium,
                            color = AppColors.TextPrimary
                        )
                        Text(
                            state.outletName ?: "",
                            style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.SemiBold),
                            color = AppColors.BlueGradientStart
                        )
                        Text(
                            "ID: ${state.outletId}",
                            style = AppTypography.Caption,
                            color = AppColors.TextTertiary
                        )
                        Spacer(Modifier.height(16.dp))
                        CdoButton(
                            text = "Create Invoice",
                            onClick = { viewModel.onAction(ScannerAction.ConfirmOutlet) },
                            trailingIcon = null
                        )
                    } else if (state.error != null) {
                        // Error state
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = AppColors.Danger,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            state.error ?: "",
                            style = AppTypography.BodySecondary,
                            color = AppColors.Danger,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        // Scanning state
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = AppColors.BlueGradientStart,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Point your camera at an outlet QR code",
                            style = AppTypography.BodySecondary,
                            color = AppColors.TextTertiary,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
