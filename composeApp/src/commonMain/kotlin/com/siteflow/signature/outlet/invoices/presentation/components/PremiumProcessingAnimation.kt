package com.siteflow.signature.outlet.invoices.presentation.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.siteflow.signature.core.presentation.design.AppColors
import com.siteflow.signature.core.presentation.design.AppTypography
import com.siteflow.signature.outlet.invoices.data.ExtractionPhase
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Premium "Neural Extraction" animation for the AI invoice processing screen.
 *
 * Three canvas layers:
 *  1. Document ghost — border draws itself, skeleton text lines shimmer left-to-right
 *  2. Scanning beam — sweeps top-to-bottom; particles arc from doc edge to AI node
 *  3. AI brain node (hexagon) — concentric radial pulses emanate outward
 *
 * The [phase] drives the rotating text label below the animation via AnimatedContent.
 */
@Composable
fun PremiumProcessingAnimation(
    phase: ExtractionPhase,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "neural_extract")

    // Beam sweeps document top→bottom, repeating every 2.5s
    val beamProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2500, easing = LinearEasing), RepeatMode.Restart),
        label = "beam"
    )

    // Soft glow pulse on doc border (0.3↔0.9)
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.88f,
        animationSpec = infiniteRepeatable(tween(1400, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "glow"
    )

    // Radial pulse from AI node (0→1, restart every 2s)
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )

    // Skeleton shimmer sweep (0→1 continuously)
    val shimmer by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1800, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmer"
    )

    // Particle arcs — 5 particles with staggered timing
    val particleProgress by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2800, easing = LinearEasing), RepeatMode.Restart),
        label = "particles"
    )

    // Second pulse ring: half phase behind pulse1 (creates staggered concentric rings)
    val pulse2 = (pulse + 0.5f) % 1f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.88f)
        ) {
            val w = size.width
            val h = size.height

            // ── Layout constants ──
            val docW = w * 0.44f
            val docH = h * 0.50f
            val docL = (w - docW) / 2f
            val docT = h * 0.32f
            val docR = docL + docW
            val docB = docT + docH
            val corner = 14f

            val nodeX = w / 2f
            val nodeY = h * 0.12f
            val nodeR = w * 0.062f

            // ── Radial pulse rings from AI node ──
            val maxPulseR = nodeR * 4.2f
            drawCircle(
                color = Color(0xFF0F766E).copy(alpha = (1f - pulse) * 0.42f),
                radius = nodeR + maxPulseR * pulse,
                center = Offset(nodeX, nodeY),
                style = Stroke(width = 2.dp.toPx())
            )
            drawCircle(
                color = Color(0xFF14B8A6).copy(alpha = (1f - pulse2) * 0.28f),
                radius = nodeR + maxPulseR * pulse2,
                center = Offset(nodeX, nodeY),
                style = Stroke(width = 1.5f)
            )

            // ── AI node glow halo ──
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF0F766E).copy(alpha = glowAlpha * 0.42f),
                        Color.Transparent
                    ),
                    center = Offset(nodeX, nodeY),
                    radius = nodeR * 2.8f
                ),
                radius = nodeR * 2.8f,
                center = Offset(nodeX, nodeY)
            )

            // ── AI node core (radial gradient circle) ──
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF93C5FD), Color(0xFF0F766E), Color(0xFF0F766E)),
                    center = Offset(nodeX - nodeR * 0.25f, nodeY - nodeR * 0.25f),
                    radius = nodeR * 1.3f
                ),
                radius = nodeR,
                center = Offset(nodeX, nodeY)
            )

            // ── Hexagon outline etched into node ──
            val hexPath = Path()
            val hexR = nodeR * 0.64f
            for (i in 0..5) {
                val angle = (PI / 3.0 * i - PI / 6.0)
                val px = nodeX + hexR * cos(angle).toFloat()
                val py = nodeY + hexR * sin(angle).toFloat()
                if (i == 0) hexPath.moveTo(px, py) else hexPath.lineTo(px, py)
            }
            hexPath.close()
            drawPath(hexPath, color = Color.White.copy(alpha = 0.32f), style = Stroke(width = 1.5f))

            // ── Dashed connector (node → document top) ──
            drawLine(
                color = Color(0xFF0F766E).copy(alpha = 0.14f),
                start = Offset(nodeX, nodeY + nodeR),
                end = Offset(nodeX, docT),
                strokeWidth = 1.5f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 7f), 0f)
            )

            // ── Document outer glow ──
            drawRoundRect(
                color = Color(0xFF0F766E).copy(alpha = glowAlpha * 0.16f),
                topLeft = Offset(docL - 8f, docT - 8f),
                size = Size(docW + 16f, docH + 16f),
                cornerRadius = CornerRadius(corner + 8f)
            )

            // ── Document background ──
            drawRoundRect(
                color = Color(0xFFF8FAFF),
                topLeft = Offset(docL, docT),
                size = Size(docW, docH),
                cornerRadius = CornerRadius(corner)
            )

            // ── Document border (gradient) ──
            drawRoundRect(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F766E).copy(alpha = 0.72f),
                        Color(0xFF60A5FA).copy(alpha = 0.48f)
                    ),
                    start = Offset(docL, docT),
                    end = Offset(docR, docB)
                ),
                topLeft = Offset(docL, docT),
                size = Size(docW, docH),
                cornerRadius = CornerRadius(corner),
                style = Stroke(width = 2f)
            )

            // ── Skeleton text lines (5 rows) ──
            val linePadH = docW * 0.1f
            val lineH = 7f
            val lineRound = 3.5f
            val lineWidthFracs = listOf(0.85f, 0.65f, 0.78f, 0.55f, 0.70f)
            val lineSpacing = docH / 6.5f

            for (i in 0 until 5) {
                val lineY = docT + lineSpacing * (i + 1) - lineH / 2f
                val lineW = (docW - linePadH * 2) * lineWidthFracs[i]
                val lineX = docL + linePadH

                // Base fill
                drawRoundRect(
                    color = Color(0xFFE2E8F0),
                    topLeft = Offset(lineX, lineY),
                    size = Size(lineW, lineH),
                    cornerRadius = CornerRadius(lineRound)
                )
                // Shimmer highlight (each line staggered by 0.18 of the shimmer cycle)
                val lineShimmer = (shimmer + i * 0.18f) % 1f
                val shimmerCenter = lineX + lineW * lineShimmer
                drawRoundRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF60A5FA).copy(alpha = 0.52f),
                            Color.Transparent
                        ),
                        startX = shimmerCenter - 38f,
                        endX = shimmerCenter + 38f
                    ),
                    topLeft = Offset(lineX, lineY),
                    size = Size(lineW, lineH),
                    cornerRadius = CornerRadius(lineRound)
                )
            }

            // ── Scanning beam ──
            val beamY = docT + docH * beamProgress
            if (beamY > docT && beamY < docB) {
                val halfBeam = 15f
                val beamTop = (beamY - halfBeam).coerceAtLeast(docT)
                val beamBot = (beamY + halfBeam).coerceAtMost(docB)
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color(0xFF0F766E).copy(alpha = 0.40f),
                            Color(0xFF60A5FA).copy(alpha = 0.72f),
                            Color(0xFF0F766E).copy(alpha = 0.40f),
                            Color.Transparent
                        ),
                        startY = beamTop,
                        endY = beamBot
                    ),
                    topLeft = Offset(docL + 2f, beamTop),
                    size = Size(docW - 4f, beamBot - beamTop)
                )
            }

            // ── Data particles: 5 arcs from doc right edge → AI node ──
            for (p in 0 until 5) {
                val t = (particleProgress + p / 5f) % 1f
                if (t < 0.25f) continue // gathering phase, not visible yet

                val arcT = ((t - 0.25f) / 0.75f).coerceIn(0f, 1f)

                // Start: right edge of document, evenly spaced vertically
                val startX = docR
                val startY = docT + docH * (p / 4f)

                // Control: arc outward to the right, then curve up toward the node
                val ctrlX = docR + docW * 0.48f
                val ctrlY = (nodeY + startY) / 2f - 18f

                // End: AI node center
                val omt = 1f - arcT
                val px = omt * omt * startX + 2f * omt * arcT * ctrlX + arcT * arcT * nodeX
                val py = omt * omt * startY + 2f * omt * arcT * ctrlY + arcT * arcT * nodeY

                // Fade out as particle reaches the node
                val alpha = if (arcT > 0.82f) (1f - arcT) / 0.18f else 1f
                val radius = 4.5f * (1f - arcT * 0.38f)

                // Glow halo
                drawCircle(
                    color = Color(0xFF14B8A6).copy(alpha = alpha * 0.32f),
                    radius = radius * 2.4f,
                    center = Offset(px, py)
                )
                // Core dot
                drawCircle(
                    color = Color(0xFF93C5FD).copy(alpha = alpha),
                    radius = radius,
                    center = Offset(px, py)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── Phase text ──
        val phaseText = when (phase) {
            ExtractionPhase.UPLOADING -> "Extracting Invoice Details..."
            ExtractionPhase.ERROR     -> "Processing encountered an issue"
        }

        AnimatedContent(
            targetState = phaseText,
            transitionSpec = {
                (fadeIn(tween(400)) + slideInVertically { it / 2 })
                    .togetherWith(fadeOut(tween(300)) + slideOutVertically { -it / 2 })
            },
            label = "phase_text"
        ) { text ->
            Text(
                text = text,
                style = AppTypography.BodyPrimary.copy(
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = AppColors.BlueGradientStart,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
