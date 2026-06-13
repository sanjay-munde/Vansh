package com.vansh.familytree.ui.tree

import androidx.compose.ui.geometry.Offset
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.RelationshipType
import javax.inject.Inject

class TreeLayoutEngine @Inject constructor() {

    /**
     * Computes a basic hierarchical layout.
     * Parents on top, children below. Spouses adjacent.
     * For a mega tree, this is a complex NP-hard graph drawing problem (Sugiyama framework).
     * Here, we implement a simplified Breadth-First traversal layout for Phase 5 testing.
     */
    fun computeLayout(
        nodes: List<Member>,
        edges: List<Relationship>,
        collapsedNodeIds: Set<String> = emptySet(),
        viewMode: ViewMode = ViewMode.FULL_TREE,
        cardWidth: Float = 200f,
        cardHeight: Float = 80f,
        horizontalSpacing: Float = 40f,
        verticalSpacing: Float = 100f
    ): Map<String, Offset> {
        val positions = mutableMapOf<String, Offset>()
        if (nodes.isEmpty()) return positions

        // Find root nodes (nodes with no parents)
        val parentIds = edges.filter { it.type == RelationshipType.PARENT }.map { it.subjectId }.toSet()
        val childIds = edges.filter { it.type == RelationshipType.CHILD }.map { it.subjectId }.toSet()
        // Or simply nodes that are never a target in a PARENT relation, or subject in a CHILD relation.
        
        // For simplicity in this dummy layout, we'll assign generations.
        val generations = mutableMapOf<String, Int>()
        val queue = ArrayDeque<String>()
        
        // Pick an arbitrary root to start
        val startNode = nodes.first().id
        generations[startNode] = 0
        queue.add(startNode)

        val visited = mutableSetOf<String>()

        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            visited.add(current)
            val currentGen = generations[current] ?: 0

            val relatedEdges = edges.filter { it.subjectId == current || it.targetId == current }
            for (edge in relatedEdges) {
                val nextId = if (edge.subjectId == current) edge.targetId else edge.subjectId
                
                // Filter by ViewMode
                if (viewMode == ViewMode.ANCESTOR && edge.type == RelationshipType.PARENT && edge.subjectId == current) {
                    continue // Skip children
                }
                if (viewMode == ViewMode.DESCENDANT && edge.type == RelationshipType.PARENT && edge.targetId == current) {
                    continue // Skip parents
                }

                if (!visited.contains(nextId)) {
                    val nextGen = when {
                        edge.type == RelationshipType.SPOUSE -> currentGen
                        edge.subjectId == current && edge.type == RelationshipType.PARENT -> currentGen + 1 // Child is 1 gen below
                        edge.targetId == current && edge.type == RelationshipType.PARENT -> currentGen - 1 // Parent is 1 gen above
                        else -> currentGen
                    }
                    if (!generations.containsKey(nextId)) {
                        // Skip descendants if current node is collapsed
                        if (collapsedNodeIds.contains(current) && edge.type == RelationshipType.PARENT && edge.subjectId == current) {
                            continue
                        }
                        generations[nextId] = nextGen
                        queue.add(nextId)
                    }
                }
            }
        }

        // Handle disconnected graphs
        nodes.forEach { member ->
            if (!generations.containsKey(member.id)) {
                generations[member.id] = 0 // Put isolated nodes at gen 0
            }
        }

        // Layered Assignment (X coordinates)
        val nodesByGen = generations.entries.groupBy({ it.value }, { it.key })
        val spouseEdges = edges.filter { it.type == RelationshipType.SPOUSE }
        
        val minGen = nodesByGen.keys.minOrNull() ?: 0
        val maxGen = nodesByGen.keys.maxOrNull() ?: 0
        
        val xPositions = mutableMapOf<String, Float>()
        
        for (gen in minGen..maxGen) {
            val genNodes = nodesByGen[gen] ?: emptyList()
            var currentX = 0f
            
            // Sort nodes by parent X positions from previous generation to minimize crossings
            val sortedGenNodes = genNodes.sortedBy { childId ->
                val parentEdges = edges.filter { it.targetId == childId && it.type == RelationshipType.PARENT }
                val parentsInPrevGen = parentEdges.filter { generations[it.subjectId] == gen - 1 }.map { it.subjectId }
                
                if (parentsInPrevGen.isNotEmpty()) {
                    parentsInPrevGen.mapNotNull { xPositions[it] }.average().toFloat()
                } else {
                    Float.MAX_VALUE 
                }
            }
            
            val placedInGen = mutableSetOf<String>()
            
            for (nodeId in sortedGenNodes) {
                if (placedInGen.contains(nodeId)) continue
                
                // Group with spouses in the same generation
                val spouses = spouseEdges.filter { 
                    (it.subjectId == nodeId && genNodes.contains(it.targetId)) || 
                    (it.targetId == nodeId && genNodes.contains(it.subjectId)) 
                }.map { if (it.subjectId == nodeId) it.targetId else it.subjectId }
                
                val group = listOf(nodeId) + spouses.filter { !placedInGen.contains(it) }
                
                // Attempt to center below parents
                val parentEdges = edges.filter { group.contains(it.targetId) && it.type == RelationshipType.PARENT }
                val parentsInPrevGen = parentEdges.filter { generations[it.subjectId] == gen - 1 }.map { it.subjectId }
                
                var idealX = currentX
                if (parentsInPrevGen.isNotEmpty()) {
                    val avgParentX = parentsInPrevGen.mapNotNull { xPositions[it] }.average().toFloat()
                    val groupWidth = group.size * (cardWidth + horizontalSpacing) - horizontalSpacing
                    idealX = maxOf(currentX, avgParentX - groupWidth / 2f)
                }
                
                // Place group
                var tempX = idealX
                for (memberId in group) {
                    xPositions[memberId] = tempX
                    placedInGen.add(memberId)
                    tempX += cardWidth + horizontalSpacing
                }
                currentX = tempX
            }
            
            // Center the entire generation around X=0
            val minX = genNodes.mapNotNull { xPositions[it] }.minOrNull() ?: 0f
            val maxX = genNodes.mapNotNull { xPositions[it] }.maxOrNull() ?: 0f
            val genWidth = maxX - minX + cardWidth
            val offsetX = - (genWidth / 2f) - minX
            
            genNodes.forEach { id ->
                xPositions[id] = (xPositions[id] ?: 0f) + offsetX
            }
        }
        
        nodes.forEach { member ->
            val gen = generations[member.id] ?: 0
            val x = xPositions[member.id] ?: 0f
            val y = gen * (cardHeight + verticalSpacing)
            positions[member.id] = Offset(x, y)
        }

        return positions
    }
}
