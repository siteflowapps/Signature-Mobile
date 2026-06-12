package com.siteflow.signature.outlet.invoices.domain

import com.siteflow.signature.core.domain.OcrResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for InvoiceOcrParser.parse() — line items, tax breakdown, and bill summary.
 * Tests against all 5 invoice formats.
 */
class InvoiceOcrParserLineItemTest {

    // ═════════════════════════════════════════════
    // Royal Enterprises — 12 items, clean table
    // ═════════════════════════════════════════════

    private val royalFullOcr = """
        Tax Invoice
        ROYAL ENTERPRISES
        Ground Floor NO 32/1-16 Mahadeshwara Nagar, Shiva Smitha Party
        Hall, Herohalli, Bengaluru Urban, Karnatka BANGALORE- 560091
        Phone no.: 8660098193 Email: royal.enterprises1922@gmail.com
        GSTIN: 29AMGPH0689A1Z6, State: 29-Karnataka
        Bill To
        RIZAL BAKERY& CONDIMENTS (RA)
        State: 29-Karnataka
        Invoice No. : 33989
        Date : 15-09-2025
        #  Item name  HSN/ SAC  Quantity  Unit  Price/ Unit  GST  Amount
        1  CAMPA COLA FLVRD DRINK 200ML PET  22021010  3  CASE  160.71  192.86 (40%)  675.00
        2  CAMPA LEMON FLVRD DRINK 200ML PET  22021010  3  CASE  160.71  192.86 (40%)  675.00
        3  CAMPA ORANGE FLVRD DRINK 200ML PET  22021010  3  CASE  160.71  192.86 (40%)  675.00
        4  CAMPA POWER UP 200ML PET  22021010  3  CASE  160.71  192.86 (40%)  675.00
        5  RASKIK MANGO150 ML PET  22029920  3  CASE  200.00  30.00 (5%)  630.00
        6  CAMPA COLA FLVRD DRINK 500ML PET  22021010  3  CASE  257.14  308.57 (40%)  1080.00
        7  CAMPA LEMON FLVRD DRINK 500ML PET  22021010  3  CASE  257.14  308.57 (40%)  1080.00
        8  CAMPA ORANGE FLVRD DRINK 500ML PET  22021010  3  CASE  257.14  308.57 (40%)  1080.00
        9  CAMPA POWER UP 500ML PET  22021010  3  CASE  257.14  308.57 (40%)  1080.00
        10  CAMPA ENERGY BERRY KICK 150 ML PET  22021090  4  CASE  150.00  240.00 (40%)  840.00
        11  RASKIK NIMBU PAANI PET 150ML  22029920  5  CASE  200.00  50.00 (5%)  1050.00
        12  CAMPA JEERA 150 ML PET  22021010  6  CASE  107.14  257.14 (40%)  900.00
        Total  42  2582.86  10440.00
        Tax type  Taxable amount  Rate  Tax amount  Amounts
        SGST  1600.00  2.5%  40.00  Sub Total  10440.00
        CGST  1600.00  2.5%  40.00  Total  10440.00
        SGST  6257.14  20%  1251.45  Balance  10440.00
        CGST  6257.14  20%  1251.45
    """.trimIndent()

    @Test
    fun royalParseExtractsItems() {
        val result = parseFullInvoice(royalFullOcr)
        assertTrue(result.skuItems.isNotEmpty(), "Should extract line items from Royal invoice")
        assertTrue(result.skuItems.size >= 5, "Should extract at least 5 items, got: ${result.skuItems.size}")
    }

    @Test
    fun royalParsePopulatesMetadata() {
        val result = parseFullInvoice(royalFullOcr)
        assertEquals("33989", result.invoiceNumber)
        assertEquals("ROYAL ENTERPRISES", result.distributorName)
        assertTrue(result.distributorGSTIN.isNotBlank(), "GSTIN should be populated")
    }

    @Test
    fun royalParseExtractsTaxBreakdown() {
        val result = parseFullInvoice(royalFullOcr)
        assertTrue(result.taxBreakdown.isNotEmpty(), "Should extract tax breakdown rows")
    }

    @Test
    fun royalParseExtractsSummary() {
        val result = parseFullInvoice(royalFullOcr)
        assertTrue(result.invoiceSummary.subTotal > 0 || result.invoiceSummary.grandTotal > 0,
            "Should extract bill summary amounts")
    }

    // ═════════════════════════════════════════════
    // Anu Shashi — 2 items, simple table
    // ═════════════════════════════════════════════

    private val anuShashiFullOcr = """
        Tax Invoice
        ANU SHASHI ENTERPRISES
        NO 106 1ST CROSS CHOWDESHWARI NAGAR LAGGERE BENGALURU URBAN KARNATAKA 560058
        Phone: 8660049465   Email: anushashibangalore@gmail.com
        GSTIN: 29BOGPA3522Q1ZP  State: 29-Karnataka
        Invoice No: 13513  CSO NAME: MOHAN
        Date: 17-02-2026
        Bill To:
        ANNAPOORNA CAKE PALACE AND SWEETS
        #  Item name  HSN/SAC  Quantity  Unit  MRP  Price/Unit  GST  Amount
        1  CAMPA COLA 200ML  22021010  1  Box  300.00  160.71  64.29 (40.0%)  225.00
        2  CAMPA LEMON 200ML  22021010  1  Box  300.00  160.71  64.29 (40.0%)  225.00
        Total  2  128.58  450.00
        Sub Total  450.00
        Total  450.00
        Received  0.00
        You Saved  150.00
    """.trimIndent()

    @Test
    fun anuShashiParseExtractsItems() {
        val result = parseFullInvoice(anuShashiFullOcr)
        assertTrue(result.skuItems.isNotEmpty(), "Should extract items from Anu Shashi")
    }

    @Test
    fun anuShashiItemsMatchCatalog() {
        val result = parseFullInvoice(anuShashiFullOcr)
        val matchedItems = result.skuItems.filter { it.isMatched }
        assertTrue(matchedItems.isNotEmpty(), "At least some items should match product catalog")
    }

    // ═════════════════════════════════════════════
    // Desire Foods — 11 items, has Round Off
    // ═════════════════════════════════════════════

    private val desireFullOcr = """
        Tax Invoice
        DESIRE FOOD AND BEVERAGES
        No 1, 1st main road, Nayandahalli, south layout, Bengaluru 560039
        GSTIN: 29MFYPS8859A1Z4, State: 29-Karnataka
        Bill To
        ALL BILAL HOTEL
        Date : 11-09-2025
        #  Item name  HSN/ SAC  Quantity  Unit  Price/ Unit  GST  Amount
        1  CAMPA COLA FLVRD DRINK 200ML PET  22021010  0  CASE  160.71  0.00 (40%)  0.00
        2  CAMPA LEMON FLVRD 200ML PET  22021010  10  CASE  160.71  642.86 (40%)  2250.00
        3  CAMPA ORANGE FLVRD 200ML PET  22021010  10  CASE  160.71  642.86 (40%)  2250.00
        4  SUNCRUSH MANGO 200ML PET  22029920  3  CASE  321.43  48.21 (5%)  1012.50
        5  SUNCRUSH MIXED FRUIT 200ML PET  22029920  3  CASE  339.29  50.89 (5%)  1068.75
        6  SUNCRUSH ORANGE 200ML PET  22029920  3  CASE  321.43  48.21 (5%)  1012.50
        7  CAMPA COLA FLVRD DRINK 500ML PET  22021010  3  CASE  257.14  308.57 (40%)  1080.00
        8  CAMPA LEMON FLVRD DRINK 500ML PET  22021010  3  CASE  257.14  308.57 (40%)  1080.00
        9  CAMPA ORANGE FLVRD 500ML PET  22021010  3  CASE  257.14  308.57 (40%)  1080.00
        10  INDEPENDENCE 750ML PET (24)  22011010  7  CASE  114.29  40.00 (5%)  840.00
        11  INDEPENDEDNCE 1.5L PET (12)  22011010  5  CASE  114.29  28.57 (5%)  600.00
        Total  50  2427.31  12273.75
        Sub Total  12273.75
        Round off  0.25
        Total  12274.00
        Received  0.00
        Balance  12274.00
    """.trimIndent()

    @Test
    fun desireParseExtractsItems() {
        val result = parseFullInvoice(desireFullOcr)
        assertTrue(result.skuItems.isNotEmpty(), "Should extract items from Desire Foods")
        assertTrue(result.skuItems.size >= 5, "Should extract at least 5 items, got: ${result.skuItems.size}")
    }

    @Test
    fun desireParseExtractsSummaryWithRoundOff() {
        val result = parseFullInvoice(desireFullOcr)
        // Verify summary captures Sub Total or Grand Total
        assertTrue(
            result.invoiceSummary.subTotal > 0 || result.invoiceSummary.grandTotal > 0,
            "Should capture summary amounts"
        )
    }

    // ═════════════════════════════════════════════
    // Jeevitha — size-before-brand naming
    // ═════════════════════════════════════════════

    private val jeevithaFullOcr = """
        Tax Invoice
        JEEVITHA DISTRIBUTORS
        NO 824 K U D A LAYOUT TAMAKA KOLAR
        Phone: 9036577626 9739128025
        Email: rameshcgowda626@gmail.com
        GSTIN: 29ARSPR8033R1ZH
        Invoice No: CS2526428
        Date: 18-02-2026
        Bill To:
        Gokul condiments
        #  Item name  HSN/SAC  Quantity  Unit  Price/Unit  GST  Amount
        1  200ML CAMPA ORANGE 30PIC  22021010  2  CS  160.71  128.57 (40.0%)  450.00
        2  200ML CAMPA LEMON 30PIC  22021010  3  CS  160.71  192.86 (40.0%)  675.00
        3  1.5L INDEPENDENCE 12PIC  22011010  5  CS  104.76  26.19 (5.0%)  550.00
        4  500ML CAMPA COLA 24PC  22021010  3  CS  261.43  313.71 (40.0%)  1098.00
        5  150ML RASKIK NIMBU PAANI 30PIC  22029920  2  CS  200.00  20.00 (5.0%)  420.00
        6  200ML SUNCRUSH MANGO  22029920  1  CS  342.86  17.14 (5.0%)  360.00
        7  200ML SUNCRUSH ORANGE  22029920  1  CS  371.43  18.57 (5.0%)  390.00
        Total  17  717.04  3943.00
        Sub Total  3943.00
        Received  3943.00
        Balance  0.00
    """.trimIndent()

    @Test
    fun jeevithaParseExtractsItems() {
        val result = parseFullInvoice(jeevithaFullOcr)
        assertTrue(result.skuItems.isNotEmpty(), "Should extract items from Jeevitha")
    }

    @Test
    fun jeevithaItemsMatchCatalogDespiteReversedFormat() {
        val result = parseFullInvoice(jeevithaFullOcr)
        val matchedItems = result.skuItems.filter { it.isMatched }
        assertTrue(matchedItems.isNotEmpty(),
            "Some items should match catalog despite size-before-brand format")
    }

    @Test
    fun jeevithaPopulatesFormDataFields() {
        val result = parseFullInvoice(jeevithaFullOcr)
        assertEquals("CS2526428", result.invoiceNumber)
        assertEquals("JEEVITHA DISTRIBUTORS", result.distributorName)
        assertTrue(result.billToName.isNotBlank(), "Bill-to should be populated in formData")
        assertTrue(result.distributorGSTIN.isNotBlank(), "GSTIN should be in formData")
        assertTrue(result.distributorEmail.isNotBlank(), "Email should be in formData")
    }

    // ═════════════════════════════════════════════
    // Prahar — embedded pack info, "No." units
    // ═════════════════════════════════════════════

    private val praharFullOcr = """
        Tax Invoice
        Prahar Enterprises
        No.31, Ground Floor, 1st Main, 11th Cross
        GSTIN/UIN: 29BDXPB2374P1ZO
        E-Mail : praharenterprises2023@gmail.com
        Invoice No.  Dated
        CC-01891/25-26  15-Sep-25
        Buyer (Bill to)
        Afzal Store (M)
        Sl No.  Description of Goods  GST Rate  Quantity  Rate  per  Amount
        1  CAMPA Cola Drink 1ltr Pet 12nox40/-  28 %  12 No.  23.57  No.  282.84
        2  CAMPA Orange Drink 1ltr Pet 12nox40/-  28 %  12 No.  23.57  No.  282.84
        3  CAMPA Lemon Drink 1ltr Pet 12nox40/-  28 %  12 No.  23.57  No.  282.84
        4  SPINNER Lemon 150ml 30nox10/-  18 %  12 No.
        5  SPINNER Nitro Blue 150ml 30nox10/-  18 %  12 No.
        6  SPINNER Orange 150ml 30nox10/-  18 %  12 No.
        7  Energy Gold Boost 185ml Can 24nox30/-  28 %  24 No.  17.86  No.  428.64
        Total  96 No.  1788.00
        CGST  178.81
        SGST  178.81
    """.trimIndent()

    @Test
    fun praharParseExtractsItems() {
        val result = parseFullInvoice(praharFullOcr)
        assertTrue(result.skuItems.isNotEmpty(), "Should extract items from Prahar")
    }

    // ═════════════════════════════════════════════
    // Helper
    // ═════════════════════════════════════════════

    private fun parseFullInvoice(text: String): com.siteflow.signature.outlet.invoices.data.UploadInvoiceFormData {
        val lines = text.lines().map { it.trim() }.filter { it.isNotBlank() }
        val ocrResult = OcrResult(rawText = text, lines = lines)
        return InvoiceOcrParser.parse(ocrResult)
    }
}
