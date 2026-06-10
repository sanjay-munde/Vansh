package com.vansh.familytree.data.repository

import com.vansh.familytree.data.dao.MediaDao
import com.vansh.familytree.data.dao.MemberDao
import com.vansh.familytree.data.dao.RelationshipDao
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.Media
import com.vansh.familytree.data.entity.FilterCriteria
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FamilyTreeRepository @Inject constructor(
    private val memberDao: MemberDao,
    private val relationshipDao: RelationshipDao,
    private val mediaDao: MediaDao
) {
    fun getAllMembers(): Flow<List<Member>> = memberDao.getAllMembers()

    fun filterMembers(criteria: FilterCriteria): Flow<List<Member>> {
        return memberDao.getAllMembers().map { members ->
            members.filter { member ->
                val matchesQuery = if (criteria.query.isNotBlank()) {
                    member.firstName.contains(criteria.query, ignoreCase = true) ||
                    member.lastName.contains(criteria.query, ignoreCase = true)
                } else true
                
                val matchesLiving = criteria.isLiving?.let { it == member.isLiving } ?: true
                val matchesGender = criteria.gender?.let { it == member.gender } ?: true
                val matchesVillage = if (!criteria.placeOfBirth.isNullOrBlank()) {
                    member.placeOfBirth?.contains(criteria.placeOfBirth, ignoreCase = true) == true
                } else true
                
                matchesQuery && matchesLiving && matchesGender && matchesVillage
            }
        }
    }

    fun searchMembers(query: String): Flow<List<Member>> = memberDao.searchMembers(query)

    fun getMemberById(id: String): Flow<Member?> = memberDao.getMemberById(id)

    suspend fun insertMember(member: Member) = memberDao.insertMember(member)

    suspend fun updateMember(member: Member) = memberDao.updateMember(member)

    suspend fun deleteMember(member: Member) = memberDao.deleteMember(member)

    fun getAllRelationships(): Flow<List<Relationship>> = relationshipDao.getAllRelationships()

    fun getRelationshipsForMember(memberId: String): Flow<List<Relationship>> =
        relationshipDao.getRelationshipsForMember(memberId)

    suspend fun insertRelationship(relationship: Relationship) =
        relationshipDao.insertRelationship(relationship)

    suspend fun deleteRelationship(relationship: Relationship) =
        relationshipDao.deleteRelationship(relationship)

    fun getMediaForMember(memberId: String): Flow<List<Media>> = mediaDao.getMediaForMember(memberId)

    suspend fun insertMedia(media: Media) = mediaDao.insertMedia(media)

    suspend fun deleteMedia(media: Media) = mediaDao.deleteMedia(media)
}
