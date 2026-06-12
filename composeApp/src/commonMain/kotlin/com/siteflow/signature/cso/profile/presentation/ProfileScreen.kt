package com.siteflow.signature.cso.profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector

import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.siteflow.signature.cso.profile.data.*
import com.siteflow.signature.cso.profile.domain.ProfileAction
import com.siteflow.signature.cso.profile.domain.ProfileEvent
import com.siteflow.signature.cso.profile.domain.ProfileViewModel
import com.siteflow.signature.core.domain.RoleManager
import com.siteflow.signature.core.domain.UserRole
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.walkthrough.data.OutletKycData
import com.siteflow.signature.getPlatform
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject
import qrcode.QRCode
import com.siteflow.signature.core.util.decodeToImageBitmap
import com.siteflow.signature.shared.pfp.AgreementClause
import com.siteflow.signature.shared.pfp.PFP_CLAUSES
import com.siteflow.signature.shared.pfp.VOLUME_SLAB_TABLE

@Composable
fun ProfileScreen(
    onLogout: () -> Unit,
    onNavigateToMyTeam: () -> Unit,
    onHelpSupport: () -> Unit,
    viewModel: ProfileViewModel = koinInject()
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAgreement by remember { mutableStateOf(false) }

    val state by viewModel.state.collectAsState()
    val profile = state.profile
    val currentRole by RoleManager.currentRole.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(ProfileAction.LoadProfile)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                ProfileEvent.NavigateToLogin -> onLogout()
                ProfileEvent.AccountDeleted -> onLogout()
            }
        }
    }

    // ── Logout Confirmation Dialog ──
    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onConfirm = {
                showLogoutDialog = false
                viewModel.onAction(ProfileAction.Logout)
            },
            onDismiss = { showLogoutDialog = false }
        )
    }

    // ── Delete Account Confirmation Dialog ──
    if (state.showDeleteAccountDialog) {
        DeleteAccountConfirmationDialog(
            onConfirm = { viewModel.onAction(ProfileAction.ConfirmDeleteAccount) },
            onDismiss = { viewModel.onAction(ProfileAction.DismissDeleteAccountDialog) }
        )
    }

    // ── PFP Agreement Full-Page Overlay ──
    if (showAgreement) {
        PfpAgreementFullScreen(onBack = { showAgreement = false })
        return
    }

    if (profile == null || state.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColors.ScreenBackground),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = AppColors.BlueGradientStart)
        }
        return
    }

    // ── Delete in-progress overlay ──
    if (state.isDeletingAccount) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(color = AppColors.Danger)
                    Text(
                        text = "Deleting account...",
                        style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF1E293B)
                    )
                }
            }
        }
        return
    }

    Scaffold(
        topBar = { ProfileTopBar() },
        containerColor = AppColors.ScreenBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            if (currentRole == UserRole.OUTLET && profile.outletProfile != null) {
                OutletProfileContent(
                    profile = profile,
                    outletId = state.outletId,
                    kycData = state.kycData,
                    isKycLoading = state.isKycLoading,
                    onLogoutClick = { viewModel.onAction(ProfileAction.LogoutTapped); showLogoutDialog = true },
                    onAgreementClick = { showAgreement = true },
                    onDeleteAccountClick = { viewModel.onAction(ProfileAction.DeleteAccountRequested) },
                    onHelpSupport = onHelpSupport
                )
            } else {
                AseAsmProfileContent(
                    profile,
                    currentRole,
                    onLogoutClick = { viewModel.onAction(ProfileAction.LogoutTapped); showLogoutDialog = true },
                    onNavigateToMyTeam,
                    onDeleteAccountClick = { viewModel.onAction(ProfileAction.DeleteAccountRequested) },
                    onHelpSupport = onHelpSupport
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ProfileTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Profile",
            style = AppTypography.TitleLarge.copy(
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E293B)
            )
        )
    }
}

@Composable
private fun OutletProfileContent(
    profile: AseProfile,
    outletId: String?,
    kycData: OutletKycData?,
    isKycLoading: Boolean,
    onLogoutClick: () -> Unit,
    onAgreementClick: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onHelpSupport: () -> Unit
) {
    val outlet = profile.outletProfile!!
    
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Identity Card ──
        OutletIdentityCard(profile, outlet)

        // ── QR Code Section ──
//        if (outletId != null) {
//            SectionLabel("My QR Code")
//            OutletQrCodeCard(
//                outletId = outletId,
//                outletName = profile.name
//            )
//        }

        // ── Payment Method (from KYC API) ──
        SectionLabel("Payment Method")
        if (isKycLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColors.BlueGradientStart, modifier = Modifier.size(24.dp))
            }
        } else {
            val isVerified = kycData?.verifiedAt != null
            val hasUpi = !kycData?.upiId.isNullOrBlank()
            if (hasUpi) {
                UpiPaymentCard(upiId = kycData!!.upiId!!, isVerified = isVerified)
            } else {
                val bankDetails = if (kycData != null) {
                    BankDetails(
                        accountHolder = kycData.accountHolderName ?: "—",
                        bankName = kycData.bankName ?: "—",
                        ifscCode = kycData.ifscCode ?: "—",
                        maskedAccountNumber = kycData.bankAccountNumber?.let {
                            if (it.length > 4) "•••• •••• •••• ${it.takeLast(4)}" else it
                        } ?: "—",
                        isVerified = isVerified,
                        accountType = kycData.bankAccountType
                    )
                } else {
                    outlet.bankDetails
                }
                BankPaymentCard(bankDetails)
            }
        }

        // ── Documents Section ──
        SectionLabel("Documents")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            ProfileMenuItem(
                icon = Icons.Default.Description,
                iconBgColor = Color(0xFFFEF3C7),
                iconTint = AppColors.orange06,
                label = "PFP Terms & Conditions",
                onClick = onAgreementClick
            )
        }

        // ── Settings Section ──
        SectionLabel("Settings")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconBgColor = Color.Transparent,
                    iconTint = AppColors.TextTertiary,
                    label = "Help & Support",
                    onClick = onHelpSupport
                )
                MenuDivider()
                ProfileMenuItem(
                    icon = Icons.AutoMirrored.Filled.Logout,
                    iconBgColor = Color.Transparent,
                    iconTint = AppColors.Danger,
                    label = "Logout",
                    labelColor = AppColors.Danger,
                    onClick = onLogoutClick
                )
                MenuDivider()
                ProfileMenuItem(
                    icon = Icons.Default.DeleteForever,
                    iconBgColor = Color(0xFFFEE2E2),
                    iconTint = AppColors.Danger,
                    label = "Delete Account",
                    labelColor = AppColors.Danger,
                    onClick = onDeleteAccountClick
                )
            }
        }

        // ── App Version ──
        Spacer(Modifier.height(8.dp))
        Text(
            text = "App Version ${getPlatform().appVersion}",
            style = AppTypography.Caption.copy(fontSize = 11.sp),
            color = AppColors.TextTertiary.copy(alpha = 0.6f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AseAsmProfileContent(
    profile: AseProfile,
    currentRole: UserRole?,
    onLogoutClick: () -> Unit,
    onNavigateToMyTeam: () -> Unit,
    onDeleteAccountClick: () -> Unit,
    onHelpSupport: () -> Unit
) {
    val uriHandler = LocalUriHandler.current
    ProfileHeader(profile, currentRole)
    
    Column(
        modifier = Modifier.padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(Modifier.height(4.dp))

        SectionLabel("General")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                ProfileMenuItem(icon = Icons.AutoMirrored.Filled.HelpOutline, iconBgColor = Color(0xFFDBEAFE), iconTint = AppColors.BlueGradientStart, label = "Help & Support", onClick = onHelpSupport)
            }
        }

        SectionLabel("Legal & More")
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                ProfileMenuItem(icon = Icons.Default.Shield, iconBgColor = Color(0xFFD1FAE5), iconTint = AppColors.Success, label = "Privacy Policy", onClick = {
                    uriHandler.openUri("https://siteflow.tech/privacy-policy")
                })
                MenuDivider()
//                ProfileMenuItem(icon = Icons.Default.Description, iconBgColor = Color(0xFFFEF3C7), iconTint = AppColors.orange06, label = "Terms & Conditions", onClick = {})
//                MenuDivider()
                ProfileMenuItem(icon = Icons.Default.Star, iconBgColor = Color(0xFFFCE7F3), iconTint = Color(0xFFEC4899), label = "Rate the App", onClick = {})
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column {
                ProfileMenuItem(icon = Icons.AutoMirrored.Filled.Logout, iconBgColor = Color(0xFFFEE2E2), iconTint = AppColors.Danger, label = "Log Out", labelColor = AppColors.Danger, onClick = onLogoutClick)
                MenuDivider()
                ProfileMenuItem(icon = Icons.Default.DeleteForever, iconBgColor = Color(0xFFFEE2E2), iconTint = AppColors.Danger, label = "Delete Account", labelColor = AppColors.Danger, onClick = onDeleteAccountClick)
            }
        }

        Text(
            text = "v${getPlatform().appVersion} · SiteFlow",
            style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
            color = AppColors.TextTertiary.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

// ── Specialized Outlet Components ──

@Composable
private fun OutletIdentityCard(profile: AseProfile, outlet: OutletProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F8FF)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                // Badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = outlet.tier,
                            style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }
                    
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(6.dp).background(Color(0xFF22C55E), CircleShape))
                            Text(
                                text = outlet.status,
                                style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = Color(0xFF166534)
                            )
                        }
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    text = profile.name,
                    style = AppTypography.TitleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                    color = Color(0xFF1E293B)
                )
                
                Text(
                    text = outlet.ownerName,
                    style = AppTypography.BodySecondary.copy(fontSize = 14.sp),
                    color = Color(0xFF64748B)
                )
                
                Spacer(Modifier.height(12.dp))
                
                // Location chip — only shown when a real location is available
                if (profile.location.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color(0xFF3B82F6),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = profile.location,
                                style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                color = Color(0xFF475569)
                            )
                        }
                    }
                }
            }
            
            // Avatar with initials
            Box {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd)
                            )
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(AppColors.lightBlueFF),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.initials,
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.BlueGradientStart
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.BottomEnd)
                        .background(Color.White, CircleShape)
                        .padding(2.dp)
                        .background(Color(0xFF3B82F6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun OutletPerformanceCard(performance: OutletPerformance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.width(4.dp).height(16.dp).background(Color(0xFF1E293B), RoundedCornerShape(2.dp)))
                    Text(
                        text = "February 2026 Performance",
                        style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )
                }
                Text(
                    text = performance.lastUpdated,
                    style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF3B82F6)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                PerformanceMetricCard(
                    label = "Forecast Target",
                    value = "₹${performance.forecastTarget.toInt()}",
                    Modifier.weight(1f),
                    bgColor = Color(0xFFF8FAFC)
                )
                PerformanceMetricCard(
                    label = "Invoice Submitted",
                    value = "₹${performance.invoiceSubmitted.toInt()}",
                    Modifier.weight(1f),
                    bgColor = Color(0xFFEFF6FF),
                    labelColor = Color(0xFF3B82F6)
                )
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Variance Badge
                Box(
                    modifier = Modifier
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Variance: ${performance.variance}%",
                        style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFFEF4444)
                    )
                }
                
                // Eligibility Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Eligible",
                        style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF3B82F6)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Expected Payout Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expected Payout",
                    style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Medium),
                    color = Color(0xFF166534)
                )
                Text(
                    text = "₹${performance.expectedPayout.toInt()}",
                    style = AppTypography.TitleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF166534)
                )
            }
            
            Spacer(Modifier.height(16.dp))
            
            // CTA Button
            OutlinedButton(
                onClick = { /* TODO */ },
                modifier = Modifier.fillMaxWidth().height(44.dp),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("View Full Monthly Report", style = AppTypography.Button.copy(color = Color(0xFF1E293B)))
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF1E293B))
                }
            }
        }
    }
}

@Composable
private fun PerformanceMetricCard(label: String, value: String, modifier: Modifier, bgColor: Color, labelColor: Color = Color(0xFF64748B)) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Text(text = label, style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold), color = labelColor)
            Spacer(Modifier.height(4.dp))
            Text(text = value, style = AppTypography.TitleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold), color = Color(0xFF1E293B))
        }
    }
}

@Composable
private fun UpiPaymentCard(upiId: String, isVerified: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "UPI ID", style = AppTypography.Caption.copy(color = Color(0xFF94A3B8)))
                    Spacer(Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = upiId,
                            style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp),
                            color = Color(0xFF1E293B)
                        )
                        if (isVerified) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF0FDF4), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Verified",
                                    style = AppTypography.Caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF22C55E)
                                )
                            }
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color(0xFFEFF6FF), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = AppColors.BlueGradientStart,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = "Payouts will be credited to this UPI ID",
                    style = AppTypography.Caption.copy(fontSize = 11.sp),
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}

@Composable
private fun BankPaymentCard(bank: BankDetails) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(text = "Account Holder", style = AppTypography.Caption.copy(color = Color(0xFF94A3B8)))
                    Text(text = bank.accountHolder, style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E293B))
                }
                Box(
                    modifier = Modifier.size(36.dp).background(Color(0xFFF1F5F9), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(18.dp))
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Bank Name", style = AppTypography.Caption.copy(color = Color(0xFF94A3B8)))
                    Text(text = bank.bankName, style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E293B))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "IFSC Code", style = AppTypography.Caption.copy(color = Color(0xFF94A3B8)))
                    Text(text = bank.ifscCode, style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Bold), color = Color(0xFF1E293B))
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = bank.maskedAccountNumber, style = AppTypography.BodyPrimary.copy(letterSpacing = 2.sp), color = Color(0xFF1E293B))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (!bank.accountType.isNullOrBlank()) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFEFF6FF), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = bank.accountType.lowercase().replaceFirstChar { it.uppercase() },
                                style = AppTypography.Caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = AppColors.BlueGradientStart
                            )
                        }
                    }
                    if (bank.isVerified) {
                        Box(
                            modifier = Modifier.background(Color(0xFFF0FDF4), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(text = "Verified", style = AppTypography.Caption.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold), color = Color(0xFF22C55E))
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
        }
    }
}

@Composable
private fun DocumentItem(doc: DocumentEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(Color(0xFFFEF2F2), RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = null, tint = AppColors.Danger, modifier = Modifier.size(18.dp))
        }
        
        Text(
            text = doc.name,
            style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Medium),
            color = Color(0xFF1E293B),
            modifier = Modifier.weight(1f)
        )
        
        Box(
            modifier = Modifier.background(Color(0xFFF0FDF4), RoundedCornerShape(6.dp)).padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Text(text = doc.status, style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = Color(0xFF22C55E))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = AppTypography.Caption.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.4.sp
        ),
        color = AppColors.TextTertiary
    )
}

@Composable
private fun ProfileHeader(profile: AseProfile, currentRole: UserRole?) {
    val roleTitle = when(currentRole) {
        UserRole.CSO -> "Customer Sales Officer"
        UserRole.ASM -> "Area Sales Manager"
        UserRole.ASE -> "Area Sales Executive"
        UserRole.OUTLET -> "Outlet Owner"
        null -> "Customer Sales Officer"
    }

    Column(modifier = Modifier.padding(horizontal = 20.dp).padding(top = 4.dp, bottom = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F8FF)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Badges row
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFF1E293B), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = roleTitle,
                                style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFDCFCE7), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(Color(0xFF22C55E), CircleShape))
                                Text(
                                    text = "Active",
                                    style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = profile.name,
                        style = AppTypography.TitleMedium.copy(fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        color = Color(0xFF1E293B)
                    )


                    Spacer(Modifier.height(12.dp))

                    // Location chip — only shown when a real location is available
                    if (profile.location.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF3B82F6),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = profile.location,
                                    style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                                    color = Color(0xFF475569)
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.width(16.dp))

                // Avatar with initials
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd)
                            )
                        )
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(AppColors.lightBlueFF),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = profile.initials,
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AppColors.BlueGradientStart
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCardView(kpi: KpiCard, modifier: Modifier = Modifier) {
    val icon: ImageVector = when (kpi.icon) {
        KpiIcon.OUTLETS -> Icons.Default.Storefront
        KpiIcon.PFP -> Icons.Default.VerifiedUser
        KpiIcon.BILLING -> Icons.Default.CurrencyRupee
        KpiIcon.INCENTIVE -> Icons.Default.EmojiEvents
    }

    val trendIcon = when (kpi.trend) {
        KpiTrend.UP -> Icons.AutoMirrored.Filled.TrendingUp
        KpiTrend.DOWN -> Icons.AutoMirrored.Filled.TrendingDown
        KpiTrend.FLAT -> null
    }

    val trendColor = when (kpi.trend) {
        KpiTrend.UP -> AppColors.Success
        KpiTrend.DOWN -> AppColors.Danger
        KpiTrend.FLAT -> AppColors.TextTertiary
    }

    Card(
        modifier = modifier.border(0.5.dp, AppColors.greyF6, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(36.dp).background(kpi.iconBgColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = kpi.iconTintColor, modifier = Modifier.size(18.dp))
                }
                if (trendIcon != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        Icon(imageVector = trendIcon, contentDescription = null, tint = trendColor, modifier = Modifier.size(14.dp))
                        Text(text = "${kpi.trendPercent}%", style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.SemiBold), color = trendColor)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(text = kpi.value, style = AppTypography.TitleMedium.copy(fontSize = 22.sp, fontWeight = FontWeight.Bold), color = AppColors.black27)
            Spacer(Modifier.height(2.dp))
            Text(text = kpi.label, style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium), color = AppColors.TextTertiary, maxLines = 1)
        }
    }
}

@Composable
private fun MenuDivider() {
    HorizontalDivider(color = AppColors.greyF6, thickness = 1.dp, modifier = Modifier.padding(horizontal = 16.dp))
}

@Composable
private fun ProfileMenuItem(icon: ImageVector, iconBgColor: Color, iconTint: Color, label: String, labelColor: Color = AppColors.grey51, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(36.dp).background(iconBgColor, RoundedCornerShape(10.dp)), contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.width(14.dp))
        Text(text = label, style = AppTypography.BodyPrimary.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium), color = labelColor, modifier = Modifier.weight(1f))
        Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null, tint = AppColors.greyEB, modifier = Modifier.size(20.dp))
    }
}

// ── Logout Confirmation Dialog ──

@Composable
private fun LogoutConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFFEE2E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = AppColors.Danger,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "Log Out?",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1E293B)
            )
        },
        text = {
            Text(
                text = "You'll need to sign in again to access your account.",
                style = AppTypography.BodySecondary.copy(fontSize = 14.sp),
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Danger),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(
                    text = "Yes, Log Out",
                    style = AppTypography.Button.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = AppTypography.Button.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF475569)
                )
            }
        }
    )
}

// ── Delete Account Confirmation Dialog ──

@Composable
private fun DeleteAccountConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White,
        icon = {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Color(0xFFFEE2E2), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = null,
                    tint = AppColors.Danger,
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        title = {
            Text(
                text = "Delete Account?",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1E293B),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "This action is permanent and cannot be undone. All your data will be permanently deleted.",
                    style = AppTypography.BodySecondary.copy(fontSize = 14.sp),
                    color = Color(0xFF64748B),
                    textAlign = TextAlign.Center
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFEF2F2), RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Text(
                        text = "⚠ Your profile, invoices, and all associated data will be erased.",
                        style = AppTypography.Caption.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                        color = Color(0xFFDC2626),
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Danger),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(
                    text = "Yes, Delete My Account",
                    style = AppTypography.Button.copy(fontWeight = FontWeight.SemiBold),
                    color = Color.White
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth().height(44.dp)
            ) {
                Text(
                    text = "Cancel",
                    style = AppTypography.Button.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF475569)
                )
            }
        }
    )
}


@Composable
private fun PfpAgreementFullScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── Top bar ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF1E293B)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PFP Terms & Conditions",
                    style = AppTypography.TitleMedium.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF1E293B)
                )
                Text(
                    text = "Campa Destination Outlet Program",
                    style = AppTypography.Caption.copy(fontSize = 12.sp),
                    color = Color(0xFF94A3B8)
                )
            }
        }

        HorizontalDivider(color = Color(0xFFE2E8F0))

        // ── Scrollable content ──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // Meta info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AgreementMetaRow("Program", "Campa Destination Outlet Program")
                AgreementMetaRow("Facilitator", "Silveraxis Technologies Private Limited")
                AgreementMetaRow("Brand Owner", "Reliance Consumer Products Limited (RCPL)")
                AgreementMetaRow("Version", "1.0")
            }

            Spacer(Modifier.height(20.dp))

            // Clauses
            PFP_CLAUSES.forEach { clause ->
                AgreementClauseItem(clause)
                Spacer(Modifier.height(14.dp))
            }

            HorizontalDivider(color = Color(0xFFE2E8F0))
            Spacer(Modifier.height(20.dp))

            // Annexure 1
            AgreementAnnexureLabel("1", "Outlet Classification")
            Spacer(Modifier.height(8.dp))
            ProfileVolumeSlabTable()
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Payout applies on Beverage Category — Sparkling, Stills and Energy. Water is excluded.",
                color = Color(0xFF64748B),
                fontSize = 12.sp,
                lineHeight = 18.sp,
                fontStyle = FontStyle.Italic
            )

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AgreementMetaRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            color = Color(0xFF64748B),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.width(80.dp)
        )
        Text(
            text = value,
            color = Color(0xFF1E293B),
            fontSize = 12.sp,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun AgreementClauseItem(clause: AgreementClause) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .background(AppColors.BlueGradientStart, CircleShape)
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
                color = Color(0xFF1E293B),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 20.sp
            )
        }

        if (clause.text != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                text = clause.text,
                color = Color(0xFF64748B),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                modifier = Modifier.padding(start = 32.dp)
            )
        }

        if (clause.subClauses.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            Column(
                modifier = Modifier.padding(start = 32.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                clause.subClauses.forEach { (id, text) ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "(${id.substringAfter(".")})",
                            color = AppColors.BlueGradientStart,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.width(24.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = text,
                            color = Color(0xFF64748B),
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
private fun AgreementAnnexureLabel(number: String, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .background(Color(0xFF7C3AED).copy(alpha = 0.12f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "Annexure $number",
                color = Color(0xFF7C3AED),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text = title,
            color = Color(0xFF1E293B),
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun ProfileVolumeSlabTable() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
            .clip(RoundedCornerShape(10.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF8FAFC))
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Text("Slab", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("Volume (excl. water)", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.4f))
            Text("Payout/case", color = Color(0xFF64748B), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        HorizontalDivider(color = Color(0xFFE2E8F0))
        VOLUME_SLAB_TABLE.forEachIndexed { index, (tier, volume, payout) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (index % 2 == 0) Color.White else Color(0xFFF8FAFC))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(tier, color = Color(0xFF1E293B), fontSize = 13.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                Text(volume, color = Color(0xFF64748B), fontSize = 12.sp, modifier = Modifier.weight(1.4f))
                Text(payout, color = AppColors.BlueGradientStart, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            if (index < VOLUME_SLAB_TABLE.lastIndex) HorizontalDivider(color = Color(0xFFE2E8F0))
        }
    }
}

// ── QR Code Card ──

@Composable
private fun OutletQrCodeCard(
    outletId: String,
    outletName: String
) {
    val jsonPayload = """{"outletId":"$outletId","outletName":"$outletName"}"""

    val qrBitmap: ImageBitmap? = remember(jsonPayload) {
        try {
            val pngBytes = QRCode.ofSquares()
                .withSize(15)
                .build(jsonPayload)
                .renderToBytes()

            decodeToImageBitmap(pngBytes)
        } catch (e: Exception) {
            println("[QR] Error generating QR code: ${e.message}")
            null
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (qrBitmap != null) {
                // QR code container with white background
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color.White, RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = qrBitmap,
                        contentDescription = "Outlet QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Outlet name label
                Text(
                    text = outletName,
                    style = AppTypography.BodyPrimary.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color(0xFF1E293B),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(4.dp))

                // Outlet ID label
                Text(
                    text = "ID: $outletId",
                    style = AppTypography.Caption.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )


            } else {
                // Error state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Unable to generate QR code",
                        style = AppTypography.BodySecondary,
                        color = AppColors.TextTertiary
                    )
                }
            }
        }
    }
}
