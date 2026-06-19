package com.siteflow.signature.outlet.invoices.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.invoices.data.SkuLineItem
import com.siteflow.signature.outlet.invoices.domain.LineItemField

@Composable
fun FormFieldLabel(text: String) {
    Text(
        text = text,
        style = AppTypography.Caption.copy(
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        ),
        color = AppColors.TextTertiary
    )
}

@Composable
fun InvoiceOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    // Select-all on focus: when the field receives focus, select all text so
    // the user can immediately type to replace the existing value.
    var textFieldValue by remember(value) {
        mutableStateOf(TextFieldValue(value, TextRange(0, value.length)))
    }
    Column(modifier = modifier) {
        if (label != null) {
            FormFieldLabel(label)
            Spacer(Modifier.height(6.dp))
        }
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = { newTfv ->
                textFieldValue = newTfv
                onValueChange(newTfv.text)
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    if (focusState.isFocused) {
                        // Select all text on focus so first keystroke replaces it
                        textFieldValue = textFieldValue.copy(
                            selection = TextRange(0, textFieldValue.text.length)
                        )
                    }
                },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppColors.BlueGradientStart,
                unfocusedBorderColor = Color(0xFFE5E7EB),
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                cursorColor = AppColors.BlueGradientStart
            ),
            textStyle = AppTypography.BodyPrimary.copy(
                fontSize = 15.sp,
                color = Color(0xFF111827)
            ),
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            singleLine = if (minLines > 1) false else singleLine,
            minLines = minLines,
            readOnly = readOnly
        )
    }
}

// ── Invoice Items Section ──

/**
 * Small chip shown below a field when its value is missing (qty=0 or price=0).
 * Tapping it triggers the region-scan flow for that field.
 */
@Composable
fun ScanChip(label: String, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
        modifier = Modifier.height(24.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFF14B8A6),
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(3.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color(0xFF14B8A6),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Single editable line item row.
 */
@Composable
fun LineItemRow(
    item: SkuLineItem,
    index: Int,
    onUpdateField: (LineItemField, String) -> Unit,
    onRemove: () -> Unit,
    onScanRegion: ((LineItemField) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with item number and delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Item ${index + 1}",
                    style = AppTypography.Caption.copy(
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFF6B7280)
                )

                // Delete button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Delete item",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Product name
            InvoiceOutlinedTextField(
                value = item.productName,
                onValueChange = { onUpdateField(LineItemField.PRODUCT_NAME, it) },
                label = "SKU Name"
            )

            Spacer(Modifier.height(12.dp))

            // Row 1: Qty, Unit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    InvoiceOutlinedTextField(
                        value = if (item.quantity == 0) "" else item.quantity.toString(),
                        onValueChange = { onUpdateField(LineItemField.QUANTITY, it) },
                        label = "Qty"
                    )
                    if (item.quantity == 0 && onScanRegion != null) {
                        ScanChip(label = "Scan qty") { onScanRegion(LineItemField.QUANTITY) }
                    }
                }
                InvoiceOutlinedTextField(
                    value = item.unit,
                    onValueChange = { onUpdateField(LineItemField.UNIT, it) },
                    label = "Unit",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))

            // Row 2: Unit Price, Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    InvoiceOutlinedTextField(
                        value = if (item.pricePerUnit > 0) item.pricePerUnit.toString() else "",
                        onValueChange = { onUpdateField(LineItemField.PRICE_PER_UNIT, it) },
                        label = "Unit Price (₹)"
                    )
                    if (item.pricePerUnit == 0.0 && onScanRegion != null) {
                        ScanChip(label = "Scan price") { onScanRegion(LineItemField.PRICE_PER_UNIT) }
                    }
                }
                InvoiceOutlinedTextField(
                    value = if (item.totalPrice > 0) {
                        val t = item.totalPrice
                        if (t == kotlin.math.floor(t)) "₹${t.toLong()}" else "₹${ ((t * 100).toLong() / 100.0).toString().let { if (it.substringAfter(".").length == 1) it + "0" else it } }"
                    } else "",
                    onValueChange = {
                        val cleaned = it.replace("₹", "").trim()
                        onUpdateField(LineItemField.TOTAL, cleaned)
                    },
                    label = "Total",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Section containing all editable invoice items.
 */
@Composable
fun InvoiceItemsSection(
    items: List<SkuLineItem>,
    onUpdateLineItem: (Int, LineItemField, String) -> Unit,
    onRemoveLineItem: (Int) -> Unit,
    onAddLineItem: () -> Unit,
    onScanRegion: ((Int, LineItemField) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Items (${items.size} items · ${items.sumOf { it.quantity }} cases)",
                style = AppTypography.TitleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF111827)
            )
            TextButton(onClick = onAddLineItem) {
                Text(
                    text = "+ Add Item",
                    style = AppTypography.BodyPrimary.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AppColors.BlueGradientStart
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        // Item rows
        items.forEachIndexed { index, item ->
            LineItemRow(
                item = item,
                index = index,
                onUpdateField = { field, value -> onUpdateLineItem(index, field, value) },
                onRemove = { onRemoveLineItem(index) },
                onScanRegion = onScanRegion?.let { callback -> { field -> callback(index, field) } }
            )
            if (index < items.size - 1) {
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

