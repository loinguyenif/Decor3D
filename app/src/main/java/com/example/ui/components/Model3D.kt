package com.example.ui.components

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

data class Point3D(val x: Float, val y: Float, val z: Float) {
    fun rotate(angleX: Float, angleY: Float): Point3D {
        // Rotate about Y axis (yaw)
        val cosY = cos(angleY)
        val sinY = sin(angleY)
        val x1 = x * cosY - z * sinY
        val z1 = x * sinY + z * cosY

        // Rotate about X axis (pitch)
        val cosX = cos(angleX)
        val sinX = sin(angleX)
        val y2 = y * cosX - z1 * sinX
        val z2 = y * sinX + z1 * cosX

        return Point3D(x1, y2, z2)
    }
}

data class Polygon3D(
    val vertexIndices: List<Int>,
    val color: Long,
    val faceTag: String = "general" // "top", "side", "front" used for lighting
)

class Mesh3D(
    val vertices: List<Point3D>,
    val polygons: List<Polygon3D>
)

object ModelFactory {

    /**
     * Helper to create a multi-faced solid box.
     * Appends vertices to the ongoing list and returns the correct global indices for faces.
     */
    private fun addBoxToMesh(
        vertices: MutableList<Point3D>,
        polygons: MutableList<Polygon3D>,
        w: Float, h: Float, d: Float,
        ox: Float, oy: Float, oz: Float,
        baseColor: Long
    ) {
        val startIdx = vertices.size
        
        // 8 Vertices of the box
        val localVertices = listOf(
            Point3D(ox - w/2, oy - h/2, oz - d/2), // 0: LBB
            Point3D(ox + w/2, oy - h/2, oz - d/2), // 1: RBB
            Point3D(ox + w/2, oy + h/2, oz - d/2), // 2: RTB
            Point3D(ox - w/2, oy + h/2, oz - d/2), // 3: LTB
            Point3D(ox - w/2, oy - h/2, oz + d/2), // 4: LBF
            Point3D(ox + w/2, oy - h/2, oz + d/2), // 5: RBF
            Point3D(ox + w/2, oy + h/2, oz + d/2), // 6: RTF
            Point3D(ox - w/2, oy + h/2, oz + d/2)  // 7: LTF
        )
        vertices.addAll(localVertices)

        // Adjust color shading based on face orientations to simulate directional lighting
        val topColor = adjustBrightness(baseColor, 1.15f)
        val sideColor = adjustBrightness(baseColor, 0.95f)
        val frontColor = baseColor
        val backColor = adjustBrightness(baseColor, 0.80f)
        val bottomColor = adjustBrightness(baseColor, 0.65f)

        // 6 Faces (quadrilaterals)
        val localPolygons = listOf(
            // front (4, 5, 6, 7)
            Polygon3D(listOf(startIdx + 4, startIdx + 5, startIdx + 6, startIdx + 7), frontColor, "front"),
            // back (1, 0, 3, 2)
            Polygon3D(listOf(startIdx + 1, startIdx + 0, startIdx + 3, startIdx + 2), backColor, "back"),
            // left (0, 4, 7, 3)
            Polygon3D(listOf(startIdx + 0, startIdx + 4, startIdx + 7, startIdx + 3), sideColor, "side"),
            // right (5, 1, 2, 6)
            Polygon3D(listOf(startIdx + 5, startIdx + 1, startIdx + 2, startIdx + 6), sideColor, "side"),
            // top (3, 2, 6, 7)
            Polygon3D(listOf(startIdx + 3, startIdx + 2, startIdx + 6, startIdx + 7), topColor, "top"),
            // bottom (4, 5, 1, 0)
            Polygon3D(listOf(startIdx + 4, startIdx + 5, startIdx + 1, startIdx + 0), bottomColor, "bottom")
        )
        polygons.addAll(localPolygons)
    }

    private fun adjustBrightness(colorHex: Long, factor: Float): Long {
        val a = (colorHex shr 24) and 0xFF
        val r = (((colorHex shr 16) and 0xFF) * factor).coerceIn(0f, 255f).toLong()
        val g = (((colorHex shr 8) and 0xFF) * factor).coerceIn(0f, 255f).toLong()
        val b = ((colorHex and 0xFF) * factor).coerceIn(0f, 255f).toLong()
        return (a shl 24) or (r shl 16) or (g shl 8) or b
    }

    fun generateModel(type: String, baseColor: Long): Mesh3D {
        val vertices = mutableListOf<Point3D>()
        val polygons = mutableListOf<Polygon3D>()

        when (type.lowercase()) {
            "sofa" -> {
                // Main seat cushion block
                addBoxToMesh(vertices, polygons, w = 2.4f, h = 0.3f, d = 1.2f, ox = 0f, oy = -0.3f, oz = 0f, baseColor = baseColor)
                // Backrest block
                addBoxToMesh(vertices, polygons, w = 2.4f, h = 0.8f, d = 0.3f, ox = 0f, oy = 0.25f, oz = -0.45f, baseColor = baseColor)
                // Left armrest
                addBoxToMesh(vertices, polygons, w = 0.3f, h = 0.7f, d = 1.2f, ox = -1.35f, oy = -0.1f, oz = 0f, baseColor = adjustBrightness(baseColor, 0.9f))
                // Right armrest
                addBoxToMesh(vertices, polygons, w = 0.3f, h = 0.7f, d = 1.2f, ox = 1.35f, oy = -0.1f, oz = 0f, baseColor = adjustBrightness(baseColor, 0.9f))
                
                // Wooden support legs
                val legColor = 0xFF5C4033 // Wooden leg hex
                addBoxToMesh(vertices, polygons, w = 0.15f, h = 0.4f, d = 0.15f, ox = -1.2f, oy = -0.65f, oz = -0.4f, baseColor = legColor)
                addBoxToMesh(vertices, polygons, w = 0.15f, h = 0.4f, d = 0.15f, ox = 1.2f, oy = -0.65f, oz = -0.4f, baseColor = legColor)
                addBoxToMesh(vertices, polygons, w = 0.15f, h = 0.4f, d = 0.15f, ox = -1.2f, oy = -0.65f, oz = 0.4f, baseColor = legColor)
                addBoxToMesh(vertices, polygons, w = 0.15f, h = 0.4f, d = 0.15f, ox = 1.2f, oy = -0.65f, oz = 0.4f, baseColor = legColor)
            }
            "table" -> {
                // Tabletop plank
                addBoxToMesh(vertices, polygons, w = 2.4f, h = 0.12f, d = 1.3f, ox = 0f, oy = 0.4f, oz = 0f, baseColor = baseColor)
                
                // Four robust wooden columns
                val legColor = adjustBrightness(baseColor, 0.8f)
                addBoxToMesh(vertices, polygons, w = 0.18f, h = 1.1f, d = 0.18f, ox = -1.0f, oy = -0.21f, oz = -0.45f, baseColor = legColor)
                addBoxToMesh(vertices, polygons, w = 0.18f, h = 1.1f, d = 0.18f, ox = 1.0f, oy = -0.21f, oz = -0.45f, baseColor = legColor)
                addBoxToMesh(vertices, polygons, w = 0.18f, h = 1.1f, d = 0.18f, ox = -1.0f, oy = -0.21f, oz = 0.45f, baseColor = legColor)
                addBoxToMesh(vertices, polygons, w = 0.18f, h = 1.1f, d = 0.18f, ox = 1.0f, oy = -0.21f, oz = 0.45f, baseColor = legColor)

                // Bottom shelf bar joining legs
                addBoxToMesh(vertices, polygons, w = 2.0f, h = 0.08f, d = 0.12f, ox = 0f, oy = -0.5f, oz = 0f, baseColor = 0xFF222222)
            }
            "bed" -> {
                // Mattress support frame
                val frameColor = 0xFF4E3629 // Dark wood
                addBoxToMesh(vertices, polygons, w = 1.9f, h = 0.4f, d = 2.1f, ox = 0f, oy = -0.3f, oz = 0.0f, baseColor = frameColor)
                // Headboard
                addBoxToMesh(vertices, polygons, w = 1.9f, h = 1.2f, d = 0.15f, ox = 0f, oy = 0.4f, oz = -1.0f, baseColor = baseColor)
                // Soft foam Mattress
                addBoxToMesh(vertices, polygons, w = 1.8f, h = 0.35f, d = 1.95f, ox = 0f, oy = 0.05f, oz = 0.05f, baseColor = 0xFFF8F8FF) // Ghost-white sheet
                
                // Pillows
                val pillowColor = adjustBrightness(baseColor, 1.2f)
                addBoxToMesh(vertices, polygons, w = 0.65f, h = 0.15f, d = 0.4f, ox = -0.45f, oy = 0.25f, oz = -0.65f, baseColor = pillowColor)
                addBoxToMesh(vertices, polygons, w = 0.65f, h = 0.15f, d = 0.4f, ox = 0.45f, oy = 0.25f, oz = -0.65f, baseColor = pillowColor)

                // Blanket sheet edge accent
                addBoxToMesh(vertices, polygons, w = 1.81f, h = 0.36f, d = 1.00f, ox = 0f, oy = 0.051f, oz = 0.55f, baseColor = baseColor)
            }
            "chair" -> {
                // Cushion seat
                addBoxToMesh(vertices, polygons, w = 1.0f, h = 0.15f, d = 1.0f, ox = 0f, oy = -0.1f, oz = 0f, baseColor = baseColor)
                // Curve mesh Backrest
                addBoxToMesh(vertices, polygons, w = 0.9f, h = 0.8f, d = 0.1f, ox = 0f, oy = 0.35f, oz = -0.4f, baseColor = adjustBrightness(baseColor, 0.9f))
                
                // Central metal pole support
                val metalColor = 0xFFB0C4DE // Slate gray metal
                addBoxToMesh(vertices, polygons, w = 0.12f, h = 0.6f, d = 0.12f, ox = 0f, oy = -0.45f, oz = 0f, baseColor = metalColor)
                
                // Base star feet (4 extensions)
                addBoxToMesh(vertices, polygons, w = 1.0f, h = 0.1f, d = 0.12f, ox = 0f, oy = -0.75f, oz = 0f, baseColor = 0xFF1C1C1C)
                addBoxToMesh(vertices, polygons, w = 0.12f, h = 0.1f, d = 1.0f, ox = 0f, oy = -0.75f, oz = 0f, baseColor = 0xFF1C1C1C)
                
                // Armrests
                addBoxToMesh(vertices, polygons, w = 0.08f, h = 0.4f, d = 0.6f, ox = -0.48f, oy = 0.15f, oz = 0f, baseColor = 0xFF1C1C1C)
                addBoxToMesh(vertices, polygons, w = 0.08f, h = 0.4f, d = 0.6f, ox = 0.48f, oy = 0.15f, oz = 0f, baseColor = 0xFF1C1C1C)
            }
            "cabinet" -> {
                // Robust outer wooden box
                addBoxToMesh(vertices, polygons, w = 1.4f, h = 2.0f, d = 0.7f, ox = 0f, oy = 0f, oz = 0f, baseColor = baseColor)
                
                // Door lines (simulated offsets / glass sheet)
                addBoxToMesh(vertices, polygons, w = 0.6f, h = 1.8f, d = 0.04f, ox = -0.32f, oy = 0f, oz = 0.351f, baseColor = adjustBrightness(baseColor, 0.75f))
                addBoxToMesh(vertices, polygons, w = 0.6f, h = 1.8f, d = 0.04f, ox = 0.32f, oy = 0f, oz = 0.351f, baseColor = adjustBrightness(baseColor, 0.75f))
                
                // Gold colored modern door handles
                val goldColor = 0xFFDAA520
                addBoxToMesh(vertices, polygons, w = 0.05f, h = 0.3f, d = 0.05f, ox = -0.05f, oy = 0f, oz = 0.38f, baseColor = goldColor)
                addBoxToMesh(vertices, polygons, w = 0.05f, h = 0.3f, d = 0.05f, ox = 0.05f, oy = 0f, oz = 0.38f, baseColor = goldColor)
            }
            // Fallback to simple building cube
            else -> {
                addBoxToMesh(vertices, polygons, w = 1.0f, h = 1.0f, d = 1.0f, ox = 0f, oy = 0f, oz = 0f, baseColor = baseColor)
            }
        }

        return Mesh3D(vertices, polygons)
    }
}
