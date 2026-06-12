package com.siteflow.signature.shared.data

/**
 * Multi-level approval hierarchy used across the Signature platform.
 *
 * L1 = ASE (field submission)
 * L2 = ASM (area manager review)
 * L3 = Finance (web portal – final settlement)
 */
enum class ApprovalLevel(val label: String, val shortLabel: String) {
    L1_ASE("ASE Approval", "L1"),
    L2_ASM("ASM Approval", "L2"),
    L3_FINANCE("Finance Approval", "L3")
}

/**
 * Status of a single step in the approval chain.
 */
enum class ApprovalStepStatus {
    PENDING,
    APPROVED,
    REJECTED,
    NOT_STARTED
}

/**
 * A single step in the multi-level approval timeline.
 */
data class ApprovalStep(
    val level: ApprovalLevel,
    val approverName: String? = null,
    val status: ApprovalStepStatus = ApprovalStepStatus.NOT_STARTED,
    val timestamp: String? = null,
    val note: String? = null
)
