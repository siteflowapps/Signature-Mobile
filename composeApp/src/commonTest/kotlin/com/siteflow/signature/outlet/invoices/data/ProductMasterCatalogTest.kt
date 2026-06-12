package com.siteflow.signature.outlet.invoices.data

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Unit tests for ProductMasterCatalog fuzzy matching
 * across all 5 invoice naming formats.
 */
class ProductMasterCatalogTest {

    // ═════════════════════════════════════════════
    // Royal Enterprises format: "CAMPA COLA FLVRD DRINK 200ML PET"
    // ═════════════════════════════════════════════

    @Test
    fun matchRoyalCampaColaFlvrdDrink200ml() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA COLA FLVRD DRINK 200ML PET")
        assertNotNull(match, "Should match Campa Cola 200ml")
        assertEquals("campa_cola_200ml", match.id)
    }

    @Test
    fun matchRoyalCampaLemonFlvrdDrink200ml() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA LEMON FLVRD DRINK 200ML PET")
        assertNotNull(match)
        assertEquals("campa_lemon_200ml", match.id)
    }

    @Test
    fun matchRoyalCampaOrangeFlvrdDrink200ml() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA ORANGE FLVRD DRINK 200ML PET")
        assertNotNull(match)
        assertEquals("campa_orange_200ml", match.id)
    }

    @Test
    fun matchRoyalCampaPowerUp200ml() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA POWER UP 200ML PET")
        assertNotNull(match)
        assertEquals("campa_powerup_200ml", match.id)
    }

    @Test
    fun matchRoyalRaskikMango150ml() {
        val match = ProductMasterCatalog.findBestMatch("RASKIK MANGO150 ML PET")
        assertNotNull(match)
        assertEquals("raskik_mango_150ml", match.id)
    }

    @Test
    fun matchRoyalCampaEnergyBerryKick150ml() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA ENERGY BERRY KICK 150 ML PET")
        assertNotNull(match)
        assertEquals("campa_energy_150ml", match.id)
    }

    @Test
    fun matchRoyalRaskikNimbuPaani150ml() {
        val match = ProductMasterCatalog.findBestMatch("RASKIK NIMBU PAANI PET 150ML")
        assertNotNull(match)
        assertEquals("raskik_nimbu_150ml", match.id)
    }

    @Test
    fun matchRoyalCampaJeera150ml() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA JEERA 150 ML PET")
        assertNotNull(match)
        assertEquals("campa_jeera_150ml", match.id)
    }

    @Test
    fun matchRoyalCampaColaFlvrdDrink500ml() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA COLA FLVRD DRINK 500ML PET")
        assertNotNull(match)
        assertEquals("campa_cola_500ml", match.id)
    }

    // ═════════════════════════════════════════════
    // Anu Shashi format: "CAMPA COLA 200ML"
    // ═════════════════════════════════════════════

    @Test
    fun matchAnuShashiCampaCola200ml() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA COLA 200ML")
        assertNotNull(match)
        assertEquals("campa_cola_200ml", match.id)
    }

    @Test
    fun matchAnuShashiCampaLemon200ml() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA LEMON 200ML")
        assertNotNull(match)
        assertEquals("campa_lemon_200ml", match.id)
    }

    // ═════════════════════════════════════════════
    // Prahar format: "CAMPA Cola Drink 1ltr Pet 12nox40/-"
    // ═════════════════════════════════════════════

    @Test
    fun matchPraharCampaCola1ltr() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA Cola Drink 1ltr Pet 12nox40/-")
        assertNotNull(match)
        assertEquals("campa_cola_1l", match.id)
    }

    @Test
    fun matchPraharCampaOrange1ltr() {
        val match = ProductMasterCatalog.findBestMatch("CAMPA Orange Drink 1ltr Pet 12nox40/-")
        assertNotNull(match)
        assertEquals("campa_orange_1l", match.id)
    }

    @Test
    fun matchPraharSpinnerLemon150ml() {
        val match = ProductMasterCatalog.findBestMatch("SPINNER Lemon 150ml 30nox10/-")
        assertNotNull(match)
        assertEquals("spinner_lemon_150ml", match.id)
    }

    @Test
    fun matchPraharSpinnerNitroBlue150ml() {
        val match = ProductMasterCatalog.findBestMatch("SPINNER Nitro Blue 150ml 30nox10/-")
        assertNotNull(match)
        assertEquals("spinner_nitro_150ml", match.id)
    }

    @Test
    fun matchPraharEnergyGoldBoost185ml() {
        val match = ProductMasterCatalog.findBestMatch("Energy Gold Boost 185ml Can 24nox30/-")
        assertNotNull(match)
        assertEquals("energy_gold_185ml", match.id)
    }

    // ═════════════════════════════════════════════
    // Jeevitha format: "200ML CAMPA ORANGE 30PIC"
    // ═════════════════════════════════════════════

    @Test
    fun matchJeevithaCampaOrange200ml() {
        val match = ProductMasterCatalog.findBestMatch("200ML CAMPA ORANGE 30PIC")
        assertNotNull(match)
        assertEquals("campa_orange_200ml", match.id)
    }

    @Test
    fun matchJeevithaCampaLemon200ml() {
        val match = ProductMasterCatalog.findBestMatch("200ML CAMPA LEMON 30PIC")
        assertNotNull(match)
        assertEquals("campa_lemon_200ml", match.id)
    }

    @Test
    fun matchJeevithaIndependence1_5L() {
        val match = ProductMasterCatalog.findBestMatch("1.5L INDEPENDENCE 12PIC")
        assertNotNull(match)
        assertEquals("independence_1_5l", match.id)
    }

    @Test
    fun matchJeevithaSuncrushMango200ml() {
        val match = ProductMasterCatalog.findBestMatch("200ML SUNCRUSH MANGO")
        assertNotNull(match)
        assertEquals("suncrush_mango_200ml", match.id)
    }

    @Test
    fun matchJeevithaSuncrushOrange200ml() {
        val match = ProductMasterCatalog.findBestMatch("200ML SUNCRUSH ORANGE")
        assertNotNull(match)
        assertEquals("suncrush_orange_200ml", match.id)
    }

    // ═════════════════════════════════════════════
    // Desire Foods format: same as Royal
    // ═════════════════════════════════════════════

    @Test
    fun matchDesireSuncrushMango200ml() {
        val match = ProductMasterCatalog.findBestMatch("SUNCRUSH MANGO 200ML PET")
        assertNotNull(match)
        assertEquals("suncrush_mango_200ml", match.id)
    }

    @Test
    fun matchDesireSuncrushMixedFruit200ml() {
        val match = ProductMasterCatalog.findBestMatch("SUNCRUSH MIXED FRUIT 200ML PET")
        assertNotNull(match)
        assertEquals("suncrush_mixed_200ml", match.id)
    }

    @Test
    fun matchDesireIndependence750ml() {
        val match = ProductMasterCatalog.findBestMatch("INDEPENDENCE 750ML PET (24)")
        assertNotNull(match)
        assertEquals("independence_750ml", match.id)
    }

    @Test
    fun matchDesireIndependence1_5L() {
        val match = ProductMasterCatalog.findBestMatch("INDEPENDEDNCE 1.5L PET (12)")
        assertNotNull(match)
        // Typo in invoice ("INDEPENDEDNCE") — verify fuzzy matching handles it
        assertEquals("independence_1_5l", match.id)
    }

    // ═════════════════════════════════════════════
    // Edge Cases
    // ═════════════════════════════════════════════

    @Test
    fun returnNullForUnknownProduct() {
        val match = ProductMasterCatalog.findBestMatch("TOTALLY UNKNOWN PRODUCT XYZ")
        assertNull(match, "Unknown product should return null")
    }

    @Test
    fun returnNullForBlankInput() {
        val match = ProductMasterCatalog.findBestMatch("")
        assertNull(match, "Blank input should return null")
    }

    @Test
    fun matchJeevitha500mlCampaCola() {
        val match = ProductMasterCatalog.findBestMatch("500ML CAMPA COLA 24PC")
        assertNotNull(match)
        assertEquals("campa_cola_500ml", match.id)
    }
}
