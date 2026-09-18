package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Official TRUEKAPP Logo Composable.
 * Faithfully reproduces the official logo provided by the user:
 * - Two circular cyclic arrows:
 *   - Green top arrow (curving clockwise right)
 *   - Orange bottom arrow (curving clockwise left)
 * - Center illustration:
 *   - Blue house on the left with window and roof
 *   - Two people in the middle greeting/high-fiving:
 *     - Purple figure on left
 *     - Turquoise/Cyan figure on right
 *     - Hands meeting in a high-five clasp
 *   - Orange shopping/tote bag on the right with dual handles
 */
@Composable
fun TruekappOfficialLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    Box(modifier = modifier.size(size)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = this.size.width
            val h = this.size.height

            // Brand palette derived from official logo in soft, pleasing tones
            val greenColor = Color(0xFF388E3C)     // Fresh Leaf Green
            val orangeColor = Color(0xFFF57C00)    // Warm Coral Orange
            val blueHouseRoof = Color(0xFF1976D2)  // Classic Cobalt/House Blue
            val blueHouseWall = Color(0xFFECEFF1)  // Off-white/Soft grey wall
            val blueHouseWindow = Color(0xFF0D47A1)// Deep Blue window
            val purplePerson = Color(0xFF7E57C2)   // Soft Violet Purple
            val turquoisePerson = Color(0xFF00ACC1)// Soft Cyan/Teal Turquoise
            val orangeBag = Color(0xFFFB8C00)      // Soft Orange Shopping Bag

            val strokeWidth = w * 0.085f

            // --- 1. TOP GREEN ARROW (Arcs from left to right at top) ---
            val greenPath = Path().apply {
                // Arc along the top half
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = w * 0.12f,
                        top = h * 0.08f,
                        right = w * 0.88f,
                        bottom = h * 0.84f
                    ),
                    startAngleDegrees = 205f,
                    sweepAngleDegrees = 145f,
                    forceMoveTo = true
                )
            }
            drawPath(
                path = greenPath,
                color = greenColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Green Arrowhead (pointing right/down at ~75%, 26%)
            val greenHead = Path().apply {
                moveTo(w * 0.65f, h * 0.13f)
                lineTo(w * 0.82f, h * 0.26f)
                lineTo(w * 0.72f, h * 0.36f)
                close()
            }
            drawPath(path = greenHead, color = greenColor, style = Fill)

            // --- 2. BOTTOM ORANGE ARROW (Arcs from right to left at bottom) ---
            val orangePath = Path().apply {
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        left = w * 0.12f,
                        top = h * 0.16f,
                        right = w * 0.88f,
                        bottom = h * 0.92f
                    ),
                    startAngleDegrees = 25f,
                    sweepAngleDegrees = 145f,
                    forceMoveTo = true
                )
            }
            drawPath(
                path = orangePath,
                color = orangeColor,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Orange Arrowhead (pointing left/up at ~25%, 74%)
            val orangeHead = Path().apply {
                moveTo(w * 0.35f, h * 0.87f)
                lineTo(w * 0.18f, h * 0.74f)
                lineTo(w * 0.28f, h * 0.64f)
                close()
            }
            drawPath(path = orangeHead, color = orangeColor, style = Fill)

            // --- 3. BLUE HOUSE (Left side inside circle) ---
            // House wall
            drawRoundRect(
                color = blueHouseWall,
                topLeft = Offset(w * 0.22f, h * 0.50f),
                size = Size(w * 0.18f, h * 0.22f),
                cornerRadius = CornerRadius(w * 0.02f, w * 0.02f)
            )

            // House roof (Triangle)
            val roofPath = Path().apply {
                moveTo(w * 0.18f, h * 0.52f)
                lineTo(w * 0.31f, h * 0.38f)
                lineTo(w * 0.43f, h * 0.52f)
                close()
            }
            drawPath(path = roofPath, color = blueHouseRoof, style = Fill)

            // Chimney / Roof accent
            drawRect(
                color = blueHouseRoof,
                topLeft = Offset(w * 0.36f, h * 0.40f),
                size = Size(w * 0.04f, h * 0.08f)
            )

            // 4-Pane Window
            val winW = w * 0.035f
            val winGap = w * 0.015f
            val winX = w * 0.27f
            val winY = h * 0.56f
            drawRect(color = blueHouseWindow, topLeft = Offset(winX, winY), size = Size(winW, winW))
            drawRect(color = blueHouseWindow, topLeft = Offset(winX + winW + winGap, winY), size = Size(winW, winW))
            drawRect(color = blueHouseWindow, topLeft = Offset(winX, winY + winW + winGap), size = Size(winW, winW))
            drawRect(color = blueHouseWindow, topLeft = Offset(winX + winW + winGap, winY + winW + winGap), size = Size(winW, winW))

            // --- 4. CENTER PEOPLE HIGH-FIVING ---
            // Left Person: PURPLE
            val p1HeadRadius = w * 0.055f
            val p1HeadCenter = Offset(w * 0.45f, h * 0.43f)
            drawCircle(color = purplePerson, radius = p1HeadRadius, center = p1HeadCenter)

            // Purple Body & Arm reaching right
            val purpleBody = Path().apply {
                moveTo(w * 0.41f, h * 0.72f)
                cubicTo(
                    w * 0.41f, h * 0.52f,
                    w * 0.49f, h * 0.52f,
                    w * 0.49f, h * 0.72f
                )
                close()
            }
            drawPath(path = purpleBody, color = purplePerson, style = Fill)

            // Purple Arm extended to high-five
            val purpleArm = Path().apply {
                moveTo(w * 0.46f, h * 0.56f)
                quadraticBezierTo(w * 0.50f, h * 0.52f, w * 0.52f, h * 0.47f)
            }
            drawPath(
                path = purpleArm,
                color = purplePerson,
                style = Stroke(width = w * 0.038f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Right Person: TURQUOISE
            val p2HeadRadius = w * 0.055f
            val p2HeadCenter = Offset(w * 0.59f, h * 0.43f)
            drawCircle(color = turquoisePerson, radius = p2HeadRadius, center = p2HeadCenter)

            // Turquoise Body & Arm reaching left
            val turqBody = Path().apply {
                moveTo(w * 0.55f, h * 0.72f)
                cubicTo(
                    w * 0.55f, h * 0.52f,
                    w * 0.63f, h * 0.52f,
                    w * 0.63f, h * 0.72f
                )
                close()
            }
            drawPath(path = turqBody, color = turquoisePerson, style = Fill)

            // Turquoise Arm extended to high-five
            val turqArm = Path().apply {
                moveTo(w * 0.58f, h * 0.56f)
                quadraticBezierTo(w * 0.54f, h * 0.52f, w * 0.52f, h * 0.47f)
            }
            drawPath(
                path = turqArm,
                color = turquoisePerson,
                style = Stroke(width = w * 0.038f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // High-five contact point sparkle/accent
            drawCircle(color = Color.White, radius = w * 0.018f, center = Offset(w * 0.52f, h * 0.47f))

            // --- 5. ORANGE SHOPPING BAG (Right side inside circle) ---
            val bagPath = Path().apply {
                moveTo(w * 0.67f, h * 0.46f)
                cubicTo(w * 0.69f, h * 0.45f, w * 0.77f, h * 0.45f, w * 0.81f, h * 0.48f)
                lineTo(w * 0.84f, h * 0.70f)
                cubicTo(w * 0.84f, h * 0.73f, w * 0.80f, h * 0.74f, w * 0.76f, h * 0.74f)
                lineTo(w * 0.66f, h * 0.74f)
                cubicTo(w * 0.63f, h * 0.74f, w * 0.63f, h * 0.71f, w * 0.65f, h * 0.68f)
                close()
            }
            drawPath(path = bagPath, color = orangeBag, style = Fill)

            // Bag Handles (two white circles or loops)
            drawCircle(
                color = Color.White,
                radius = w * 0.022f,
                center = Offset(w * 0.71f, h * 0.54f)
            )
            drawCircle(
                color = Color.White,
                radius = w * 0.022f,
                center = Offset(w * 0.77f, h * 0.55f)
            )
        }
    }
}
