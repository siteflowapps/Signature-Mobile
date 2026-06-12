package com.siteflow.signature.outlet.invoices.data

import com.siteflow.signature.core.util.toTitleCase

import androidx.compose.ui.graphics.Color
import com.siteflow.signature.cso.dashboard.data.OutletSlab
import com.siteflow.signature.cso.invoices.data.InvoiceItem
import com.siteflow.signature.cso.invoices.data.InvoiceLineItem
import com.siteflow.signature.cso.invoices.data.InvoiceStatus
import com.siteflow.signature.cso.invoices.data.SlabQualification
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.outlet.dashboard.data.OutletRecentInvoice

/**
 * Maps API status string to the InvoiceStatus enum.
 */
fun mapInvoiceStatus(status: String?): InvoiceStatus {
    return when (status?.uppercase()) {
        "SUBMITTED" -> InvoiceStatus.SUBMITTED
        "ASE_APPROVED" -> InvoiceStatus.ASE_APPROVED
        "ASM_APPROVED" -> InvoiceStatus.ASM_APPROVED
        "FINANCE_APPROVED" -> InvoiceStatus.FINANCE_APPROVED
        "CALCULATED" -> InvoiceStatus.CALCULATED
        "PAID" -> InvoiceStatus.PAID
        "REJECTED" -> InvoiceStatus.REJECTED
        "APPROVED" -> InvoiceStatus.APPROVED
        else -> InvoiceStatus.PENDING
    }
}

/**
 * Maps an API [InvoiceDto] to the shared [InvoiceItem] used by all list screens.
 */
fun InvoiceDto.toInvoiceItem(): InvoiceItem {
    val mappedStatus = mapInvoiceStatus(status)
    val formattedAmount = "₹${(totalAmount ?: 0.0).toInt()}"

    val lineItems = items.map { item ->
        InvoiceLineItem(
            skuName = item.skuName,
            quantity = item.invoicedQuantity.toString(),
            unit = item.invoicedUnit,
            unitPrice = item.invoicedUnitPrice,
            totalPrice = item.invoicedTotalPrice,
            matchedSkuName = item.matchedSkuName,
            invoicedSkuName = item.invoicedSkuName,
            caseConfiguration = item.caseConfiguration,
            mrpPerCase = item.mrpPerCase,
            mrpPerBottle = item.mrpPerBottle,
            finalQuantity = item.finalQuantity,
            finalUnit = item.finalUnit
        )
    }

    return InvoiceItem(
        id = id,
        outletName = (outletName ?: "Unknown Outlet").toTitleCase(),
        initials = (outletName ?: "U").take(2).uppercase(),
        slab = OutletSlab.SILVER,
        location = distributorName ?: "",
        status = mappedStatus,
        invoiceAmount = formattedAmount,
        invoicePeriod = invoiceDate ?: "",
        lineItemCount = lineItems.size,
        slabQualification = SlabQualification.MEETS,
        submittedTime = formatUploadDate(uploadDate),
        invoiceImageUrl = photoUrl,
        lineItems = lineItems,
        invoiceNumber = invoiceNumber ?: "",
        distributorName = distributorName ?: "",
        totalQuantity = quantity ?: 0.0,
        submittedByAse = ""
    )
}

/**
 * Maps an API [InvoiceDto] to [OutletRecentInvoice] for the Outlet dashboard.
 */
fun InvoiceDto.toOutletRecentInvoice(): OutletRecentInvoice {
    val mappedStatus = mapInvoiceStatus(status)
    val formattedAmount = "₹${(totalAmount ?: 0.0).toInt()}"

    return OutletRecentInvoice(
        id = id,
        title = invoiceNumber ?: "Invoice",
        subtitle = formatUploadDate(uploadDate),
        amount = formattedAmount,
        status = mappedStatus,
        accentColor = when (mappedStatus) {
            InvoiceStatus.APPROVED, InvoiceStatus.FINANCE_APPROVED, InvoiceStatus.PAID -> AppColors.Success
            InvoiceStatus.REJECTED -> AppColors.Danger
            InvoiceStatus.ASE_APPROVED, InvoiceStatus.ASM_APPROVED -> AppColors.BlueGradientStart
            InvoiceStatus.SUBMITTED, InvoiceStatus.PENDING, InvoiceStatus.CALCULATED -> Color(0xFFF59E0B)
        }
    )
}

/**
 * Format ISO upload date to a human-readable relative string.
 */
private fun formatUploadDate(uploadDate: String?): String {
    if (uploadDate == null) return "Recently"
    // Show just the date portion for now
    return try {
        val datePart = uploadDate.substringBefore("T")
        datePart
    } catch (_: Exception) {
        "Recently"
    }
}

/**
 * Converts an [InvoiceLineItem] (from the detail/list model) to a [SkuLineItem]
 * (from the upload/review model) so both ASE/ASM and Outlet screens can reuse
 * the [MatchedItemCard] composable for SKU display.
 */
fun InvoiceLineItem.toSkuLineItem(): SkuLineItem {
    val effectiveQuantity = if (finalQuantity > 0) finalQuantity else (try { quantity.toInt() } catch (_: Exception) { 0 })

    // MRP Revenue = mrpPerCase * quantity (cases)
    val mrpRevenue = if (mrpPerCase != null && mrpPerCase > 0) {
        mrpPerCase * effectiveQuantity
    } else {
        totalPrice // fallback to API total if MRP data unavailable
    }

    return SkuLineItem(
        productName = matchedSkuName ?: skuName,
        invoicedSkuName = invoicedSkuName,
        matchedSkuName = matchedSkuName,
        quantity = try { quantity.toInt() } catch (_: Exception) { 0 },
        unit = unit,
        pricePerUnit = unitPrice,
        totalPrice = mrpRevenue,
        caseConfiguration = caseConfiguration,
        mrpPerCase = mrpPerCase,
        mrpPerBottle = mrpPerBottle ?: if (unitPrice > 0) unitPrice else null,
        finalQuantity = effectiveQuantity,
        finalUnit = finalUnit.ifBlank { unit },
        confidence = 100,
        isMatched = true
    )
}
