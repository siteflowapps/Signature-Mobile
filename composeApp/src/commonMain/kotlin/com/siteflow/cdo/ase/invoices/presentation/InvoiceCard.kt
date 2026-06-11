package com.siteflow.cdo.ase.invoices.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siteflow.cdo.ase.invoices.data.InvoiceItem
import com.siteflow.cdo.ase.invoices.data.InvoiceStatus
import com.siteflow.cdo.ase.invoices.data.SlabQualification
import com.siteflow.cdo.core.presentation.design.AppColors
import com.siteflow.cdo.core.presentation.design.AppTypography

/**
 * Invoice card — clean layout consistent with OutletCard design tokens.
 *
 * Zone 1: Outlet name + slab badge + location • period + status badge
 * Zone 2: Amount + slab qualifier chip
 * Zone 3: Submitted time + Review button / View Details link
 */
@Composable
fun InvoiceCard(
    invoice: InvoiceItem,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    actionableStatuses: Set<InvoiceStatus> = setOf(InvoiceStatus.SUBMITTED)
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
            // Left accent bar — status color
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(
                        invoice.status.color,
                        RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // ── Zone 1: Identity ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = invoice.outletName,
                            style = AppTypography.TitleMedium.copy(fontSize = 16.sp),
                            color = Color(0xFF111827),
                            maxLines = 1
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Slab badge — matches OutletCard
                            Box(
                                modifier = Modifier
                                    .background(invoice.slab.bgColor, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${invoice.slab.emoji} ${invoice.slab.label}",
                                    style = AppTypography.Caption.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = invoice.slab.color
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${invoice.location}  •  ${invoice.invoicePeriod}",
                                style = AppTypography.Caption.copy(fontSize = 11.sp),
                                color = AppColors.TextTertiary,
                                maxLines = 1
                            )
                        }
                    }

                    Spacer(Modifier.width(8.dp))

                    // Status badge — matches OutletCard
                    Box(
                        modifier = Modifier
                            .background(invoice.status.bgColor, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = invoice.status.label,
                            style = AppTypography.Caption.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = invoice.status.color
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // ── Zone 2: Cases (hero) + Qualifier + Amount (secondary) ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cases — hero metric
                    Text(
                        text = "${invoice.totalCases}",
                        style = AppTypography.TitleMedium.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF059669)
                    )
                    Text(
                        text = " cases",
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF059669).copy(alpha = 0.7f)
                    )

                    Spacer(Modifier.width(10.dp))

//                    // Slab qualification inline chip
//                    Row(
//                        modifier = Modifier
//                            .background(
//                                if (invoice.slabQualification == SlabQualification.MEETS)
//                                    Color(0xFFF0FDF4) else Color(0xFFFFFBEB),
//                                RoundedCornerShape(6.dp)
//                            )
//                            .padding(horizontal = 6.dp, vertical = 2.dp),
//                        verticalAlignment = Alignment.CenterVertically,
//                        horizontalArrangement = Arrangement.spacedBy(3.dp)
//                    ) {
//                        Icon(
//                            imageVector = if (invoice.slabQualification == SlabQualification.MEETS)
//                                Icons.Default.CheckCircle else Icons.Default.Warning,
//                            contentDescription = null,
//                            tint = invoice.slabQualification.color,
//                            modifier = Modifier.size(12.dp)
//                        )
//                        Text(
//                            text = "${invoice.slabQualification.label} for ${invoice.slab.label}",
//                            style = AppTypography.Caption.copy(
//                                fontSize = 10.sp,
//                                fontWeight = FontWeight.Medium
//                            ),
//                            color = invoice.slabQualification.color
//                        )
//                    }

//                    Spacer(Modifier.weight(1f))

                    // Invoice amount — secondary, right-aligned
                    Text(
                        text = invoice.invoiceAmount,
                        style = AppTypography.Caption.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AppColors.TextTertiary
                    )
                }


                Spacer(Modifier.height(10.dp))

                // ── Zone 3: Time + CTA ──
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Submitted ${invoice.submittedTime}",
                        style = AppTypography.Caption.copy(fontSize = 11.sp),
                        color = AppColors.TextTertiary
                    )

                    if (invoice.status in actionableStatuses) {
                        // Review button — gradient fill (only for actionable status)
                        Button(
                            onClick = onClick,
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                AppColors.BlueGradientStart,
                                                AppColors.BlueGradientEnd
                                            )
                                        ),
                                        RoundedCornerShape(8.dp)
                                    )
                                    .padding(horizontal = 16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Review",
                                    style = AppTypography.Caption.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = Color.White
                                )
                            }
                        }
                    } else {
                        // View Details — for already-processed invoices
                        Text(
                            text = "View Details →",
                            style = AppTypography.BodyPrimary.copy(
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = AppColors.BlueGradientStart
                        )
                    }
                }
            }
        }
    }
}
