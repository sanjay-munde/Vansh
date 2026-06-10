package com.vansh.familytree.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "relationships",
    foreignKeys = [
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Member::class,
            parentColumns = ["id"],
            childColumns = ["targetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("subjectId"),
        Index("targetId")
    ]
)
data class Relationship(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val subjectId: String,
    val targetId: String,
    val type: RelationshipType,
    val subtype: RelationshipSubtype? = null,
    val startDate: Long? = null, // e.g., Marriage Date
    val endDate: Long? = null, // e.g., Divorce Date
    val location: String? = null // e.g., Marriage Location
)

enum class RelationshipType {
    PARENT,   // Subject is the parent of Target
    CHILD,    // Subject is the child of Target
    SPOUSE    // Subject is the spouse of Target
}

enum class RelationshipSubtype {
    BIOLOGICAL, ADOPTED, STEP, MARRIED, DIVORCED, SEPARATED
}
