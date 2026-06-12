package com.siteflow.signature.outlet.invoices.data

/**
 * Master product catalog with token-based fuzzy matching.
 * Supports matching invoice product names from various distributors that use
 * different naming conventions for the same physical SKU.
 */
data class MasterProduct(
    val id: String,
    val displayName: String,
    val hsnCode: String,
    val defaultGstRate: Double,
    val matchTokens: List<String>  // lowercase normalized tokens for fuzzy matching
)

object ProductMasterCatalog {

    private val catalog = listOf(
        // Campa Cola
        MasterProduct(
            id = "campa_cola_200ml",
            displayName = "Campa Cola 200ml",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "cola", "200ml")
        ),
        MasterProduct(
            id = "campa_cola_500ml",
            displayName = "Campa Cola 500ml",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "cola", "500ml")
        ),
        MasterProduct(
            id = "campa_cola_1l",
            displayName = "Campa Cola 1L",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "cola", "1l")
        ),

        // Campa Lemon
        MasterProduct(
            id = "campa_lemon_200ml",
            displayName = "Campa Lemon 200ml",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "lemon", "200ml")
        ),
        MasterProduct(
            id = "campa_lemon_500ml",
            displayName = "Campa Lemon 500ml",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "lemon", "500ml")
        ),
        MasterProduct(
            id = "campa_lemon_1l",
            displayName = "Campa Lemon 1L",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "lemon", "1l")
        ),

        // Campa Orange
        MasterProduct(
            id = "campa_orange_200ml",
            displayName = "Campa Orange 200ml",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "orange", "200ml")
        ),
        MasterProduct(
            id = "campa_orange_500ml",
            displayName = "Campa Orange 500ml",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "orange", "500ml")
        ),
        MasterProduct(
            id = "campa_orange_1l",
            displayName = "Campa Orange 1L",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "orange", "1l")
        ),

        // Campa Power Up
        MasterProduct(
            id = "campa_powerup_200ml",
            displayName = "Campa Power Up 200ml",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "power", "up", "200ml")
        ),
        MasterProduct(
            id = "campa_powerup_500ml",
            displayName = "Campa Power Up 500ml",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "power", "up", "500ml")
        ),

        // Campa Energy
        MasterProduct(
            id = "campa_energy_150ml",
            displayName = "Campa Energy Berry Kick 150ml",
            hsnCode = "22021090",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "energy", "berry", "kick", "150ml")
        ),

        // Campa Jeera
        MasterProduct(
            id = "campa_jeera_150ml",
            displayName = "Campa Jeera 150ml",
            hsnCode = "22021010",
            defaultGstRate = 40.0,
            matchTokens = listOf("campa", "jeera", "150ml")
        ),

        // Raskik
        MasterProduct(
            id = "raskik_mango_150ml",
            displayName = "Raskik Mango 150ml",
            hsnCode = "22029920",
            defaultGstRate = 5.0,
            matchTokens = listOf("raskik", "mango", "150ml")
        ),
        MasterProduct(
            id = "raskik_nimbu_150ml",
            displayName = "Raskik Nimbu Paani 150ml",
            hsnCode = "22029920",
            defaultGstRate = 5.0,
            matchTokens = listOf("raskik", "nimbu", "150ml")
        ),

        // Independence (Drinking Water)
        MasterProduct(
            id = "independence_500ml",
            displayName = "Independence 500ml",
            hsnCode = "22011010",
            defaultGstRate = 5.0,
            matchTokens = listOf("independence", "500ml")
        ),
        MasterProduct(
            id = "independence_750ml",
            displayName = "Independence 750ml",
            hsnCode = "22011010",
            defaultGstRate = 5.0,
            matchTokens = listOf("independence", "750ml")
        ),
        MasterProduct(
            id = "independence_1l",
            displayName = "Independence 1L",
            hsnCode = "22011010",
            defaultGstRate = 5.0,
            matchTokens = listOf("independence", "1l")
        ),
        MasterProduct(
            id = "independence_1_5l",
            displayName = "Independence 1.5L",
            hsnCode = "22011010",
            defaultGstRate = 5.0,
            matchTokens = listOf("independence", "1.5l")
        ),

        // Suncrush
        MasterProduct(
            id = "suncrush_mango_200ml",
            displayName = "Suncrush Mango 200ml",
            hsnCode = "22029920",
            defaultGstRate = 5.0,
            matchTokens = listOf("suncrush", "mango", "200ml")
        ),
        MasterProduct(
            id = "suncrush_orange_200ml",
            displayName = "Suncrush Orange 200ml",
            hsnCode = "22029920",
            defaultGstRate = 5.0,
            matchTokens = listOf("suncrush", "orange", "200ml")
        ),
        MasterProduct(
            id = "suncrush_mixed_200ml",
            displayName = "Suncrush Mixed Fruit 200ml",
            hsnCode = "22029920",
            defaultGstRate = 5.0,
            matchTokens = listOf("suncrush", "mixed", "200ml")
        ),

        // Spinner
        MasterProduct(
            id = "spinner_lemon_150ml",
            displayName = "Spinner Lemon 150ml",
            hsnCode = "22021010",
            defaultGstRate = 18.0,
            matchTokens = listOf("spinner", "lemon", "150ml")
        ),
        MasterProduct(
            id = "spinner_orange_150ml",
            displayName = "Spinner Orange 150ml",
            hsnCode = "22021010",
            defaultGstRate = 18.0,
            matchTokens = listOf("spinner", "orange", "150ml")
        ),
        MasterProduct(
            id = "spinner_nitro_150ml",
            displayName = "Spinner Nitro Blue 150ml",
            hsnCode = "22021010",
            defaultGstRate = 18.0,
            matchTokens = listOf("spinner", "nitro", "150ml")
        ),

        // Energy Gold
        MasterProduct(
            id = "energy_gold_185ml",
            displayName = "Energy Gold Boost 185ml",
            hsnCode = "22021010",
            defaultGstRate = 28.0,
            matchTokens = listOf("energy", "gold", "boost", "185ml")
        )
    )

    /**
     * Finds the best matching product from the catalog based on OCR-extracted product name.
     * Uses token-based fuzzy matching with normalization for different invoice formats.
     *
     * Matching algorithm:
     * 1. Normalize OCR text: lowercase, normalize sizes, strip noise words
     * 2. Tokenize into words
     * 3. For each product: score = matched_tokens / product_tokens
     * 4. Return product with highest score >= 0.66, else null
     *
     * @param rawOcrName The product name extracted by OCR (may contain noise)
     * @return MasterProduct if matched (score >= 0.66), null otherwise
     */
    fun findBestMatch(rawOcrName: String): MasterProduct? {
        if (rawOcrName.isBlank()) return null

        val normalizedName = normalizeProductName(rawOcrName)
        val ocrTokens = normalizedName.split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        if (ocrTokens.isEmpty()) return null

        var bestMatch: MasterProduct? = null
        var bestScore = 0.0

        for (product in catalog) {
            val matchedTokens = product.matchTokens.count { token ->
                ocrTokens.any { ocrToken -> ocrToken.contains(token) || token.contains(ocrToken) }
            }
            val score = matchedTokens.toDouble() / product.matchTokens.size

            if (score > bestScore && score >= 0.66) {
                bestScore = score
                bestMatch = product
            }
        }

        return bestMatch
    }

    /**
     * Normalizes product name for fuzzy matching.
     * - Converts to lowercase
     * - Normalizes size formats (1ltr -> 1l, 150 ml -> 150ml, etc.)
     * - Removes noise words and package info
     */
    private fun normalizeProductName(name: String): String {
        var normalized = name.lowercase().trim()

        // Normalize size formats
        normalized = normalized
            .replace(Regex("1\\.?5\\s*l(tr|iter|itre)?"), "1.5l")
            .replace(Regex("1\\s*l(tr|iter|itre)?"), "1l")
            .replace(Regex("(\\d+)\\s*ml"), "$1ml")
            .replace(Regex("(\\d+)\\s*l"), "$1l")

        // Remove noise words (order matters — longer patterns first)
        val noiseWords = listOf(
            "flvrd", "flavoured", "flavored", "drink", "pet", "pkgd", "packaging",
            "can", "bottle", "pack", "packing", "box", "nox", "pic", "pcs", "packs", "no"
        )
        for (noise in noiseWords) {
            normalized = normalized.replace(Regex("\\b$noise\\b"), " ")
        }

        // Remove pack patterns like "12nox40/-", "30pic", "24pc"
        normalized = normalized.replace(Regex("\\d+\\s*nox\\s*\\d+[/-]*"), " ")
        normalized = normalized.replace(Regex("\\d+\\s*pic"), " ")
        normalized = normalized.replace(Regex("\\d+\\s*pc"), " ")

        // Remove common packaging size suffixes that follow product
        normalized = normalized.replace(Regex("\\(\\d+\\s*(pc|pcs|pieces)\\)"), " ")

        // Collapse multiple spaces
        normalized = normalized.replace(Regex("\\s+"), " ").trim()

        // Fix common OCR/invoice typos
        normalized = normalized.replace("independednce", "independence")

        // Fix OCR misreading 0 as O in size tokens: 20oml → 200ml, 50oml → 500ml
        normalized = normalized.replace(Regex("(\\d)o(\\d*ml)"), "$1" + "0" + "$2")

        return normalized
    }
}
