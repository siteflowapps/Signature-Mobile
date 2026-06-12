package com.siteflow.signature.cso.compliance.domain

import com.siteflow.signature.cso.onboarding.data.PhotoSlot

sealed interface ComplianceAction {
    data class SerialNoChanged(val value: String) : ComplianceAction
    data class CoolerInstalledChanged(val value: Boolean) : ComplianceAction
    data class CoolerTypeChanged(val value: String) : ComplianceAction
    data class CapacityChanged(val value: String) : ComplianceAction
    data class SignageInstalledChanged(val value: Boolean) : ComplianceAction
    data class PhotoCaptured(val slotId: String, val imagePath: String) : ComplianceAction
    data class PhotoRemoved(val slotId: String) : ComplianceAction
    object Submit : ComplianceAction
}

data class ComplianceState(
    val outletId: String = "",
    val coolerInstalled: Boolean = true,
    val serialNo: String = "",
    val coolerType: String = "",
    val capacity: String = "",
    val signageInstalled: Boolean = true,
    val photoSlots: List<PhotoSlot> = listOf(
        PhotoSlot(id = "coolerImage",       label = "Cooler Photo",        required = true),
        PhotoSlot(id = "assetLabelImage",   label = "Asset Label Photo",   required = true),
        PhotoSlot(id = "signageImage",      label = "Signage Photo",       required = true),
        PhotoSlot(id = "outerOutletImage",  label = "Outer Outlet Photo",  required = true),
        PhotoSlot(id = "innerOutletImage",  label = "Inner Outlet Photo",  required = true)
    ),
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val capturedCount: Int get() = photoSlots.count { it.imagePath != null }
    val allPhotosCaptured: Boolean get() = photoSlots.all { it.imagePath != null }
    val isFormValid: Boolean get() = serialNo.isNotBlank() && coolerType.isNotBlank() && capacity.isNotBlank() && allPhotosCaptured
}

sealed interface ComplianceEvent {
    object SubmitSuccess : ComplianceEvent
    object NavigateBack : ComplianceEvent
}
