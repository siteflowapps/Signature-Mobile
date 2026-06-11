package com.siteflow.cdo.outlet.invoices.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Tips dialog shown before scanning (pre-scan guidance) or after a failed scan
 * (hard block: no document detected / coverage < 50%).
 *
 * @param isPreScan True = shown before the scanner opens (CTA = "Start Scanning").
 *                  False = shown after detection failure (CTA = "Retake Photo").
 * @param onRetake Called when user confirms / retakes.
 * @param onDismiss Called when user dismisses (only visible in post-failure mode).
 */
@Composable
fun InvoiceCaptureGuideDialog(
    onRetake: () -> Unit,
    onDismiss: () -> Unit,
    isPreScan: Boolean = false
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .wrapContentHeight(),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ── Header ──────────────────────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color(0xFFEFF6FF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = if (isPreScan) "How to scan an invoice" else "Tips for a perfect scan",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        color = Color(0xFF111827)
                    )
                    Text(
                        text = if (isPreScan)
                            "Follow these tips for a quick, accurate scan."
                        else
                            "The invoice was not detected clearly.\nFollow these tips and retake the photo.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                        color = Color(0xFF6B7280),
                        textAlign = TextAlign.Center
                    )
                }

                // ── Tips ─────────────────────────────────────────────────
                HorizontalDivider(color = Color(0xFFF3F4F6))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    CaptureTip(
                        icon = Icons.Default.TableRows,
                        iconBg = Color(0xFFF0FDF4),
                        iconTint = Color(0xFF16A34A),
                        title = "Place on a flat surface",
                        body = "Lay the invoice flat — no folds or curves"
                    )
                    CaptureTip(
                        icon = Icons.Default.WbSunny,
                        iconBg = Color(0xFFFFFBEB),
                        iconTint = Color(0xFFD97706),
                        title = "Use good lighting",
                        body = "Avoid shadows, glare, or dim conditions"
                    )
                    CaptureTip(
                        icon = Icons.Default.CropFree,
                        iconBg = Color(0xFFEFF6FF),
                        iconTint = Color(0xFF2563EB),
                        title = "Fill the frame",
                        body = "Move closer so the invoice fills the camera view"
                    )
                    CaptureTip(
                        icon = Icons.Default.PanTool,
                        iconBg = Color(0xFFFDF4FF),
                        iconTint = Color(0xFF9333EA),
                        title = "Hold steady",
                        body = "Keep the phone still — avoid motion blur"
                    )
                }

                HorizontalDivider(color = Color(0xFFF3F4F6))

                // ── Actions ───────────────────────────────────────────────
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onRetake,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2563EB)
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (isPreScan) "Start Scanning" else "Retake Photo",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            ),
                            color = Color.White
                        )
                    }

                    if (!isPreScan) {
                        TextButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text(
                                text = "Continue anyway",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 13.sp),
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureTip(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    body: String
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(iconBg, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = Color(0xFF111827)
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                color = Color(0xFF6B7280)
            )
        }
    }
}
