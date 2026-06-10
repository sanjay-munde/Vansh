package com.vansh.familytree.ui.member

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.repository.FamilyTreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

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

    fun loadMember(id: String) {
        viewModelScope.launch {
            _member.value = repository.getMemberById(id).firstOrNull()
            _timeline.value = timelineGenerator.generateTimeline(id)
            repository.getMediaForMember(id).collect { mediaList ->
                _media.value = mediaList
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
}
