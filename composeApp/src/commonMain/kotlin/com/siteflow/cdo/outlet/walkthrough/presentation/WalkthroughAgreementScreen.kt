package com.siteflow.cdo.outlet.walkthrough.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.outlet.walkthrough.domain.WalkthroughAction
import com.siteflow.cdo.outlet.walkthrough.domain.WalkthroughEvent
import com.siteflow.cdo.outlet.walkthrough.domain.WalkthroughViewModel
import com.siteflow.cdo.shared.pfp.AgreementClause
import com.siteflow.cdo.shared.pfp.PFP_CLAUSES
import com.siteflow.cdo.shared.pfp.VOLUME_SLAB_TABLE
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun WalkthroughAgreementScreen(
    onNavigateToPayment: () -> Unit,
    onBack: () -> Unit,
    viewModel: WalkthroughViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                WalkthroughEvent.NavigateToPayment -> onNavigateToPayment()
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
        // ── Sticky header ─────────────────────────────────────────────────────
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
                WalkthroughStepIndicator(currentStep = 3, totalSteps = 4)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "Program Agreement",
                color = AppColors.black27,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Campa Destination Outlet – PFP Program",
                color = AppColors.WalkthroughTextSubtle,
                fontSize = 13.sp,
                modifier = Modifier.padding(start = 12.dp)
            )
            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = AppColors.Divider)
        }

        // ── Scrollable body ───────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Meta info card
            AgreementMetaCard()

            Spacer(Modifier.height(20.dp))

            // Clauses
            PFP_CLAUSES.forEach { clause ->
                ClauseItem(clause)
                Spacer(Modifier.height(14.dp))
            }

            HorizontalDivider(color = AppColors.Divider)
            Spacer(Modifier.height(20.dp))

            // Annexure 1
            AnnexureHeader(number = "1", title = "Outlet Classification")
            Spacer(Modifier.height(8.dp))
            VolumeSlabTable()
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Payout applies on Beverage Category — Sparkling, Stills and Energy. Water is excluded.",
                color = AppColors.WalkthroughTextSubtle,
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontStyle = FontStyle.Italic
            )

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = AppColors.Divider)
            Spacer(Modifier.height(20.dp))

            // Consent checkboxes
            ConsentCheckbox(
                checked = state.consentConfirmInfo,
                text = "I confirm that the outlet information provided is correct.",
                onToggle = { viewModel.onAction(WalkthroughAction.ToggleConsentConfirmInfo) }
            )
            Spacer(Modifier.height(12.dp))
            ConsentCheckbox(
                checked = state.consentAcceptTerms,
                text = "I agree to the Terms and Conditions of the Campa Destination Outlet Program.",
                onToggle = { viewModel.onAction(WalkthroughAction.ToggleConsentAcceptTerms) }
            )

            Spacer(Modifier.height(24.dp))
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
                onClick = { viewModel.onAction(WalkthroughAction.ContinueFromAgreement) },
                enabled = state.canProceedFromAgreement,
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
                    text = "I Agree & Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (state.canProceedFromAgreement) Color.White else AppColors.grey80
                )
            }
            if (!state.canProceedFromAgreement) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Please accept both checkboxes to continue",
                    color = AppColors.WalkthroughTextSubtle,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────────────────────

@Composable
private fun AgreementMetaCard() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(1.dp, AppColors.Divider, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetaRow(label = "Program", value = "Campa Destination Outlet Program")
        MetaRow(label = "Facilitator", value = "Silveraxis Technologies Private Limited")
        MetaRow(label = "Brand Owner", value = "Reliance Consumer Products Limited (RCPL)")
        MetaRow(label = "Version", value = "1.0")
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            color = AppColors.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            color = AppColors.TextPrimary,
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ClauseItem(clause: AgreementClause) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // Clause title with number badge
        Row(verticalAlignment = Alignment.Top) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .background(AppColors.WalkthroughTextBlue, CircleShape)
            ) {
                Text(
                    text = clause.id.toString(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = clause.title,
                color = AppColors.black27,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )
        }

        // Clause body text
        if (clause.text != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = clause.text,
                color = AppColors.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(start = 32.dp)
            )
        }

        // Sub-clauses
        if (clause.subClauses.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier.padding(start = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                clause.subClauses.forEach { sub ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "(${sub.first.substringAfter(".")})",
                            color = AppColors.WalkthroughTextBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(24.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = sub.second,
                            color = AppColors.TextSecondary,
                            fontSize = 13.sp,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnnexureHeader(number: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(AppColors.WalkthroughAccentPurple.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Annexure $number",
                color = AppColors.WalkthroughAccentPurple,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            color = AppColors.black27,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun VolumeSlabTable() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.Divider, RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColors.greyF6)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text("Slab", color = AppColors.grey51, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("Volume (excl. water)", color = AppColors.grey51, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.4f))
            Text("Payout/case", color = AppColors.grey51, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = AppColors.Divider)
        VOLUME_SLAB_TABLE.forEachIndexed { index, (tier, volume, payout) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) Color.White else AppColors.ScreenBackground)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TierBadge(tier = tier)
                Spacer(Modifier.width(6.dp))
                Text(tier, color = AppColors.TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(volume, color = AppColors.TextSecondary, fontSize = 12.sp, modifier = Modifier.weight(1.4f))
                Text(payout, color = AppColors.WalkthroughTextBlue, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            if (index < VOLUME_SLAB_TABLE.lastIndex) HorizontalDivider(color = AppColors.Divider)
        }
    }
}

@Composable
private fun TierBadge(tier: String) {
    val (bg, fg) = when (tier) {
        "Silver"   -> Color(0xFFE2E8F0) to Color(0xFF475569)
        "Gold"     -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        "Diamond"  -> Color(0xFFE0F2FE) to Color(0xFF0369A1)
        "Platinum" -> Color(0xFFF0FDF4) to Color(0xFF15803D)
        else       -> AppColors.greyF6 to AppColors.grey51
    }
    Box(
        modifier = Modifier
            .size(8.dp)
            .background(bg, CircleShape)
            .border(1.dp, fg.copy(alpha = 0.4f), CircleShape)
    )
}

@Composable
private fun ConsentCheckbox(
    checked: Boolean,
    text: String,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() }
            .background(
                if (checked) AppColors.WalkthroughTextBlue.copy(alpha = 0.05f)
                else Color.Transparent,
                RoundedCornerShape(10.dp)
            )
            .border(
                1.dp,
                if (checked) AppColors.WalkthroughTextBlue.copy(alpha = 0.25f)
                else AppColors.Divider,
                RoundedCornerShape(10.dp)
            )
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Checkbox box
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(20.dp)
                .background(
                    if (checked) AppColors.WalkthroughTextBlue else Color.White,
                    RoundedCornerShape(5.dp)
                )
                .border(
                    1.5.dp,
                    if (checked) AppColors.WalkthroughTextBlue else AppColors.grey80,
                    RoundedCornerShape(5.dp)
                )
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = AppColors.TextPrimary, fontSize = 13.sp)) {
                    append(text)
                }
            },
            lineHeight = 19.sp
        )
    }
}

// ── Step indicator (shared) ───────────────────────────────────────────────────

@Composable
internal fun WalkthroughStepIndicator(currentStep: Int, totalSteps: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 1..totalSteps) {
            Box(
                modifier = Modifier.then(
                    if (i == currentStep) {
                        Modifier
                            .size(width = 24.dp, height = 8.dp)
                            .background(AppColors.BlueGradientStart, RoundedCornerShape(4.dp))
                    } else {
                        Modifier
                            .size(8.dp)
                            .background(
                                if (i < currentStep) AppColors.BlueGradientStart.copy(alpha = 0.5f)
                                else AppColors.greyEB,
                                CircleShape
                            )
                    }
                )
            )
        }
    }
}
