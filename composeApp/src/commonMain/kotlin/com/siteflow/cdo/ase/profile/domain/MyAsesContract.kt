package com.siteflow.cdo.ase.profile.domain

import com.siteflow.cdo.ase.profile.data.dto.AseUserDto

sealed interface MyAsesAction {
    data object LoadTeam : MyAsesAction
    data object Refresh : MyAsesAction
    data class Search(val query: String) : MyAsesAction
}

data class MyAsesState(
    val ases: List<AseUserDto> = emptyList(),
    val filteredAses: List<AseUserDto> = emptyList(),
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val error: String? = null
)

sealed interface MyAsesEvent {
    data class ShowError(val message: String) : MyAsesEvent
}
