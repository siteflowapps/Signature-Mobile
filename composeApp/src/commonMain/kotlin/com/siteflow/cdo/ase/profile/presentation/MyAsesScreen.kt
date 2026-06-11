package com.siteflow.cdo.ase.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.ase.profile.data.dto.AseUserDto
import com.siteflow.cdo.ase.profile.domain.MyAsesAction
import com.siteflow.cdo.ase.profile.domain.MyAsesViewModel
import com.siteflow.cdo.core.presentation.components.CdoListHeader
import com.siteflow.cdo.core.presentation.components.CdoTextField
import com.siteflow.cdo.core.presentation.components.state.EmptyState
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyAsesScreen(
    onBack: () -> Unit,
    viewModel: MyAsesViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()
    val pullToRefreshState = rememberPullToRefreshState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.ScreenBackground)
    ) {
        // Search Section
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            CdoTextField(
                value = state.searchQuery,
                placeholder = "Search by name or phone...",
                onValueChange = { viewModel.onAction(MyAsesAction.Search(it)) },
                leadingIconVector = Icons.Default.Search
            )
            
            Spacer(Modifier.height(12.dp))
            
            CdoListHeader(label = "MY TEAM (${state.filteredAses.size})")
        }

        // List Section
        Box(
            modifier = Modifier
                .weight(1f)
                .pullToRefresh(
                    isRefreshing = state.isRefreshing,
                    state = pullToRefreshState,
                    onRefresh = { viewModel.onAction(MyAsesAction.Refresh) }
                )
        ) {
            if (state.isLoading && state.ases.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.BlueGradientStart)
                }
            } else if (state.filteredAses.isEmpty()) {
                EmptyState(
                    icon = Icons.Default.Person,
                    title = if (state.searchQuery.isNotBlank()) "No ASE found for '${state.searchQuery}'" else "No members in your team yet",
                    subtitle = "All Area Sales Executives under your management will appear here"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.filteredAses) { ase ->
                        AseUserCard(ase)
                    }
                    item {
                        Spacer(Modifier.height(80.dp))
                    }
                }
            }

            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = state.isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.White,
                color = AppColors.BlueGradientStart
            )
        }
    }
}

@Composable
private fun AseUserCard(ase: AseUserDto) {
    val initials = ase.name.split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercase() }
        .joinToString("")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AppColors.CardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEFF6FF)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.Bold),
                    color = AppColors.BlueGradientStart
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ase.name,
                        style = AppTypography.TitleMedium.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        color = AppColors.black27
                    )
                    
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (ase.status == "ACTIVE") Color(0xFFD1FAE5) else Color(0xFFF3F4F6)
                    ) {
                        Text(
                            text = ase.status,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                            color = if (ase.status == "ACTIVE") AppColors.Success else AppColors.TextTertiary
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = AppColors.TextTertiary
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = ase.phone,
                        style = AppTypography.BodySecondary.copy(fontSize = 13.sp),
                        color = AppColors.TextSecondary
                    )
                }

                if (ase.createdAt != null) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Joined on ${ase.createdAt.substringBefore("T")}",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )
                }
            }
        }
    }
}
