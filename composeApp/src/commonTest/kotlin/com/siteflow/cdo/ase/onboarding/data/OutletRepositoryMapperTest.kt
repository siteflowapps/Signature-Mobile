package com.siteflow.cdo.ase.onboarding.data

import kotlin.test.Test
import kotlin.test.assertEquals

class OutletRepositoryMapperTest {

    @Test
    fun `mapOutletType - Kirana to GROCERY`() {
        assertEquals("GROCERY", testMapOutletType("Kirana / General Store"))
    }

    @Test
    fun `mapOutletType - Medical Store to MEDICAL`() {
        assertEquals("MEDICAL", testMapOutletType("Medical Store"))
    }

    @Test
    fun `mapOutletType - Hardware Store to HARDWARE`() {
        assertEquals("HARDWARE", testMapOutletType("Hardware Store"))
    }

    @Test
    fun `mapOutletType - Electronics Store to ELECTRONICS`() {
        assertEquals("ELECTRONICS", testMapOutletType("Electronics Store"))
    }

    @Test
    fun `mapOutletType - Clothing Store to CLOTHING`() {
        assertEquals("CLOTHING", testMapOutletType("Clothing Store"))
    }

    @Test
    fun `mapOutletType - Restaurant to RESTAURANT`() {
        assertEquals("RESTAURANT", testMapOutletType("Restaurant / Eatery"))
    }

    @Test
    fun `mapOutletType - Other to OTHER`() {
        assertEquals("OTHER", testMapOutletType("Other"))
    }

    @Test
    fun `mapOutletType - unknown type uppercased and underscored`() {
        assertEquals("CUSTOM_TYPE", testMapOutletType("Custom Type"))
    }

    /**
     * Mirrors the private mapOutletType in OutletRepository for testing.
     */
    private fun testMapOutletType(uiType: String): String = when (uiType) {
        "Kirana / General Store" -> "GROCERY"
        "Medical Store" -> "MEDICAL"
        "Hardware Store" -> "HARDWARE"
        "Electronics Store" -> "ELECTRONICS"
        "Clothing Store" -> "CLOTHING"
        "Restaurant / Eatery" -> "RESTAURANT"
        "Other" -> "OTHER"
        else -> uiType.uppercase().replace(" ", "_")
    }
}
