package com.vansh.familytree.ui.tree

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MegaTreeScreen(
    onNavigateBack: () -> Unit,
    viewModel: TreeViewModel = hiltViewModel()
) {
    val nodes by viewModel.graphNodes.collectAsState()
    val edges by viewModel.graphEdges.collectAsState()
    val nodePositions by viewModel.nodePositions.collectAsState()
    val collapsedNodeIds by viewModel.collapsedNodeIds.collectAsState()
    val highlightedNodeId by viewModel.highlightedNodeId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mega Family Tree") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* Add specific tree actions later */ }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Node")
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
                    onNodeDrag = { nodeId, delta -> viewModel.updateNodePosition(nodeId, delta) },
                    onNodeToggleCollapse = { nodeId -> viewModel.toggleNodeCollapse(nodeId) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
