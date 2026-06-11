package com.vansh.familytree.ui.member

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.repository.FamilyTreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.RelationshipType
import com.vansh.familytree.data.entity.RelationshipSubtype

import com.vansh.familytree.domain.timeline.TimelineEvent
import com.vansh.familytree.domain.timeline.TimelineGenerator
import com.vansh.familytree.data.entity.Media
import com.vansh.familytree.data.entity.MediaType
import com.vansh.familytree.data.local.LocalMediaManager

@HiltViewModel
class MemberProfileViewModel @Inject constructor(
    private val repository: FamilyTreeRepository,
    private val timelineGenerator: TimelineGenerator,
    private val localMediaManager: LocalMediaManager
) : ViewModel() {

    private val _member = MutableStateFlow<Member?>(null)
    val member: StateFlow<Member?> = _member

    private val _media = MutableStateFlow<List<Media>>(emptyList())
    val media: StateFlow<List<Media>> = _media

    private val _timeline = MutableStateFlow<List<TimelineEvent>>(emptyList())
    val timeline: StateFlow<List<TimelineEvent>> = _timeline

    private val _relationships = MutableStateFlow<List<Relationship>>(emptyList())
    val relationships: StateFlow<List<Relationship>> = _relationships

    val allMembers: StateFlow<List<Member>> = repository.getAllMembers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun loadMember(id: String) {
        viewModelScope.launch {
            _member.value = repository.getMemberById(id).firstOrNull()
            _timeline.value = timelineGenerator.generateTimeline(id)
            repository.getMediaForMember(id).collect { mediaList ->
                _media.value = mediaList
            }
        }
        viewModelScope.launch {
            repository.getRelationshipsForMember(id).collect { rels ->
                _relationships.value = rels
            }
        }
    }

    fun addProfilePhoto(uri: Uri) {
        val memberId = _member.value?.id ?: return
        viewModelScope.launch {
            val internalUri = localMediaManager.copyUriToInternalStorage(uri)
            if (internalUri != null) {
                val media = Media(
                    memberId = memberId,
                    uri = internalUri.toString(),
                    type = MediaType.PHOTO,
                    isProfilePhoto = true
                )
                repository.insertMedia(media)
            }
        }
    }

    fun addDocument(uri: Uri) {
        val memberId = _member.value?.id ?: return
        viewModelScope.launch {
            val internalUri = localMediaManager.copyUriToInternalStorage(uri)
            if (internalUri != null) {
                val media = Media(
                    memberId = memberId,
                    uri = internalUri.toString(),
                    type = MediaType.DOCUMENT,
                    isProfilePhoto = false
                )
                repository.insertMedia(media)
            }
        }
    }

    fun addRelationship(
        targetId: String,
        type: RelationshipType,
        subtype: RelationshipSubtype?,
        startDate: Long?,
        endDate: Long?,
        location: String?
    ) {
        val subjectId = _member.value?.id ?: return
        viewModelScope.launch {
            val relationship = Relationship(
                subjectId = subjectId,
                targetId = targetId,
                type = type,
                subtype = subtype,
                startDate = startDate,
                endDate = endDate,
                location = location
            )
            repository.insertRelationship(relationship)
        }
    }
}
