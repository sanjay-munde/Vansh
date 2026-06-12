package com.vansh.familytree.ui.tree

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.RelationshipType

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.foundation.gestures.detectTapGestures

@OptIn(ExperimentalTextApi::class)
@Composable
fun TreeCanvas(
    nodes: List<Member>,
    edges: List<Relationship>,
    nodePositions: Map<String, Offset>,
    collapsedNodeIds: Set<String>,
    highlightedNodeId: String?,
    onNodeDrag: (String, Offset) -> Unit,
    onNodeToggleCollapse: (String) -> Unit,
    onNodeClick: (String) -> Unit,
    onNodeLongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Zoom & Pan state
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale = (scale * zoomChange).coerceIn(0.1f, 5f)
        offset += offsetChange
    }

    // Text Measurer for drawing names on canvas
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.labelSmall.copy(color = Color.Black)
    val cardColor = MaterialTheme.colorScheme.primaryContainer
    val cardBorderColor = MaterialTheme.colorScheme.primary
    val lineColor = MaterialTheme.colorScheme.onSurfaceVariant

    val cardWidth = 200f
    val cardHeight = 80f

    // Store the node being dragged
    var draggedNodeId by remember { mutableStateOf<String?>(null) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { touchOffset ->
                        val actualTouchX = (touchOffset.x - offset.x) / scale
                        val actualTouchY = (touchOffset.y - offset.y) / scale
                        val tappedNode = nodes.find { member ->
                            val pos = nodePositions[member.id] ?: Offset.Zero
                            actualTouchX >= pos.x && actualTouchX <= pos.x + cardWidth &&
                            actualTouchY >= pos.y && actualTouchY <= pos.y + cardHeight
                        }
                        tappedNode?.let { onNodeToggleCollapse(it.id) }
                    },
                    onTap = { touchOffset ->
                        val actualTouchX = (touchOffset.x - offset.x) / scale
                        val actualTouchY = (touchOffset.y - offset.y) / scale
                        val tappedNode = nodes.find { member ->
                            val pos = nodePositions[member.id] ?: Offset.Zero
                            actualTouchX >= pos.x && actualTouchX <= pos.x + cardWidth &&
                            actualTouchY >= pos.y && actualTouchY <= pos.y + cardHeight
                        }
                        tappedNode?.let { onNodeClick(it.id) }
                    },
                    onLongPress = { touchOffset ->
                        val actualTouchX = (touchOffset.x - offset.x) / scale
                        val actualTouchY = (touchOffset.y - offset.y) / scale
                        val tappedNode = nodes.find { member ->
                            val pos = nodePositions[member.id] ?: Offset.Zero
                            actualTouchX >= pos.x && actualTouchX <= pos.x + cardWidth &&
                            actualTouchY >= pos.y && actualTouchY <= pos.y + cardHeight
                        }
                        tappedNode?.let { onNodeLongClick(it.id) }
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { touchOffset ->
                        // Reverse the zoom/pan transform to find the actual canvas coordinate touched
                        val actualTouchX = (touchOffset.x - offset.x) / scale
                        val actualTouchY = (touchOffset.y - offset.y) / scale
                        
                        draggedNodeId = nodes.find { member ->
                            val pos = nodePositions[member.id] ?: Offset.Zero
                            actualTouchX >= pos.x && actualTouchX <= pos.x + cardWidth &&
                            actualTouchY >= pos.y && actualTouchY <= pos.y + cardHeight
                        }?.id
                    },
                    onDragEnd = { draggedNodeId = null },
                    onDragCancel = { draggedNodeId = null },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        draggedNodeId?.let { id ->
                            // Adjust drag amount by scale
                            val scaledDragAmount = dragAmount / scale
                            onNodeDrag(id, scaledDragAmount)
                        } ?: run {
                            // If no node dragged, pan the canvas
                            offset += dragAmount
                        }
                    }
                )
            }
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = state) // Kept for pinch-to-zoom
    ) {

        // Draw Edges
        edges.forEach { edge ->
            val startPos = nodePositions[edge.subjectId]
            val endPos = nodePositions[edge.targetId]

            if (startPos != null && endPos != null) {
                val startCenter = Offset(startPos.x + cardWidth / 2, startPos.y + cardHeight / 2)
                val endCenter = Offset(endPos.x + cardWidth / 2, endPos.y + cardHeight / 2)

                // Different colors/strokes based on relationship type
                val strokeColor = if (edge.type == RelationshipType.SPOUSE) Color.Red else lineColor
                drawLine(
                    color = strokeColor,
                    start = startCenter,
                    end = endCenter,
                    strokeWidth = 4f
                )
            }
        }

        // Draw Nodes
        nodes.forEach { member ->
            val pos = nodePositions[member.id] ?: Offset.Zero

            // If a node is highlighted, we dim others.
            // In a real app we'd compute lineage. For now, we highlight just the selected node and dim the rest.
            val isDimmed = highlightedNodeId != null && highlightedNodeId != member.id
            val alphaValue = if (isDimmed) 0.3f else 1.0f

            // Draw Card Background
            drawRect(
                color = cardColor,
                topLeft = pos,
                size = Size(cardWidth, cardHeight),
                alpha = alphaValue
            )

            // Draw Card Border
            drawRect(
                color = cardBorderColor,
                topLeft = pos,
                size = Size(cardWidth, cardHeight),
                style = Stroke(width = 2f),
                alpha = alphaValue
            )

            // Draw Text
            val fullName = "${member.firstName} ${member.lastName}"
            drawText(
                textMeasurer = textMeasurer,
                text = fullName,
                style = textStyle.copy(color = textStyle.color.copy(alpha = alphaValue)),
                topLeft = Offset(pos.x + 10f, pos.y + 10f)
            )
            
            drawText(
                textMeasurer = textMeasurer,
                text = if (member.isLiving) "Living" else "Deceased",
                style = textStyle.copy(color = (if (member.isLiving) Color.Blue else Color.Gray).copy(alpha = alphaValue)),
                topLeft = Offset(pos.x + 10f, pos.y + 40f)
            )

            if (collapsedNodeIds.contains(member.id)) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "[+]",
                    style = textStyle.copy(color = Color.Red.copy(alpha = alphaValue)),
                    topLeft = Offset(pos.x + cardWidth - 25f, pos.y + cardHeight - 25f)
                )
            }
        }
    }
}
