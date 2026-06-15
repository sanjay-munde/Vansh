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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.RelationshipType
import com.vansh.familytree.data.entity.Gender

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import com.vansh.familytree.ui.theme.MaleAccent
import com.vansh.familytree.ui.theme.FemaleAccent
import com.vansh.familytree.ui.theme.OtherAccent

@OptIn(ExperimentalTextApi::class)
@Composable
fun TreeCanvas(
    nodes: List<Member>,
    edges: List<Relationship>,
    nodePositions: Map<String, Offset>,
    collapsedNodeIds: Set<String>,
    highlightedNodeId: String?,
    selectedNodeIds: Set<String> = emptySet(),
    scale: Float,
    offset: Offset,
    onScaleChange: (Float) -> Unit,
    onOffsetChange: (Offset) -> Unit,
    onNodeDrag: (String, Offset) -> Unit,
    onNodeToggleCollapse: (String) -> Unit,
    onNodeClick: (String) -> Unit,
    onNodeLongClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {

    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        onScaleChange((scale * zoomChange).coerceIn(0.1f, 5f))
        onOffsetChange(offset + offsetChange)
    }

    val textMeasurer = rememberTextMeasurer()
    val textStyle = TextStyle(
        fontFamily = androidx.compose.ui.text.font.FontFamily.Default,
        color = Color.Black,
        fontSize = 12.sp
    )
    val defaultCardColor = MaterialTheme.colorScheme.surface
    val cardBorderColor = MaterialTheme.colorScheme.outline
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)

    val cardWidth = 200f
    val cardHeight = 80f

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
                            val scaledDragAmount = dragAmount / scale
                            onNodeDrag(id, scaledDragAmount)
                        } ?: run {
                            onOffsetChange(offset + dragAmount)
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
            .transformable(state = state)
    ) {

        // Draw Edges (Connector Lines)
        edges.forEach { edge ->
            val startPos = nodePositions[edge.subjectId]
            val endPos = nodePositions[edge.targetId]

            if (startPos != null && endPos != null) {
                if (edge.type == RelationshipType.SPOUSE) {
                    // Draw Side-to-Side Spouse Connectors
                    val startCenterY = startPos.y + cardHeight / 2f
                    val endCenterY = endPos.y + cardHeight / 2f
                    
                    if (Math.abs(startPos.y - endPos.y) < cardHeight) {
                        // Spouses placed horizontally
                        val lineStart = if (startPos.x < endPos.x) {
                            Offset(startPos.x + cardWidth, startCenterY)
                        } else {
                            Offset(startPos.x, startCenterY)
                        }
                        val lineEnd = if (startPos.x < endPos.x) {
                            Offset(endPos.x, endCenterY)
                        } else {
                            Offset(endPos.x + cardWidth, endCenterY)
                        }
                        
                        drawLine(
                            color = Color(0xFFD32F2F), // Muted Crimson Spouse Line
                            start = lineStart,
                            end = lineEnd,
                            strokeWidth = 5f
                        )
                    } else {
                        // Fallback straight line
                        drawLine(
                            color = Color(0xFFD32F2F),
                            start = Offset(startPos.x + cardWidth / 2f, startCenterY),
                            end = Offset(endPos.x + cardWidth / 2f, endCenterY),
                            strokeWidth = 5f
                        )
                    }
                } else {
                    // Draw Curved Parent-Child Connectors (Bezier Curves)
                    val parentNode = if (startPos.y < endPos.y) startPos else endPos
                    val childNode = if (startPos.y < endPos.y) endPos else startPos
                    
                    val pX = parentNode.x + cardWidth / 2f
                    val pY = parentNode.y + cardHeight
                    val cX = childNode.x + cardWidth / 2f
                    val cY = childNode.y
                    
                    val path = Path().apply {
                        moveTo(pX, pY)
                        val midY = (pY + cY) / 2f
                        cubicTo(pX, midY, cX, midY, cX, cY)
                    }
                    
                    drawPath(
                        path = path,
                        color = lineColor,
                        style = Stroke(width = 4f)
                    )
                }
            }
        }

        // Draw Nodes (Family Cards)
        nodes.forEach { member ->
            val pos = nodePositions[member.id] ?: Offset.Zero

            val isDimmed = highlightedNodeId != null && highlightedNodeId != member.id
            val alphaValue = if (isDimmed) 0.25f else 1.0f

            val parsedColor = member.cardColor?.let { hex ->
                try {
                    Color(android.graphics.Color.parseColor(hex))
                } catch (e: Exception) {
                    null
                }
            } ?: defaultCardColor

            // 1. Draw Card Drop Shadow
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.08f),
                topLeft = pos + Offset(4f, 4f),
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(12f, 12f),
                alpha = alphaValue
            )

            // 2. Draw Card Background
            drawRoundRect(
                color = parsedColor,
                topLeft = pos,
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(12f, 12f),
                alpha = alphaValue
            )

            // 3. Draw Gender Accent Strip on the left edge
            val accentColor = when (member.gender) {
                Gender.MALE -> MaleAccent
                Gender.FEMALE -> FemaleAccent
                Gender.OTHER -> OtherAccent
            }
            drawRect(
                color = accentColor,
                topLeft = Offset(pos.x + 2f, pos.y + 2f),
                size = Size(8f, cardHeight - 4f),
                alpha = alphaValue
            )

            // 4. Draw Card Border
            val isSelected = selectedNodeIds.contains(member.id)
            val borderStrokeColor = if (isSelected) Color(0xFF4C8C67) else cardBorderColor
            val borderStrokeWidth = if (isSelected) 5f else 2f
            
            drawRoundRect(
                color = borderStrokeColor,
                topLeft = pos,
                size = Size(cardWidth, cardHeight),
                cornerRadius = CornerRadius(12f, 12f),
                style = Stroke(width = borderStrokeWidth),
                alpha = alphaValue
            )

            // 5. Draw Member Name
            val fullName = "${member.firstName} ${member.lastName}"
            drawText(
                textMeasurer = textMeasurer,
                text = fullName,
                style = textStyle.copy(
                    fontWeight = FontWeight.Bold, 
                    fontSize = 13.sp,
                    color = Color.Black.copy(alpha = alphaValue)
                ),
                topLeft = Offset(pos.x + 20f, pos.y + 12f)
            )
            
            // 6. Draw Subtext (Living status and Age)
            val subText = buildString {
                append(if (member.isLiving) "Living" else "Deceased")
                if (member.dateOfBirth != null) {
                    val endMillis = if (!member.isLiving && member.dateOfDeath != null) member.dateOfDeath else System.currentTimeMillis()
                    val age = ((endMillis - member.dateOfBirth) / (1000L * 60 * 60 * 24 * 365.25)).toInt()
                    append(" • Age $age")
                }
            }
            
            drawText(
                textMeasurer = textMeasurer,
                text = subText,
                style = textStyle.copy(
                    fontSize = 10.sp, 
                    color = Color.Gray.copy(alpha = alphaValue)
                ),
                topLeft = Offset(pos.x + 20f, pos.y + 36f)
            )

            // 7. Draw Birthplace Icon/Label
            if (!member.placeOfBirth.isNullOrBlank()) {
                drawText(
                    textMeasurer = textMeasurer,
                    text = "📍 ${member.placeOfBirth}",
                    style = textStyle.copy(
                        fontSize = 9.sp, 
                        color = Color.Gray.copy(alpha = alphaValue * 0.8f)
                    ),
                    topLeft = Offset(pos.x + 20f, pos.y + 54f)
                )
            }

            // 8. Draw Expand/Collapse Indicator
            if (collapsedNodeIds.contains(member.id)) {
                val circleCenter = Offset(pos.x + cardWidth - 14f, pos.y + cardHeight - 14f)
                drawCircle(
                    color = Color(0xFFD32F2F),
                    radius = 9f,
                    center = circleCenter,
                    alpha = alphaValue
                )
                drawText(
                    textMeasurer = textMeasurer,
                    text = "+",
                    style = textStyle.copy(
                        color = Color.White, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 9.sp
                    ),
                    topLeft = Offset(circleCenter.x - 3f, circleCenter.y - 7f)
                )
            }
        }
    }
}
