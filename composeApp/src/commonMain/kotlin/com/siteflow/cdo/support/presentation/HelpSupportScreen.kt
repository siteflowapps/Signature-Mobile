package com.siteflow.cdo.support.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography
import com.siteflow.cdo.support.data.TicketItem
import com.siteflow.cdo.support.domain.HelpSupportAction
import com.siteflow.cdo.support.domain.HelpSupportEvent
import com.siteflow.cdo.support.domain.HelpSupportViewModel
import com.siteflow.cdo.support.domain.TicketFilter
import kotlinx.coroutines.flow.collectLatest
import org.koin.compose.koinInject

@Composable
fun HelpSupportScreen(
    onBack: () -> Unit,
    onRaiseTicket: () -> Unit,
    viewModel: HelpSupportViewModel = koinInject()
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.onAction(HelpSupportAction.LoadTickets)
    }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                HelpSupportEvent.NavigateBack -> onBack()
                HelpSupportEvent.NavigateToRaiseTicket -> onRaiseTicket()
            }
        }
    }

    val selectedTicket = state.selectedTicket
    if (selectedTicket != null) {
        TicketDetailSheet(
            ticket = selectedTicket,
            onDismiss = { viewModel.onAction(HelpSupportAction.DismissTicket) }
        )
    }

    Scaffold(
        topBar = {
            HelpSupportTopBar(onBack = onBack)
        },
        floatingActionButton = {
            NewTicketFab(onClick = onRaiseTicket)
        },
        containerColor = AppColors.ScreenBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary strip
            TicketSummaryStrip(
                total = state.tickets.size,
                open = state.openCount,
                closed = state.closedCount
            )

            // Filter tabs
            FilterRow(
                active = state.activeFilter,
                onSelect = { viewModel.onAction(HelpSupportAction.FilterChanged(it)) }
            )

            AnimatedContent(
                targetState = state.isLoading && state.tickets.isEmpty(),
                label = "list-state"
            ) { loading ->
                if (loading) {
                    LoadingPlaceholder()
                } else if (state.filteredTickets.isEmpty()) {
                    EmptyTicketsState(filter = state.activeFilter)
                } else {
                    TicketList(
                        tickets = state.filteredTickets,
                        hasMore = state.hasMore,
                        isLoadingMore = state.isLoading && state.tickets.isNotEmpty(),
                        onTicketClick = { viewModel.onAction(HelpSupportAction.SelectTicket(it)) },
                        onLoadMore = { viewModel.onAction(HelpSupportAction.LoadMore) }
                    )
                }
            }
        }
    }
}

// ── Top Bar ──────────────────────────────────────────────────────────────────

@Composable
private fun HelpSupportTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 8.dp, vertical = 8.dp),
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
                text = "Help & Support",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF1E293B)
            )
            Text(
                text = "Track and manage your support tickets",
                style = AppTypography.Caption.copy(fontSize = 12.sp),
                color = Color(0xFF94A3B8)
            )
        }
    }
}

// ── Summary Strip ──────────────────────────────────────────────────────────────

@Composable
private fun TicketSummaryStrip(total: Int, open: Int, closed: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SummaryChip(
            label = "Total",
            count = total,
            bgColor = Color(0xFFF1F5F9),
            textColor = Color(0xFF475569),
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Open",
            count = open,
            bgColor = Color(0xFFFFFBEB),
            textColor = Color(0xFFD97706),
            modifier = Modifier.weight(1f)
        )
        SummaryChip(
            label = "Resolved",
            count = closed,
            bgColor = Color(0xFFF0FDF4),
            textColor = Color(0xFF16A34A),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SummaryChip(
    label: String,
    count: Int,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(bgColor, RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                style = AppTypography.TitleMedium.copy(
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = textColor
            )
            Text(
                text = label,
                style = AppTypography.Caption.copy(fontSize = 11.sp, fontWeight = FontWeight.Medium),
                color = textColor.copy(alpha = 0.8f)
            )
        }
    }
}

// ── Filter Row ────────────────────────────────────────────────────────────────

@Composable
private fun FilterRow(active: TicketFilter, onSelect: (TicketFilter) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TicketFilter.entries.forEach { filter ->
            val selected = filter == active
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (selected)
                            Brush.linearGradient(listOf(AppColors.BlueGradientStart, AppColors.BlueGradientEnd))
                        else
                            Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFFF1F5F9)))
                    )
                    .clickable { onSelect(filter) }
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = filter.label,
                    style = AppTypography.Caption.copy(
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    color = if (selected) Color.White else Color(0xFF64748B)
                )
            }
        }
    }
}

// ── Ticket List ───────────────────────────────────────────────────────────────

@Composable
private fun TicketList(
    tickets: List<TicketItem>,
    hasMore: Boolean,
    isLoadingMore: Boolean,
    onTicketClick: (TicketItem) -> Unit,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState.layoutInfo) {
        val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@LaunchedEffect
        if (last >= tickets.size - 2 && hasMore && !isLoadingMore) {
            onLoadMore()
        }
    }

    LazyColumn(
        state = listState,
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(tickets, key = { it.id }) { ticket ->
            TicketCard(
                ticket = ticket,
                onClick = { onTicketClick(ticket) }
            )
        }
        if (isLoadingMore) {
            item {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = AppColors.BlueGradientStart,
                        strokeWidth = 2.dp
                    )
                }
            }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

// ── Ticket Card ───────────────────────────────────────────────────────────────

@Composable
private fun TicketCard(ticket: TicketItem, onClick: () -> Unit) {
    val isOpen = ticket.status == "OPEN"
    val accentColor = if (isOpen) Color(0xFFF59E0B) else Color(0xFF22C55E)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(IntrinsicSize.Max)
                    .background(accentColor, RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                    .defaultMinSize(minHeight = 80.dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 14.dp, end = 16.dp, top = 14.dp, bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = ticket.ticketNumber,
                        style = AppTypography.Caption.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        ),
                        color = Color(0xFF94A3B8)
                    )
                    TicketStatusBadge(isOpen = isOpen)
                }

                Spacer(Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryDot(category = ticket.category)
                    Text(
                        text = ticket.category,
                        style = AppTypography.BodyPrimary.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF1E293B)
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = ticket.description,
                    style = AppTypography.BodySecondary.copy(fontSize = 13.sp),
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFCBD5E1),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = formatTicketDate(ticket.createdAt),
                            style = AppTypography.Caption.copy(fontSize = 11.sp),
                            color = Color(0xFF94A3B8)
                        )
                    }
                    if (ticket.screenshotUrls.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = Color(0xFFCBD5E1),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${ticket.screenshotUrls.size}",
                                style = AppTypography.Caption.copy(fontSize = 11.sp),
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFCBD5E1),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// ── Status Badge ──────────────────────────────────────────────────────────────

@Composable
private fun TicketStatusBadge(isOpen: Boolean) {
    val bgColor = if (isOpen) Color(0xFFFFFBEB) else Color(0xFFF0FDF4)
    val textColor = if (isOpen) Color(0xFFD97706) else Color(0xFF16A34A)
    val dotColor = if (isOpen) Color(0xFFF59E0B) else Color(0xFF22C55E)
    val label = if (isOpen) "Open" else "Resolved"

    Box(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(modifier = Modifier.size(5.dp).background(dotColor, CircleShape))
            Text(
                text = label,
                style = AppTypography.Caption.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                color = textColor
            )
        }
    }
}

// ── Category Dot ──────────────────────────────────────────────────────────────

@Composable
private fun CategoryDot(category: String) {
    val color = when {
        category.contains("App", ignoreCase = true) -> Color(0xFF3B82F6)
        category.contains("Invoice", ignoreCase = true) -> Color(0xFF8B5CF6)
        category.contains("Payout", ignoreCase = true) -> Color(0xFF10B981)
        category.contains("Login", ignoreCase = true) || category.contains("OTP", ignoreCase = true) -> Color(0xFFEF4444)
        category.contains("Onboarding", ignoreCase = true) -> Color(0xFFF59E0B)
        category.contains("Performance", ignoreCase = true) -> Color(0xFF06B6D4)
        category.contains("Data", ignoreCase = true) -> Color(0xFFEC4899)
        else -> Color(0xFF94A3B8)
    }
    Box(modifier = Modifier.size(8.dp).background(color, CircleShape))
}

// ── FAB ───────────────────────────────────────────────────────────────────────

@Composable
private fun NewTicketFab(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = AppColors.BlueGradientStart,
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp),
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = "New Ticket",
                style = AppTypography.Button.copy(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                color = Color.White
            )
        }
    }
}

// ── Loading / Empty States ────────────────────────────────────────────────────

@Composable
private fun LoadingPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(4) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(Color(0xFFF1F5F9), RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
private fun EmptyTicketsState(filter: TicketFilter) {
    val message = when (filter) {
        TicketFilter.ALL -> "No tickets yet"
        TicketFilter.OPEN -> "No open tickets"
        TicketFilter.CLOSED -> "No resolved tickets"
    }
    val sub = when (filter) {
        TicketFilter.ALL -> "Tap New Ticket to report an issue"
        TicketFilter.OPEN -> "All your issues have been resolved"
        TicketFilter.CLOSED -> "You haven't had any resolved tickets yet"
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(Color(0xFFEFF6FF), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                    contentDescription = null,
                    tint = AppColors.BlueGradientStart,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = message,
                style = AppTypography.TitleMedium.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                color = Color(0xFF1E293B)
            )
            Text(
                text = sub,
                style = AppTypography.BodySecondary.copy(fontSize = 14.sp),
                color = Color(0xFF94A3B8)
            )
        }
    }
}

// ── Ticket Detail Bottom Sheet ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TicketDetailSheet(ticket: TicketItem, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    var copied by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 8.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .background(Color(0xFFE2E8F0), CircleShape)
            )
        }
    ) {
        val isOpen = ticket.status == "OPEN"

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // ── Sheet header ──
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = ticket.ticketNumber,
                            style = AppTypography.TitleMedium.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            ),
                            color = Color(0xFF1E293B)
                        )
                        AnimatedVisibility(visible = copied, enter = fadeIn(), exit = fadeOut()) {
                            Text(
                                text = "Copied!",
                                style = AppTypography.Caption.copy(fontSize = 11.sp),
                                color = Color(0xFF22C55E)
                            )
                        }
                    }
                    Text(
                        text = "Raised on ${formatTicketDate(ticket.createdAt)}",
                        style = AppTypography.Caption.copy(fontSize = 12.sp),
                        color = Color(0xFF94A3B8)
                    )
                }
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(ticket.ticketNumber))
                        copied = true
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(Color(0xFFF1F5F9), CircleShape)
                ) {
                    Icon(
                        imageVector = if (copied) Icons.Default.CheckCircle else Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = if (copied) Color(0xFF22C55E) else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 12.dp))

            // ── Scrollable content ──
            val scrollState = rememberScrollState()
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                    // Category + Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CategoryDot(category = ticket.category)
                            Text(
                                text = ticket.category,
                                style = AppTypography.BodyPrimary.copy(fontWeight = FontWeight.SemiBold, fontSize = 14.sp),
                                color = Color(0xFF1E293B)
                            )
                        }
                        TicketStatusBadge(isOpen = isOpen)
                    }

                    // Description
                    DetailSection(title = "Description") {
                        Text(
                            text = ticket.description,
                            style = AppTypography.BodySecondary.copy(fontSize = 14.sp, lineHeight = 22.sp),
                            color = Color(0xFF475569)
                        )
                    }

                    // Screenshots
                    if (ticket.screenshotUrls.isNotEmpty()) {
                        DetailSection(title = "Screenshots (${ticket.screenshotUrls.size})") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(end = 4.dp)
                            ) {
                                items(ticket.screenshotUrls) { url ->
                                    AsyncImage(
                                        model = url,
                                        contentDescription = "Screenshot",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(width = 160.dp, height = 120.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                            .background(Color(0xFFF8FAFC))
                                    )
                                }
                            }
                        }
                    }

                    // Resolution note (only if closed)
                    if (!isOpen && !ticket.resolutionNote.isNullOrBlank()) {
                        DetailSection(title = "Resolution") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF0FDF4), RoundedCornerShape(12.dp))
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF22C55E),
                                    modifier = Modifier.size(18.dp).padding(top = 2.dp)
                                )
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text(
                                        text = ticket.resolutionNote,
                                        style = AppTypography.BodySecondary.copy(fontSize = 13.sp, lineHeight = 20.sp),
                                        color = Color(0xFF166534)
                                    )
                                    if (!ticket.resolvedAt.isNullOrBlank()) {
                                        Text(
                                            text = "Resolved on ${formatTicketDate(ticket.resolvedAt)}",
                                            style = AppTypography.Caption.copy(fontSize = 11.sp),
                                            color = Color(0xFF4ADE80)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // App version footer
                    if (!ticket.appVersion.isNullOrBlank()) {
                        Text(
                            text = "Reported from app version ${ticket.appVersion}",
                            style = AppTypography.Caption.copy(fontSize = 11.sp),
                            color = Color(0xFFCBD5E1),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = AppTypography.Caption.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp
            ),
            color = Color(0xFF94A3B8)
        )
        content()
    }
}

// ── Date helper ───────────────────────────────────────────────────────────────

private fun formatTicketDate(isoDate: String?): String {
    if (isoDate.isNullOrBlank()) return "—"
    return try {
        val parts = isoDate.substring(0, 10).split("-")
        val month = when (parts[1]) {
            "01" -> "Jan"; "02" -> "Feb"; "03" -> "Mar"; "04" -> "Apr"
            "05" -> "May"; "06" -> "Jun"; "07" -> "Jul"; "08" -> "Aug"
            "09" -> "Sep"; "10" -> "Oct"; "11" -> "Nov"; "12" -> "Dec"
            else -> parts[1]
        }
        "${parts[2].trimStart('0')} $month ${parts[0]}"
    } catch (e: Exception) {
        isoDate.take(10)
    }
}
