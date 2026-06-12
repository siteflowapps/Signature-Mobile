package com.siteflow.signature.outlet.walkthrough.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.cso.onboarding.data.dto.OutletResponseData
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughAction
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughEvent
import com.siteflow.signature.outlet.walkthrough.domain.WalkthroughViewModel
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun WalkthroughOutletDetailsScreen(
    onNavigateToAgreement: () -> Unit,
    onBack: () -> Unit,
    viewModel: WalkthroughViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(WalkthroughAction.LoadOutlet)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collectLatest { event ->
            when (event) {
                WalkthroughEvent.NavigateToAgreement -> onNavigateToAgreement()
                WalkthroughEvent.NavigateBack -> onBack()
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AppColors.BackgroundGradientStart, AppColors.BackgroundGradientEnd)
                )
            )
    ) {
        // ── Sticky header ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.BackgroundGradientStart)
                .padding(start = 8.dp, end = 20.dp, top = 48.dp, bottom = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(
                    onClick = { viewModel.onAction(WalkthroughAction.GoBack) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.black27
                    )
                }
                WalkthroughStepIndicator(currentStep = 2, totalSteps = 4)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Your Outlet Details",
                color = AppColors.black27,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Review your registered outlet information",
                color = AppColors.WalkthroughTextSubtle,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = AppColors.Divider)
        }

        // ── Body ──────────────────────────────────────────────────────────────
        when {
            state.isLoadingOutlet -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.BlueGradientStart)
                }
            }

            state.outletError != null -> {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.outletError!!,
                            color = AppColors.TextSecondary,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                        Spacer(Modifier.height(16.dp))
                        TextButton(
                            onClick = { viewModel.onAction(WalkthroughAction.RetryLoadOutlet) }
                        ) {
                            Text("Retry", color = AppColors.BlueGradientStart, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutletInfoCard(
                        title = "Outlet Identity",
                        rows = buildList {
                            val o = state.outlet
                            add("Name" to (o?.name ?: "—"))
                            add("Phone" to (o?.phone ?: "—"))
                            add("Type" to (o?.outletType?.toFriendlyOutletType() ?: "—"))
                        }
                    )

                    OutletInfoCard(
                        title = "Owner Details",
                        rows = buildList {
                            val o = state.outlet
                            add("Owner Name" to (o?.ownerName ?: "—"))
                            add("Mobile" to (o?.ownerMobile ?: "—"))
                            o?.ownerWhatsapp?.let { add("WhatsApp" to it) }
                            o?.email?.let { add("Email" to it) }
                        }
                    )

                    OutletInfoCard(
                        title = "Location",
                        rows = buildList {
                            val o = state.outlet
                            add("Address" to (o?.address ?: "—"))
                            o?.landmark?.let { add("Landmark" to it) }
                            o?.locality?.let { add("Locality" to it) }
                            o?.pincode?.let { add("Pincode" to it) }
                            o?.city?.let { add("City" to it) }
                            o?.state?.let { add("State" to it) }
                        }
                    )
                }
            }
        }

        // ── Sticky footer ─────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.CardBackground)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            HorizontalDivider(color = AppColors.Divider, modifier = Modifier.padding(bottom = 16.dp))
            Button(
                onClick = { viewModel.onAction(WalkthroughAction.ContinueFromOutletDetails) },
                enabled = !state.isLoadingOutlet && state.outlet != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.BlueGradientStart,
                    disabledContainerColor = AppColors.greyEB
                )
            ) {
                Text(
                    text = "Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (!state.isLoadingOutlet && state.outlet != null) Color.White else AppColors.grey80
                )
            }
        }
    }
}

// ── Card composables ──────────────────────────────────────────────────────────

@Composable
private fun OutletInfoCard(title: String, rows: List<Pair<String, String>>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
    ) {
        // Card header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    AppColors.BlueGradientStart.copy(alpha = 0.06f),
                    RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text(
                text = title,
                color = AppColors.WalkthroughTextBlue,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
        HorizontalDivider(color = AppColors.Divider)
        rows.forEachIndexed { index, (label, value) ->
            OutletInfoRow(label = label, value = value)
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    color = AppColors.Divider.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
private fun OutletInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = AppColors.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            color = AppColors.TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun String.toFriendlyOutletType(): String = when (this.uppercase()) {
    "GROCERY" -> "Kirana / General Store"
    "MEDICAL" -> "Medical Store"
    "HARDWARE" -> "Hardware Store"
    "ELECTRONICS" -> "Electronics Store"
    "CLOTHING" -> "Clothing Store"
    "RESTAURANT" -> "Restaurant / Eatery"
    else -> this.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }
}

private fun String.toTitleCase(): String =
    this.replace('_', ' ').lowercase().split(' ')
        .joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
