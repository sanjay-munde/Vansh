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
        if (path.isEmpty()) return "Self"
        if (path.size == 1 && path[0] == RelationshipEdge.SPOUSE) return "Spouse"

        var hasSpousePrefix = false
        var hasSpouseSuffix = false
        
        val bloodPath = path.toMutableList()
        if (bloodPath.first() == RelationshipEdge.SPOUSE) {
            hasSpousePrefix = true
            bloodPath.removeFirst()
        } else if (bloodPath.last() == RelationshipEdge.SPOUSE) {
            hasSpouseSuffix = true
            bloodPath.removeLast()
        }
        
        if (bloodPath.contains(RelationshipEdge.SPOUSE)) {
            return "Relative by Marriage"
        }

        var up = 0
        var down = 0
        var phase = "UP"
        
        for (edge in bloodPath) {
            if (edge == RelationshipEdge.PARENT) {
                if (phase == "DOWN") return "Relative (Complex lineage)"
                up++
            } else if (edge == RelationshipEdge.CHILD) {
                phase = "DOWN"
                down++
            }
        }
        
        val baseRelationship = getBloodRelationship(up, down)
        
        if (hasSpousePrefix || hasSpouseSuffix) {
            if (baseRelationship == "Parent") return "Parent-in-Law"
            if (baseRelationship == "Child") return "Child-in-Law"
            if (baseRelationship == "Sibling") return "Sibling-in-Law"
            return "$baseRelationship (by marriage)"
        }
        
        return baseRelationship
    }

    private fun getBloodRelationship(up: Int, down: Int): String {
        if (up == 0) {
            if (down == 0) return "Self"
            if (down == 1) return "Child"
            if (down == 2) return "Grandchild"
            return "Great ".repeat(down - 2) + "Grandchild"
        }
        if (down == 0) {
            if (up == 1) return "Parent"
            if (up == 2) return "Grandparent"
            return "Great ".repeat(up - 2) + "Grandparent"
        }
        if (up == 1 && down == 1) return "Sibling"
        if (up == 1) {
            if (down == 2) return "Nephew / Niece"
            if (down == 3) return "Grandnephew / Grandniece"
            return "Great ".repeat(down - 3) + "Grandnephew / Grandniece"
        }
        if (down == 1) {
            if (up == 2) return "Uncle / Aunt"
            if (up == 3) return "Great Uncle / Aunt"
            return "Great ".repeat(up - 3) + "Granduncle / Grandaunt"
        }
        
        val cousinDegree = minOf(up, down) - 1
        val removed = kotlin.math.abs(up - down)
        
        val degreeStr = when (cousinDegree) {
            1 -> "First"
            2 -> "Second"
            3 -> "Third"
            4 -> "Fourth"
            5 -> "Fifth"
            else -> "${cousinDegree}th"
        }
        
        val removedStr = when (removed) {
            0 -> ""
            1 -> " Once Removed"
            2 -> " Twice Removed"
            3 -> " Thrice Removed"
            else -> " ${removed}x Removed"
        }
        
        return "$degreeStr Cousin$removedStr"
    }
}

enum class RelationshipEdge {
    PARENT, CHILD, SPOUSE
}
