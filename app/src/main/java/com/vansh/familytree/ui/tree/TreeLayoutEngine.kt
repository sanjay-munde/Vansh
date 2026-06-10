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

        // Assign X positions based on index within the generation
        val nodesByGen = generations.entries.groupBy({ it.value }, { it.key })
        
        nodesByGen.forEach { (gen, memberIds) ->
            val startY = gen * (cardHeight + verticalSpacing)
            var startX = -(memberIds.size * (cardWidth + horizontalSpacing)) / 2f // Center the generation
            
            memberIds.forEach { id ->
                positions[id] = Offset(startX, startY)
                startX += cardWidth + horizontalSpacing
            }
        }

        return positions
    }
}
