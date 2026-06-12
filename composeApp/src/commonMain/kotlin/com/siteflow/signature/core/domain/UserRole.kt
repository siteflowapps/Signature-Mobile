package com.siteflow.signature.core.domain

/**
 * App roles, ordered by field hierarchy: CSO (field onboarding) -> ASE (L1
 * approver) -> ASM (L2 approver). OUTLET is the retailer (backend role
 * RETAILER, aliased in [com.siteflow.signature.core.data.networking.util.JwtUtils]).
 */
enum class UserRole {
    CSO,
    ASE,
    ASM,
    OUTLET
}
