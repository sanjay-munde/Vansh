package com.vansh.familytree.ui.tree

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.repository.FamilyTreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ViewMode {
    FULL_TREE, ANCESTOR, DESCENDANT
}

@HiltViewModel
class TreeViewModel @Inject constructor(
    private val repository: FamilyTreeRepository
) : ViewModel() {

    private val _graphNodes = MutableStateFlow<List<Member>>(emptyList())
    val graphNodes: StateFlow<List<Member>> = _graphNodes

    private val _graphEdges = MutableStateFlow<List<Relationship>>(emptyList())
    val graphEdges: StateFlow<List<Relationship>> = _graphEdges

    private val _nodePositions = MutableStateFlow<Map<String, Offset>>(emptyMap())
    val nodePositions: StateFlow<Map<String, Offset>> = _nodePositions

    private val _collapsedNodeIds = MutableStateFlow<Set<String>>(emptySet())
    val collapsedNodeIds: StateFlow<Set<String>> = _collapsedNodeIds

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _highlightedNodeId = MutableStateFlow<String?>(null)
    val highlightedNodeId: StateFlow<String?> = _highlightedNodeId

    private val _viewMode = MutableStateFlow<ViewMode>(ViewMode.FULL_TREE)
    val viewMode: StateFlow<ViewMode> = _viewMode

    private val layoutEngine = TreeLayoutEngine()

    init {
        loadEntireGraph()
    }

    private fun loadEntireGraph() {
        viewModelScope.launch {
            combine(
                repository.getAllMembers(),
                repository.getAllRelationships(),
                _collapsedNodeIds,
                _searchQuery,
                _viewMode
            ) { nodes, edges, collapsedIds, query, mode ->
                
                val filteredNodes = if (query.isBlank()) {
                    nodes
                } else {
                    nodes.filter { it.firstName.contains(query, ignoreCase = true) || it.lastName.contains(query, ignoreCase = true) }
                }

                _graphNodes.value = filteredNodes
                _graphEdges.value = edges
                _nodePositions.value = layoutEngine.computeLayout(filteredNodes, edges, collapsedIds, mode)
            }.collect { }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setHighlightedNode(nodeId: String?) {
        _highlightedNodeId.value = nodeId
    }

    fun setViewMode(mode: ViewMode) {
        _viewMode.value = mode
    }

    fun updateNodePosition(nodeId: String, delta: Offset) {
        val currentPositions = _nodePositions.value.toMutableMap()
        val oldPos = currentPositions[nodeId] ?: Offset.Zero
        currentPositions[nodeId] = oldPos + delta
        _nodePositions.value = currentPositions
    }

    fun toggleNodeCollapse(nodeId: String) {
        val current = _collapsedNodeIds.value.toMutableSet()
        if (current.contains(nodeId)) {
            current.remove(nodeId)
        } else {
            current.add(nodeId)
        }
        _collapsedNodeIds.value = current
    }
}
