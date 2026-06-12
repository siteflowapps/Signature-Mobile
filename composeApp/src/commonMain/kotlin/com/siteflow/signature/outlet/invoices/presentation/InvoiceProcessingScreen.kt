package com.siteflow.signature.outlet.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.invoices.data.ExtractionPhase
import com.siteflow.signature.outlet.invoices.domain.UploadInvoiceAction
import com.siteflow.signature.outlet.invoices.domain.UploadInvoiceEvent
import com.siteflow.signature.outlet.invoices.domain.UploadInvoiceViewModel
import com.siteflow.signature.outlet.invoices.presentation.components.PremiumProcessingAnimation
import org.koin.compose.koinInject

/**
 * Full-screen AI extraction in-progress screen.
 *
 * Shows [PremiumProcessingAnimation] while the ViewModel polls the backend.
 * Navigates to [InvoiceReviewScreen] on [UploadInvoiceEvent.NavigateToReview],
 * or pops back on cancellation.
 */
@Composable
fun InvoiceProcessingScreen(
    onBack: () -> Unit,
    onNavigateToReview: () -> Unit,
    viewModel: UploadInvoiceViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    var showCancelDialog by remember { mutableStateOf(false) }
    var failureMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                UploadInvoiceEvent.NavigateToReview -> onNavigateToReview()
                is UploadInvoiceEvent.ExtractionFailed -> failureMessage = event.reason
                else -> {}
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFF0F4FF), Color(0xFFFFFFFF))
                )
            )
    ) {
        // Cancel button — top right
        TextButton(
            onClick = { showCancelDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 12.dp, end = 12.dp)
        ) {
            Text(
                text = "Cancel",
                style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                color = AppColors.TextSecondary
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "AI Invoice Analysis",
                style = AppTypography.TitleLarge.copy(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = AppColors.TextPrimary
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "Our AI is reading your invoice.\nThis usually takes under 30 seconds.",
                style = AppTypography.BodyPrimary.copy(fontSize = 14.sp),
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(36.dp))

            PremiumProcessingAnimation(
                phase = state.extractionPhase,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(28.dp))
        }
    }

    // Cancel confirmation dialog
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Processing?") },
            text = {
                Text("The AI extraction will stop. You can still fill in invoice details manually.")
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onAction(UploadInvoiceAction.CancelExtraction)
                    showCancelDialog = false
                    onBack()
                }) {
                    Text("Yes, Cancel", color = AppColors.Danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Waiting")
                }
            }
        )
    }

    // Extraction failure dialog
    failureMessage?.let { reason ->
        AlertDialog(
            onDismissRequest = {
                failureMessage = null
                onBack()
            },
            title = { Text("Extraction Failed") },
            text = { Text(reason) },
            confirmButton = {
                TextButton(onClick = {
                    failureMessage = null
                    onBack()
                }) {
                    Text("Go Back")
                }
            }
        )
    }
}
