package com.siteflow.cdo.ase.onboarding.data

// ── Photo Slot Model ──
data class PhotoSlot(
    val id: String,
    val label: String,
    val required: Boolean,
    val imagePath: String? = null,
    val capturedAt: String? = null,   // e.g. "15/03/2026 00:44"
    val gpsLabel: String? = null      // e.g. "12.8542, 77.5443"
)

fun defaultPhotoSlots() = listOf(
    PhotoSlot(id = "SHOP_FRONT", label = "Shop Front", required = true),
    PhotoSlot(id = "INSIDE_SHOP", label = "Inside Shop", required = true),
    PhotoSlot(id = "COOLER_AREA", label = "Cooler Space / Placement Area", required = false),
    PhotoSlot(id = "SHELF", label = "Shelf Visibility", required = false),
    PhotoSlot(id = "BRANDING", label = "Branding Opportunity", required = false)
)

enum class Classification(
    val label: String,
    val description: String,
    val range: String,
    val minVolume: Int
) {
    SILVER("Silver", "Standard outlets", "< 100 cs", 0),
    GOLD("Gold", "Medium volume", "100-150 cs", 100),
    DIAMOND("Diamond", "High volume", "150-200 cs", 150),
    PLATINUM("Platinum", "Premium outlets", "> 200 cs", 200)
}

data class GpsLocation(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val areaName: String = ""
)
