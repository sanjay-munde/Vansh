package com.vansh.familytree.domain.kinship

import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.RelationshipType
import com.vansh.familytree.data.repository.FamilyTreeRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class KinshipCalculator @Inject constructor(
    private val repository: FamilyTreeRepository
) {
    /**
     * Calculates kinship (e.g., "Uncle", "First Cousin") between two members.
     * Finds the shortest path in the undirected relationship graph and maps it.
     */
    suspend fun calculateKinship(startMemberId: String, targetMemberId: String): String {
        if (startMemberId == targetMemberId) return "Self"

        // Queue stores Pair of current ID and the path taken to reach it
        val queue = ArrayDeque<Pair<String, List<RelationshipEdge>>>()
        val visited = mutableSetOf<String>()

        queue.add(Pair(startMemberId, emptyList()))

        while (queue.isNotEmpty()) {
            val (currentId, path) = queue.removeFirst()
            
            if (currentId == targetMemberId) {
                return mapPathToKinship(path)
            }

            visited.add(currentId)

            val relations = repository.getRelationshipsForMember(currentId).firstOrNull() ?: emptyList()

            for (rel in relations) {
                val nextId = if (rel.subjectId == currentId) rel.targetId else rel.subjectId
                if (!visited.contains(nextId)) {
                    val edge = determineDirectionalEdge(currentId, nextId, rel)
                    queue.add(Pair(nextId, path + edge))
                }
            }
        }

        return "Unrelated"
    }

    private fun determineDirectionalEdge(fromId: String, toId: String, rel: Relationship): RelationshipEdge {
        return if (rel.type == RelationshipType.SPOUSE) {
            RelationshipEdge.SPOUSE
        } else if (rel.subjectId == fromId && rel.type == RelationshipType.PARENT) {
            RelationshipEdge.CHILD // From perspective of parent looking at child
        } else if (rel.subjectId == fromId && rel.type == RelationshipType.CHILD) {
            RelationshipEdge.PARENT
        } else if (rel.targetId == fromId && rel.type == RelationshipType.PARENT) {
            RelationshipEdge.PARENT
        } else {
            RelationshipEdge.CHILD
        }
    }

    private fun mapPathToKinship(path: List<RelationshipEdge>): String {
        // Simplified Path matching logic
        val pathString = path.joinToString("-") { it.name }
        
        return when (pathString) {
            "PARENT" -> "Parent"
            "CHILD" -> "Child"
            "SPOUSE" -> "Spouse"
            "PARENT-PARENT" -> "Grandparent"
            "CHILD-CHILD" -> "Grandchild"
            "PARENT-CHILD" -> "Sibling"
            "PARENT-PARENT-CHILD" -> "Uncle / Aunt"
            "PARENT-CHILD-CHILD" -> "Nephew / Niece"
            "PARENT-PARENT-CHILD-CHILD" -> "First Cousin"
            "SPOUSE-PARENT" -> "Parent-in-Law"
            "CHILD-SPOUSE" -> "Child-in-Law"
            "PARENT-CHILD-SPOUSE" -> "Sibling-in-Law" // Spouse of sibling
            "SPOUSE-PARENT-CHILD" -> "Sibling-in-Law" // Sibling of spouse
            else -> "Relative ($pathString)"
        }
    }
}

enum class RelationshipEdge {
    PARENT, CHILD, SPOUSE
}
