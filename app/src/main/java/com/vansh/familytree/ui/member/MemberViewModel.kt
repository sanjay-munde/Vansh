package com.vansh.familytree.ui.member

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vansh.familytree.data.entity.FilterCriteria
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.repository.FamilyTreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemberViewModel @Inject constructor(
    private val repository: FamilyTreeRepository
) : ViewModel() {

    private val _filterCriteria = MutableStateFlow(FilterCriteria())
    val filterCriteria: StateFlow<FilterCriteria> = _filterCriteria

    val members: StateFlow<List<Member>> = _filterCriteria
        .flatMapLatest { criteria ->
            repository.filterMembers(criteria)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateFilterCriteria(criteria: FilterCriteria) {
        _filterCriteria.value = criteria
    }

    private val _duplicateWarning = MutableStateFlow<String?>(null)
    val duplicateWarning: StateFlow<String?> = _duplicateWarning

    fun clearDuplicateWarning() {
        _duplicateWarning.value = null
    }

    fun saveMemberWithValidation(member: Member, ignoreWarning: Boolean = false, onSuccess: () -> Unit) {
        viewModelScope.launch {
            if (!ignoreWarning) {
                val all = repository.getAllMembers().firstOrNull() ?: emptyList()
                val isDuplicate = all.any { 
                    it.id != member.id && 
                    it.firstName.equals(member.firstName, ignoreCase = true) && 
                    it.lastName.equals(member.lastName, ignoreCase = true) &&
                    it.dateOfBirth == member.dateOfBirth
                }
                
                if (isDuplicate) {
                    _duplicateWarning.value = "A member named ${member.firstName} ${member.lastName} with the same birth details already exists. Are you sure you want to save?"
                    return@launch
                }
            }
            repository.insertMember(member)
            onSuccess()
        }
    }

    fun getMemberById(id: String): Flow<Member?> = repository.getMemberById(id)

    fun saveMember(member: Member) {
        viewModelScope.launch {
            repository.insertMember(member)
        }
    }

    fun deleteMember(member: Member) {
        viewModelScope.launch {
            repository.deleteMember(member)
        }
    }
}
