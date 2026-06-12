package com.siteflow.signature.cso.profile.domain

import com.siteflow.signature.cso.profile.data.UserRepository
import com.siteflow.signature.core.data.networking.result.onError
import com.siteflow.signature.core.data.networking.result.onSuccess
import com.siteflow.signature.core.presentation.BaseViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MyAsesViewModel(
    private val scope: CoroutineScope,
    private val userRepository: UserRepository
) : BaseViewModel<MyAsesState, MyAsesAction, MyAsesEvent>(
    MyAsesState()
) {

    init {
        loadTeam()
    }

    override fun onAction(action: MyAsesAction) {
        when (action) {
            MyAsesAction.LoadTeam -> loadTeam()
            MyAsesAction.Refresh -> refresh()
            is MyAsesAction.Search -> search(action.query)
        }
    }

    private fun loadTeam() {
        scope.launch {
            updateState { it.copy(isLoading = true, error = null) }
            userRepository.getMyAses()
                .onSuccess { response ->
                    val ases = response.data?.content ?: emptyList()
                    updateState {
                        it.copy(
                            ases = ases,
                            filteredAses = ases,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
                .onError { error ->
                    updateState {
                        it.copy(
                            isLoading = false,
                            isRefreshing = false,
                            error = error.message
                        )
                    }
                }
        }
    }

    private fun refresh() {
        updateState { it.copy(isRefreshing = true) }
        loadTeam()
    }

    private fun search(query: String) {
        updateState { state ->
            val filtered = if (query.isBlank()) {
                state.ases
            } else {
                state.ases.filter { ase ->
                    ase.name.contains(query, ignoreCase = true) ||
                    ase.phone.contains(query)
                }
            }
            state.copy(
                searchQuery = query,
                filteredAses = filtered
            )
        }
    }
}
