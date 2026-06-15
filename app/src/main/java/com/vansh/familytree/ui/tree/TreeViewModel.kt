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
import androidx.compose.ui.geometry.Offset

enum class ViewMode {
    FULL_TREE, ANCESTOR, DESCENDANT
}

enum class AutoColorMode {
    GENDER, STATUS, CLEAR
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

    private val _profilePhotos = MutableStateFlow<Map<String, String>>(emptyMap())
    val profilePhotos: StateFlow<Map<String, String>> = _profilePhotos

    private val _collapsedNodeIds = MutableStateFlow<Set<String>>(emptySet())
    val collapsedNodeIds: StateFlow<Set<String>> = _collapsedNodeIds

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _highlightedNodeId = MutableStateFlow<String?>(null)
    val highlightedNodeId: StateFlow<String?> = _highlightedNodeId

    private val _viewMode = MutableStateFlow<ViewMode>(ViewMode.FULL_TREE)
    val viewMode: StateFlow<ViewMode> = _viewMode

    private val _selectionMode = MutableStateFlow(false)
    val selectionMode: StateFlow<Boolean> = _selectionMode

    private val _selectedNodeIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedNodeIds: StateFlow<Set<String>> = _selectedNodeIds

    private val layoutEngine = TreeLayoutEngine()

    init {
        loadEntireGraph()
        loadProfilePhotos()
    }

    private fun loadProfilePhotos() {
        viewModelScope.launch {
            repository.getAllProfilePhotos().collect { photos ->
                _profilePhotos.value = photos.associate { it.memberId to it.uri }
            }
        }
    }

    private fun loadEntireGraph() {
        viewModelScope.launch {
            combine(
                repository.getAllMembers(),
                repository.getAllRelationships(),
                _collapsedNodeIds,
                _viewMode
            ) { nodes, edges, collapsedIds, mode ->
                _graphNodes.value = nodes
                _graphEdges.value = edges
                _nodePositions.value = layoutEngine.computeLayout(
                    nodes = nodes,
                    edges = edges,
                    collapsedNodeIds = collapsedIds,
                    viewMode = mode,
                    cardWidth = 220f,
                    cardHeight = 90f,
                    horizontalSpacing = 50f,
                    verticalSpacing = 110f
                )
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

    fun autoColorNodes(mode: AutoColorMode) {
        viewModelScope.launch {
            val nodes = _graphNodes.value
            nodes.forEach { member ->
                val colorHex = when (mode) {
                    AutoColorMode.GENDER -> {
                        when (member.gender) {
                            com.vansh.familytree.data.entity.Gender.MALE -> "#ADD8E6" // Light Blue
                            com.vansh.familytree.data.entity.Gender.FEMALE -> "#FFB6C1" // Light Pink
                            com.vansh.familytree.data.entity.Gender.OTHER -> "#D3D3D3" // Light Gray
                        }
                    }
                    AutoColorMode.STATUS -> {
                        if (member.isLiving) "#90EE90" else "#D3D3D3" // Light Green vs Light Gray
                    }
                    AutoColorMode.CLEAR -> null
                }
                repository.insertMember(member.copy(cardColor = colorHex))
            }
        }
    }

    fun toggleSelectionMode() {
        val current = _selectionMode.value
        _selectionMode.value = !current
        if (current) {
            _selectedNodeIds.value = emptySet()
        }
    }

    fun toggleNodeSelection(nodeId: String) {
        if (!_selectionMode.value) return
        val current = _selectedNodeIds.value.toMutableSet()
        if (current.contains(nodeId)) {
            current.remove(nodeId)
        } else {
            current.add(nodeId)
        }
        _selectedNodeIds.value = current
    }

    fun bulkColorSelectedNodes(colorHex: String?) {
        viewModelScope.launch {
            val nodesToUpdate = _graphNodes.value.filter { _selectedNodeIds.value.contains(it.id) }
            nodesToUpdate.forEach { member ->
                repository.insertMember(member.copy(cardColor = colorHex))
            }
            _selectedNodeIds.value = emptySet()
            _selectionMode.value = false
        }
    }

    fun bulkDeleteSelectedNodes() {
        viewModelScope.launch {
            val nodesToDelete = _graphNodes.value.filter { _selectedNodeIds.value.contains(it.id) }
            nodesToDelete.forEach { member ->
                repository.deleteMember(member)
            }
            _selectedNodeIds.value = emptySet()
            _selectionMode.value = false
        }
    }
}
