package com.vansh.familytree.ui.tree

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.RelationshipType
import com.vansh.familytree.data.entity.Gender
import com.vansh.familytree.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TreeCanvas(
    nodes: List<Member>,
    edges: List<Relationship>,
    nodePositions: Map<String, Offset>,
    collapsedNodeIds: Set<String>,
    highlightedNodeId: String?,
    selectedNodeIds: Set<String>,
    profilePhotos: Map<String, String>,
    searchQuery: String,
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
    val density = LocalDensity.current.density
    val state = rememberTransformableState { zoomChange, offsetChange, _ ->
        onScaleChange((scale * zoomChange).coerceIn(0.1f, 5f))
        onOffsetChange(offset + offsetChange)
    }

    val cardWidth = 220f
    val cardHeight = 90f

    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)

    var draggedNodeId by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clipToBounds()
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
                            // Convert dragAmount from pixels to DP, taking scale into account
                            val scaledDragAmountX = (dragAmount.x / scale) / density
                            val scaledDragAmountY = (dragAmount.y / scale) / density
                            onNodeDrag(id, Offset(scaledDragAmountX, scaledDragAmountY))
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
        // 1. Draw connection lines (Bezier Curves and Spousal connects)
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            edges.forEach { edge ->
                val startPos = nodePositions[edge.subjectId]
                val endPos = nodePositions[edge.targetId]

                if (startPos != null && endPos != null) {
                    if (edge.type == RelationshipType.SPOUSE) {
                        // Draw horizontal spouse connecting line
                        val startCenterY = (startPos.y + cardHeight / 2f) * density
                        val endCenterY = (endPos.y + cardHeight / 2f) * density
                        
                        if (Math.abs(startPos.y - endPos.y) < cardHeight) {
                            val lineStart = if (startPos.x < endPos.x) {
                                Offset((startPos.x + cardWidth) * density, startCenterY)
                            } else {
                                Offset(startPos.x * density, startCenterY)
                            }
                            val lineEnd = if (startPos.x < endPos.x) {
                                Offset(endPos.x * density, endCenterY)
                            } else {
                                Offset((endPos.x + cardWidth) * density, endCenterY)
                            }
                            
                            drawLine(
                                color = Color(0xFFA94A2D).copy(alpha = 0.8f), // Secondary theme terracotta color
                                start = lineStart,
                                end = lineEnd,
                                strokeWidth = 5f
                            )
                        } else {
                            // Fallback straight line
                            drawLine(
                                color = Color(0xFFA94A2D).copy(alpha = 0.8f),
                                start = Offset((startPos.x + cardWidth / 2f) * density, startCenterY),
                                end = Offset((endPos.x + cardWidth / 2f) * density, endCenterY),
                                strokeWidth = 5f
                            )
                        }
                    } else {
                        // Curved parent-child connector (Bezier Curve)
                        val parentNode = if (startPos.y < endPos.y) startPos else endPos
                        val childNode = if (startPos.y < endPos.y) endPos else startPos
                        
                        val pX = (parentNode.x + cardWidth / 2f) * density
                        val pY = (parentNode.y + cardHeight) * density
                        val cX = (childNode.x + cardWidth / 2f) * density
                        val cY = childNode.y * density
                        
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
        }

        // 2. Render cards as actual jetpack Compose Composables
        nodes.forEach { member ->
            val pos = nodePositions[member.id] ?: Offset.Zero
            val hasChildren = edges.any { it.subjectId == member.id && it.type == RelationshipType.PARENT }
            
            // Search highlighting logic
            val isSearchActive = searchQuery.isNotBlank()
            val matchesSearch = isSearchActive && (
                member.firstName.contains(searchQuery, ignoreCase = true) ||
                member.lastName.contains(searchQuery, ignoreCase = true)
            )
            val isDimmed = isSearchActive && !matchesSearch
            val isHighlighted = isSearchActive && matchesSearch

            FamilyNodeCard(
                member = member,
                profilePhotoUri = profilePhotos[member.id],
                isSelected = selectedNodeIds.contains(member.id),
                isHighlighted = isHighlighted || highlightedNodeId == member.id,
                isDimmed = isDimmed && highlightedNodeId != member.id,
                isCollapsed = collapsedNodeIds.contains(member.id),
                hasChildren = hasChildren,
                onClick = { onNodeClick(member.id) },
                onLongClick = { onNodeLongClick(member.id) },
                onToggleCollapse = { onNodeToggleCollapse(member.id) },
                modifier = Modifier
                    .offset(x = pos.x.dp, y = pos.y.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FamilyNodeCard(
    member: Member,
    profilePhotoUri: String?,
    isSelected: Boolean,
    isHighlighted: Boolean,
    isDimmed: Boolean,
    isCollapsed: Boolean,
    hasChildren: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onToggleCollapse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardColor = member.cardColor?.let { hex ->
        try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            null
        }
    } ?: MaterialTheme.colorScheme.surface

    val accentColor = when (member.gender) {
        Gender.MALE -> MaleAccent
        Gender.FEMALE -> FemaleAccent
        Gender.OTHER -> OtherAccent
    }

    val alphaValue = if (isDimmed) 0.3f else 1.0f

    val borderStrokeColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else if (isHighlighted) {
        Color(0xFFD4AF37) // Golden outline for matches
    } else {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    }

    val borderStrokeWidth = if (isSelected) {
        3.dp
    } else if (isHighlighted) {
        2.5.dp
    } else {
        1.dp
    }

    Box(
        modifier = modifier
            .width(220.dp)
            .height(104.dp) // extra padding height for the collapse overlay button
            .alpha(alphaValue)
    ) {
        Card(
            modifier = Modifier
                .width(220.dp)
                .height(90.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(borderStrokeWidth, borderStrokeColor, RoundedCornerShape(16.dp))
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                ),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left hand gender stripe
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(accentColor)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Avatar Box
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(accentColor.copy(alpha = 0.15f))
                        .border(1.5.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (profilePhotoUri != null) {
                        AsyncImage(
                            model = profilePhotoUri,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val initials = if (member.firstName.isNotEmpty()) member.firstName.take(1).uppercase() else "?"
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Details Text
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    val fullName = "${member.firstName} ${member.lastName}"
                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = SerifFontFamily
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    val datesText = buildString {
                        if (member.dateOfBirth != null) {
                            val birthYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(member.dateOfBirth))
                            append(birthYear)
                            if (!member.isLiving && member.dateOfDeath != null) {
                                val deathYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(member.dateOfDeath))
                                append(" - $deathYear")
                            } else {
                                val age = ((System.currentTimeMillis() - member.dateOfBirth) / (1000L * 60 * 60 * 24 * 365.25)).toInt()
                                append(" ($age yrs)")
                            }
                        } else {
                            append(if (member.isLiving) "Living" else "Deceased")
                        }
                    }

                    Text(
                        text = datesText,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1
                    )

                    if (!member.placeOfBirth.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "📍 ${member.placeOfBirth}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 9.sp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Overlay collapse button centered at the bottom edge of the card
        if (hasChildren) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 80.dp) // card height is 90, so centering it around the bottom border
            ) {
                IconButton(
                    onClick = onToggleCollapse,
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(1.5.dp, Color.White, CircleShape)
                ) {
                    Text(
                        text = if (isCollapsed) "+" else "—",
                        style = TextStyle(
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        ),
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                }
            }
        }
    }
}
