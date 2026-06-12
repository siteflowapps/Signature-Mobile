package com.siteflow.signature.support.domain

import com.siteflow.signature.support.data.TicketItem

enum class TicketFilter(val label: String) {
    ALL("All"),
    OPEN("Open"),
    CLOSED("Closed")
}

data class HelpSupportState(
    val tickets: List<TicketItem> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val selectedTicket: TicketItem? = null,
    val activeFilter: TicketFilter = TicketFilter.ALL,
    val page: Int = 0,
    val hasMore: Boolean = false
) {
    val filteredTickets: List<TicketItem> get() = when (activeFilter) {
        TicketFilter.ALL -> tickets
        TicketFilter.OPEN -> tickets.filter { it.status == "OPEN" }
        TicketFilter.CLOSED -> tickets.filter { it.status == "CLOSED" }
    }

    val openCount: Int get() = tickets.count { it.status == "OPEN" }
    val closedCount: Int get() = tickets.count { it.status == "CLOSED" }
}

sealed interface HelpSupportAction {
    data object LoadTickets : HelpSupportAction
    data object Refresh : HelpSupportAction
    data object LoadMore : HelpSupportAction
    data class SelectTicket(val ticket: TicketItem) : HelpSupportAction
    data object DismissTicket : HelpSupportAction
    data class FilterChanged(val filter: TicketFilter) : HelpSupportAction
}

sealed interface HelpSupportEvent {
    data object NavigateToRaiseTicket : HelpSupportEvent
    data object NavigateBack : HelpSupportEvent
}
