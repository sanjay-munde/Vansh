package com.vansh.familytree.domain.validation

import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.RelationshipType
import com.vansh.familytree.data.repository.FamilyTreeRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class CycleDetectionValidator @Inject constructor(
    private val repository: FamilyTreeRepository
) {
    /**
     * Checks if adding this relationship will create an impossible lineage cycle.
     * E.g., Person A is Parent of Person B, and we try to add Person B as Parent of Person A.
     */
    suspend fun validate(newRelationship: Relationship): ValidationResult {
        if (newRelationship.subjectId == newRelationship.targetId) {
            return ValidationResult.Invalid("A person cannot be related to themselves in this way.")
        }

        // Only parent/child relationships can cause lineage cycles
        if (newRelationship.type == RelationshipType.SPOUSE) {
            return ValidationResult.Valid
        }

        val allRelationships = fetchAllGraphEdges()

        // Temporarily add the new relationship to the graph
        allRelationships.add(newRelationship)

        return if (hasLineageCycle(allRelationships)) {
            ValidationResult.Invalid("This relationship creates a circular lineage loop, which is biologically impossible.")
        } else {
            ValidationResult.Valid
        }
    }

    private suspend fun fetchAllGraphEdges(): MutableList<Relationship> {
        // In a real app with huge graphs, you'd fetch only the connected component,
        // or query the DB using recursive CTEs. For now, assuming a manageable graph size.
        // We'll simulate fetching all relations.
        // A better approach is to do a DFS starting from targetId going UP to check if we reach subjectId.
        return mutableListOf() // Placeholder, let's implement actual DFS
    }

    suspend fun validateByDFS(subjectId: String, targetId: String, type: RelationshipType): ValidationResult {
        // If subject is going to be parent of target, we must ensure target is NOT an ancestor of subject.
        if (subjectId == targetId) return ValidationResult.Invalid("Cannot link to self.")

        if (type == RelationshipType.PARENT) {
            val isAncestor = isTargetAnAncestorOfSubject(targetId, subjectId)
            if (isAncestor) return ValidationResult.Invalid("Cycle detected: Target is already an ancestor of the Subject.")
        } else if (type == RelationshipType.CHILD) {
            val isAncestor = isTargetAnAncestorOfSubject(subjectId, targetId)
            if (isAncestor) return ValidationResult.Invalid("Cycle detected: Subject is already an ancestor of the Target.")
        }

        return ValidationResult.Valid
    }

    private suspend fun isTargetAnAncestorOfSubject(targetId: String, subjectId: String): Boolean {
        // targetId is the potential ancestor. We need to check if targetId is actually reachable by traversing
        // 'parent' links upwards from subjectId.
        val visited = mutableSetOf<String>()
        val queue = ArrayDeque<String>()
        queue.add(subjectId)

        while (queue.isNotEmpty()) {
            val currentId = queue.removeFirst()
            if (currentId == targetId) return true

            visited.add(currentId)

            val relations = repository.getRelationshipsForMember(currentId).firstOrNull() ?: emptyList()
            
            // Find parents of currentId
            // A relation where currentId is subject, and type is CHILD (meaning target is parent)
            // Or currentId is target, and type is PARENT (meaning subject is parent)
            for (rel in relations) {
                var parentId: String? = null
                if (rel.subjectId == currentId && rel.type == RelationshipType.CHILD) {
                    parentId = rel.targetId
                } else if (rel.targetId == currentId && rel.type == RelationshipType.PARENT) {
                    parentId = rel.subjectId
                }

                if (parentId != null && !visited.contains(parentId)) {
                    queue.add(parentId)
                }
            }
        }
        return false
    }
}
