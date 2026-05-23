package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Product
import kotlin.math.min

@Composable
fun Studio3DCanvas(
    product: Product,
    selectedColorHex: Long,
    materialType: String,
    angleX: Float,
    angleY: Float,
    onAnglesChanged: (Float, Float) -> Unit,
    roomLayoutType: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(350.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E293B), // Slate deep base
                        Color(0xFF0F172A)  // Dark carbon midnight
                    )
                )
            )
    ) {
        // Renders visual aids like perspective grids depending on select floor styling
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // X drag represents horizontal movement (yaw angleY)
                        // Y drag represents vertical elevation movement (pitch angleX)
                        val nextAngleY = angleY + dragAmount.x * 0.007f
                        val nextAngleX = (angleX + dragAmount.y * 0.007f).coerceIn(-1.2f, 1.2f)
                        onAnglesChanged(nextAngleX, nextAngleY)
                    }
                }
        ) {
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            
            // Core coordinate calculation grid scale
            val responsiveScale = min(size.width, size.height) / 4.5f

            // --- 1. Draw Simulated Perspective Grid Floor ---
            val gridColor = Color(0xFF334155).copy(alpha = 0.5f)
            val gridZRange = -4..4
            val gridXRange = -4..4

            // Generate floor vertices and project them
            for (gx in gridXRange) {
                val ptStart = Point3D(gx.toFloat() * 0.8f, -0.8f, -4f * 0.8f).rotate(angleX, angleY)
                val ptEnd = Point3D(gx.toFloat() * 0.8f, -0.8f, 4f * 0.8f).rotate(angleX, angleY)
                
                val sxStart = centerX + ptStart.x * responsiveScale
                val syStart = centerY - ptStart.y * responsiveScale
                val sxEnd = centerX + ptEnd.x * responsiveScale
                val syEnd = centerY - ptEnd.y * responsiveScale

                // Simple depth clipping to filter points far behind camera
                if (ptStart.z > -4 && ptEnd.z > -4) {
                    drawLine(
                        color = gridColor,
                        start = Offset(sxStart, syStart),
                        end = Offset(sxEnd, syEnd),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            for (gz in gridZRange) {
                val ptStart = Point3D(-4f * 0.8f, -0.8f, gz.toFloat() * 0.8f).rotate(angleX, angleY)
                val ptEnd = Point3D(4f * 0.8f, -0.8f, gz.toFloat() * 0.8f).rotate(angleX, angleY)

                val sxStart = centerX + ptStart.x * responsiveScale
                val syStart = centerY - ptStart.y * responsiveScale
                val sxEnd = centerX + ptEnd.x * responsiveScale
                val syEnd = centerY - ptEnd.y * responsiveScale

                if (ptStart.z > -4 && ptEnd.z > -4) {
                    drawLine(
                        color = gridColor,
                        start = Offset(sxStart, syStart),
                        end = Offset(sxEnd, syEnd),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }

            // --- 2. Load 3D Mesh ---
            val activeMesh = ModelFactory.generateModel(product.modelType, selectedColorHex)

            // Dynamic rotation about angleX and angleY
            val rotatedVertices = activeMesh.vertices.map { it.rotate(angleX, angleY) }

            // --- 3. Painter's Algorithm (Occlusion Sorting) ---
            // Sort polygons based on average Z-depth (Z-index facing camera). Farthest first = larger Z values.
            val sortedPolygons = activeMesh.polygons.mapIndexed { index, polygon ->
                val avgZ = polygon.vertexIndices.map { rotatedVertices[it].z }.average().toFloat()
                Pair(polygon, avgZ)
            }.sortedByDescending { it.second }.map { it.first }

            // --- 4. Draw Polygons ---
            for (poly in sortedPolygons) {
                val path = Path()
                var isPointBehindCamera = false

                poly.vertexIndices.forEachIndexed { idx, vIdx ->
                    val rPt = rotatedVertices[vIdx]
                    if (rPt.z < -3.5f) { // Simple camera plane clipping
                        isPointBehindCamera = true
                    }
                    val sx = centerX + rPt.x * responsiveScale
                    val sy = centerY - rPt.y * responsiveScale
                    if (idx == 0) {
                        path.moveTo(sx, sy)
                    } else {
                        path.lineTo(sx, sy)
                    }
                }
                path.close()

                if (!isPointBehindCamera) {
                    // Draw filled face matching material sheen adjustments
                    val baseColor = Color(poly.color)
                    drawPath(
                        path = path,
                        color = baseColor
                    )

                    // Draw sharp blueprints-style subtle grid borders
                    val strokeColor = when (poly.faceTag) {
                        "top" -> Color.White.copy(alpha = 0.35f)
                        "front" -> Color.White.copy(alpha = 0.20f)
                        else -> Color.White.copy(alpha = 0.15f)
                    }
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
            }

            // --- 5. Custom Canvas Decor Overlay matching Selected Room Layout ---
            if (roomLayoutType == "Minimalist Boho") {
                // Subtle Boho design layout indicators
                drawCircle(
                    color = Color(0xFFC39B78).copy(alpha = 0.4f),
                    radius = 20f,
                    center = Offset(centerX - 120f, centerY + 100f)
                )
            }
        }

        // Help overlays
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "info",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.height(14.dp)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(2.dp))
                Text(
                    text = "Xoay mô hình 3D bằng cách chạm & vuốt thả",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }

        // Material spec overlay in top-right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .background(Color(0xFF38BDF8).copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Vật liệu: $materialType",
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
        }
    }
}
