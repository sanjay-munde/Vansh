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
