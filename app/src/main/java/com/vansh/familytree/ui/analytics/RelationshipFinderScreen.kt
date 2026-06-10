package com.vansh.familytree.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.vansh.familytree.R
import androidx.lifecycle.viewModelScope
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.repository.FamilyTreeRepository
import com.vansh.familytree.domain.kinship.KinshipCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RelationshipFinderViewModel @Inject constructor(
    private val repository: FamilyTreeRepository,
    private val kinshipCalculator: KinshipCalculator
) : ViewModel() {
    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members
    
    private val _selectedMemberA = MutableStateFlow<Member?>(null)
    val selectedMemberA: StateFlow<Member?> = _selectedMemberA
    
    private val _selectedMemberB = MutableStateFlow<Member?>(null)
    val selectedMemberB: StateFlow<Member?> = _selectedMemberB
    
    private val _kinshipPath = MutableStateFlow<String?>(null)
    val kinshipPath: StateFlow<String?> = _kinshipPath

    init {
        viewModelScope.launch {
            repository.getAllMembers().collect {
                _members.value = it
            }
        }
    }
    
    fun selectMemberA(member: Member?) {
        _selectedMemberA.value = member
        calculate()
    }
    
    fun selectMemberB(member: Member?) {
        _selectedMemberB.value = member
        calculate()
    }
    
    private fun calculate() {
        val a = _selectedMemberA.value
        val b = _selectedMemberB.value
        if (a != null && b != null) {
            viewModelScope.launch {
                _kinshipPath.value = kinshipCalculator.calculateKinship(a.id, b.id)
            }
        } else {
            _kinshipPath.value = null
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipFinderScreen(
    onNavigateBack: () -> Unit,
    viewModel: RelationshipFinderViewModel = hiltViewModel()
) {
    val members by viewModel.members.collectAsState()
    val memberA by viewModel.selectedMemberA.collectAsState()
    val memberB by viewModel.selectedMemberB.collectAsState()
    val path by viewModel.kinshipPath.collectAsState()
    
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.relationship_finder)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            
            ExposedDropdownMenuBox(expanded = expandedA, onExpandedChange = { expandedA = it }) {
                OutlinedTextField(
                    value = memberA?.firstName ?: stringResource(R.string.select_member_1),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text(stringResource(R.string.member_1)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedA) }
                )
                ExposedDropdownMenu(expanded = expandedA, onDismissRequest = { expandedA = false }) {
                    members.forEach { m ->
                        DropdownMenuItem(
                            text = { Text("${m.firstName} ${m.lastName}") },
                            onClick = {
                                viewModel.selectMemberA(m)
                                expandedA = false
                            }
                        )
                    }
                }
            }

            ExposedDropdownMenuBox(expanded = expandedB, onExpandedChange = { expandedB = it }) {
                OutlinedTextField(
                    value = memberB?.firstName ?: stringResource(R.string.select_member_2),
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text(stringResource(R.string.member_2)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedB) }
                )
                ExposedDropdownMenu(expanded = expandedB, onDismissRequest = { expandedB = false }) {
                    members.forEach { m ->
                        DropdownMenuItem(
                            text = { Text("${m.firstName} ${m.lastName}") },
                            onClick = {
                                viewModel.selectMemberB(m)
                                expandedB = false
                            }
                        )
                    }
                }
            }
            Divider()
            
            if (memberA != null && memberB != null) {
                if (path != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${stringResource(R.string.relationship)}: $path", style = MaterialTheme.typography.headlineSmall)
                        }
                    }
                } else {
                    Text(stringResource(R.string.calculating_relationship), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
