package com.siteflow.signature.support.domain

import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.presentation.BaseViewModel
import com.siteflow.signature.core.presentation.components.toast.GlobalToastHandler
import com.siteflow.signature.support.data.SupportRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class HelpSupportViewModel(
    private val scope: CoroutineScope,
    private val repository: SupportRepository
) : BaseViewModel<HelpSupportState, HelpSupportAction, HelpSupportEvent>(HelpSupportState()) {

    override fun onAction(action: HelpSupportAction) {
        when (action) {
            HelpSupportAction.LoadTickets -> loadPage(0, replace = true)
            HelpSupportAction.Refresh -> refresh()
            HelpSupportAction.LoadMore -> {
                val s = state.value
                if (!s.isLoading && s.hasMore) loadPage(s.page + 1, replace = false)
            }
            is HelpSupportAction.SelectTicket -> updateState { it.copy(selectedTicket = action.ticket) }
            HelpSupportAction.DismissTicket -> updateState { it.copy(selectedTicket = null) }
            is HelpSupportAction.FilterChanged -> updateState { it.copy(activeFilter = action.filter) }
        }
    }

    private fun refresh() {
        updateState { it.copy(isRefreshing = true) }
        loadPage(0, replace = true, isRefresh = true)
    }

    private fun loadPage(page: Int, replace: Boolean, isRefresh: Boolean = false) {
        scope.launch {
            if (!isRefresh) updateState { it.copy(isLoading = true, error = null) }

            repository.getMyTickets(page = page)
                .onSuccess { response ->
                    val incoming = response.data?.content ?: emptyList()
                    updateState { s ->
                        val merged = if (replace) incoming else s.tickets + incoming
                        s.copy(
                            tickets = merged,
                            isLoading = false,
                            isRefreshing = false,
                            page = response.data?.page ?: 0,
                            hasMore = response.data?.last == false,
                            error = null
                        )
                    }
                }
                .onError { error ->
                    updateState { it.copy(isLoading = false, isRefreshing = false, error = error.message) }
                    GlobalToastHandler.showError(error.message)
                }
        }
    }
}
