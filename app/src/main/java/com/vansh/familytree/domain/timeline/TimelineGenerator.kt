package com.vansh.familytree.domain.timeline

import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.RelationshipType
import com.vansh.familytree.data.repository.FamilyTreeRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

data class TimelineEvent(
    val year: Int?,
    val timestamp: Long?,
    val title: String,
    val description: String,
    val type: EventType
)

enum class EventType {
    BIRTH, MARRIAGE, CHILD_BORN, DEATH, HISTORICAL
}

class TimelineGenerator @Inject constructor(
    private val repository: FamilyTreeRepository
) {
    suspend fun generateTimeline(memberId: String): List<TimelineEvent> {
        val member = repository.getMemberById(memberId).firstOrNull() ?: return emptyList()
        val relationships = repository.getRelationshipsForMember(memberId).firstOrNull() ?: emptyList()

        val events = mutableListOf<TimelineEvent>()

        // 1. Birth Event
        if (member.dateOfBirth != null) {
            events.add(
                TimelineEvent(
                    year = extractYear(member.dateOfBirth),
                    timestamp = member.dateOfBirth,
                    title = "Born",
                    description = "${member.firstName} was born in ${member.placeOfBirth ?: "an unknown location"}.",
                    type = EventType.BIRTH
                )
            )
        }

        // 2. Relationship Events (Marriage, Children)
        for (rel in relationships) {
            // Find the other person in the relationship
            val otherPersonId = if (rel.subjectId == memberId) rel.targetId else rel.subjectId
            val otherPerson = repository.getMemberById(otherPersonId).firstOrNull() ?: continue

            when (rel.type) {
                RelationshipType.SPOUSE -> {
                    events.add(
                        TimelineEvent(
                            year = null, // In a real app we'd have a marriageDate on the relationship entity
                            timestamp = null,
                            title = "Marriage",
                            description = "Married to ${otherPerson.firstName} ${otherPerson.lastName}.",
                            type = EventType.MARRIAGE
                        )
                    )
                }
                RelationshipType.CHILD -> {
                    if ((rel.type == RelationshipType.CHILD && rel.targetId == memberId) ||
                        (rel.type == RelationshipType.PARENT && rel.subjectId == memberId)
                    ) {
                        events.add(
                            TimelineEvent(
                                year = extractYear(otherPerson.dateOfBirth),
                                timestamp = otherPerson.dateOfBirth,
                                title = "Child Born",
                                description = "Child ${otherPerson.firstName} was born.",
                                type = EventType.CHILD_BORN
                            )
                        )
                    }
                }
                else -> {}
            }
        }

        // 3. Death Event
        if (!member.isLiving && member.dateOfDeath != null) {
            events.add(
                TimelineEvent(
                    year = extractYear(member.dateOfDeath),
                    timestamp = member.dateOfDeath,
                    title = "Death",
                    description = "${member.firstName} passed away.",
                    type = EventType.DEATH
                )
            )
        }

        // Sort chronologically by timestamp
        return events.sortedWith(compareBy(nullsLast()) { it.timestamp })
    }

    private fun extractYear(timestamp: Long?): Int? {
        if (timestamp == null) return null
        val calendar = java.util.Calendar.getInstance()
        calendar.timeInMillis = timestamp
        return calendar.get(java.util.Calendar.YEAR)
    }
}
