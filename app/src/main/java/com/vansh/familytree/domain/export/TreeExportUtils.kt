package com.vansh.familytree.domain.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.ui.tree.TreeNode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object TreeExportUtils {
    
    suspend fun exportTreeToBitmap(
        context: Context,
        nodes: List<TreeNode>,
        edges: List<Relationship>,
        nodePositions: Map<String, androidx.compose.ui.geometry.Offset>,
        uri: Uri
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            if (nodes.isEmpty() || nodePositions.isEmpty()) return@withContext false
            
            // Calculate bounding box
            var minX = Float.MAX_VALUE
            var minY = Float.MAX_VALUE
            var maxX = Float.MIN_VALUE
            var maxY = Float.MIN_VALUE
            
            val cardWidth = 300f
            val cardHeight = 150f
            
            nodePositions.values.forEach { pos ->
                if (pos.x < minX) minX = pos.x
                if (pos.y < minY) minY = pos.y
                if (pos.x > maxX) maxX = pos.x
                if (pos.y > maxY) maxY = pos.y
            }
            
            // Add padding
            minX -= 200f
            minY -= 200f
            maxX += cardWidth + 200f
            maxY += cardHeight + 200f
            
            val width = (maxX - minX).toInt()
            val height = (maxY - minY).toInt()
            
            if (width <= 0 || height <= 0) return@withContext false
            
            // Limit max size to prevent OOM
            val scale = if (width > 8000 || height > 8000) {
                8000f / maxOf(width, height)
            } else 1f
            
            val finalWidth = (width * scale).toInt()
            val finalHeight = (height * scale).toInt()
            
            val bitmap = Bitmap.createBitmap(finalWidth, finalHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE) // Background
            
            canvas.scale(scale, scale)
            canvas.translate(-minX, -minY) // Shift to visible area
            
            val edgePaint = Paint().apply {
                color = Color.DKGRAY
                strokeWidth = 4f
                style = Paint.Style.STROKE
            }
            
            val cardPaint = Paint().apply {
                color = Color.parseColor("#EADDFF") // Primary container fallback
                style = Paint.Style.FILL
            }
            
            val textPaint = Paint().apply {
                color = Color.BLACK
                textSize = 28f
                isAntiAlias = true
            }
            
            val subTextPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 22f
                isAntiAlias = true
            }
            
            // Draw edges
            edges.forEach { edge ->
                val startPos = nodePositions[edge.subjectId]
                val endPos = nodePositions[edge.targetId]
                if (startPos != null && endPos != null) {
                    val startX = startPos.x + cardWidth / 2
                    val startY = startPos.y + cardHeight
                    val endX = endPos.x + cardWidth / 2
                    val endY = endPos.y
                    canvas.drawLine(startX, startY, endX, endY, edgePaint)
                }
            }
            
            // Draw nodes
            nodes.forEach { node ->
                val pos = nodePositions[node.member.id] ?: return@forEach
                
                // Parse custom color
                val nodeColor = node.member.cardColor?.let { hex ->
                    try {
                        Color.parseColor(hex)
                    } catch (e: Exception) {
                        null
                    }
                } ?: Color.parseColor("#EADDFF")
                
                cardPaint.color = nodeColor
                
                // Draw rect
                val rect = android.graphics.RectF(pos.x, pos.y, pos.x + cardWidth, pos.y + cardHeight)
                canvas.drawRoundRect(rect, 16f, 16f, cardPaint)
                
                // Draw border
                val borderPaint = Paint().apply {
                    color = Color.DKGRAY
                    strokeWidth = 2f
                    style = Paint.Style.STROKE
                }
                canvas.drawRoundRect(rect, 16f, 16f, borderPaint)
                
                // Draw text
                val textX = pos.x + 20f
                val textY = pos.y + 50f
                val name = "${node.member.firstName} ${node.member.lastName}"
                canvas.drawText(name, textX, textY, textPaint)
                
                val status = if (node.member.isLiving) "Living" else "Deceased"
                canvas.drawText(status, textX, textY + 40f, subTextPaint)
            }
            
            // Save to URI
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            bitmap.recycle()
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
