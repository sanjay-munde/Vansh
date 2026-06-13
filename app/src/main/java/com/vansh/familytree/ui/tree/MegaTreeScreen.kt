package com.vansh.familytree.ui.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

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

    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mega Family Tree") },
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
                        HorizontalDivider()
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
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToMemberForm(null) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Member")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                placeholder = { Text("Filter tree by name...") }
            )
            
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                TreeCanvas(
                    nodes = nodes,
                    edges = edges,
                    nodePositions = nodePositions,
                    collapsedNodeIds = collapsedNodeIds,
                    highlightedNodeId = highlightedNodeId,
                    scale = scale,
                    offset = offset,
                    onScaleChange = { scale = it },
                    onOffsetChange = { offset = it },
                    onNodeDrag = { nodeId, delta -> viewModel.updateNodePosition(nodeId, delta) },
                    onNodeToggleCollapse = { nodeId -> viewModel.toggleNodeCollapse(nodeId) },
                    onNodeClick = { nodeId -> onNavigateToMemberProfile(nodeId) },
                    onNodeLongClick = { nodeId -> onNavigateToMemberForm(nodeId) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
