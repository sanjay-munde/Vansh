package com.vansh.familytree.ui.tree

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import com.vansh.familytree.domain.export.TreeExportUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MegaTreeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToMemberProfile: (String) -> Unit,
    onNavigateToMemberForm: (String?) -> Unit,
    viewModel: TreeViewModel = hiltViewModel()
) {
    val nodes by viewModel.graphNodes.collectAsState()
    val edges by viewModel.graphEdges.collectAsState()
    val nodePositions by viewModel.nodePositions.collectAsState()
    val collapsedNodeIds by viewModel.collapsedNodeIds.collectAsState()
    val highlightedNodeId by viewModel.highlightedNodeId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectionMode by viewModel.selectionMode.collectAsState()
    val selectedNodeIds by viewModel.selectedNodeIds.collectAsState()

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("image/png"),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    TreeExportUtils.exportTreeToBitmap(context, nodes, edges, nodePositions, it)
                }
            }
        }
    )

    val colorPickers = listOf(
        Pair("Blue", "#E8F0FE"),
        Pair("Pink", "#FCE8E6"),
        Pair("Green", "#E6F4EA"),
        Pair("Yellow", "#FEF7E0"),
        Pair("Clear", null)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mega Family Tree", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    var menuExpanded by remember { mutableStateOf(false) }
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Reset View") },
                            onClick = {
                                scale = 1f
                                offset = Offset.Zero
                                menuExpanded = false
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Full Tree") },
                            onClick = {
                                viewModel.setViewMode(ViewMode.FULL_TREE)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Ancestors Only") },
                            onClick = {
                                viewModel.setViewMode(ViewMode.ANCESTOR)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Descendants Only") },
                            onClick = {
                                viewModel.setViewMode(ViewMode.DESCENDANT)
                                menuExpanded = false
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Color by Gender") },
                            onClick = {
                                viewModel.autoColorNodes(AutoColorMode.GENDER)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Color by Status") },
                            onClick = {
                                viewModel.autoColorNodes(AutoColorMode.STATUS)
                                menuExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Colors") },
                            onClick = {
                                viewModel.autoColorNodes(AutoColorMode.CLEAR)
                                menuExpanded = false
                            }
                        )
                        Divider()
                        DropdownMenuItem(
                            text = { Text("Export as PNG") },
                            onClick = {
                                exportLauncher.launch("family_tree.png")
                                menuExpanded = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            if (!selectionMode) {
                FloatingActionButton(
                    onClick = { onNavigateToMemberForm(null) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Member")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            
            // Search Input bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                placeholder = { Text("Filter tree by name...") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
            
            // Canvas Wrapper Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                TreeCanvas(
                    nodes = nodes,
                    edges = edges,
                    nodePositions = nodePositions,
                    collapsedNodeIds = collapsedNodeIds,
                    highlightedNodeId = highlightedNodeId,
                    selectedNodeIds = selectedNodeIds,
                    scale = scale,
                    offset = offset,
                    onScaleChange = { scale = it },
                    onOffsetChange = { offset = it },
                    onNodeDrag = { nodeId, delta -> viewModel.updateNodePosition(nodeId, delta) },
                    onNodeToggleCollapse = { nodeId -> viewModel.toggleNodeCollapse(nodeId) },
                    onNodeClick = { nodeId -> 
                        if (selectionMode) {
                            viewModel.toggleNodeSelection(nodeId)
                        } else {
                            onNavigateToMemberProfile(nodeId) 
                        }
                    },
                    onNodeLongClick = { nodeId -> 
                        if (!selectionMode) {
                            viewModel.toggleSelectionMode()
                            viewModel.toggleNodeSelection(nodeId)
                        } else {
                            onNavigateToMemberForm(nodeId) 
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                
                // FLOATING CANVAS NAVIGATION CONTROLS (Zoom +, Zoom -, Reset)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SmallFloatingActionButton(
                        onClick = { scale = (scale * 1.2f).coerceAtMost(5f) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text("+", style = MaterialTheme.typography.titleMedium)
                    }
                    SmallFloatingActionButton(
                        onClick = { scale = (scale / 1.2f).coerceAtLeast(0.1f) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ) {
                        Text("—", style = MaterialTheme.typography.titleMedium)
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            scale = 1f
                            offset = Offset.Zero
                        },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Text("⟲", style = MaterialTheme.typography.titleSmall)
                    }
                }
            }

            // SELECTION AND BULK ACTIONS OVERLAY
            if (selectionMode) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Selected members: ${selectedNodeIds.size}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            IconButton(
                                onClick = { viewModel.toggleSelectionMode() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Close Selection Mode")
                            }
                        }

                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Bulk Color Pickers
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Assign Color:", style = MaterialTheme.typography.labelSmall)
                                colorPickers.forEach { (name, hex) ->
                                    val color = hex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.outline
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color)
                                            .border(1.dp, Color.Gray.copy(alpha = 0.4f), CircleShape)
                                            .clickable { viewModel.bulkColorSelectedNodes(hex) }
                                    )
                                }
                            }

                            // Bulk Delete Button
                            Button(
                                onClick = { viewModel.bulkDeleteSelectedNodes() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(10.dp),
                                enabled = selectedNodeIds.isNotEmpty()
                            ) {
                                Text("Delete", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}
