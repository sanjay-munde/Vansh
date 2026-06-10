package com.vansh.familytree.ui.analytics

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vansh.familytree.R
import com.vansh.familytree.data.entity.Gender
import com.vansh.familytree.data.entity.RelationshipType
import com.vansh.familytree.data.repository.FamilyTreeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class AnalyticsState(
    val totalMembers: Int = 0,
    val livingMembers: Int = 0,
    val deceasedMembers: Int = 0,
    val maleMembers: Int = 0,
    val femaleMembers: Int = 0,
    val averageAge: Double = 0.0,
    val maxGenerationDepth: Int = 0
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: FamilyTreeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AnalyticsState())
    val state: StateFlow<AnalyticsState> = _state

    init {
        viewModelScope.launch {
            combine(
                repository.getAllMembers(),
                repository.getAllRelationships()
            ) { members, edges ->
                val total = members.size
                val living = members.count { it.isLiving }
                val deceased = total - living
                val males = members.count { it.gender == Gender.MALE }
                val females = members.count { it.gender == Gender.FEMALE }
                
                var totalAge = 0
                var ageCount = 0
                val currentYear = LocalDate.now().year
                members.forEach { m ->
                    if (m.dateOfBirth != null) {
                        // dateOfBirth is a timestamp in ms. Calculate approx year by converting to days
                        val birthYear = java.time.Instant.ofEpochMilli(m.dateOfBirth).atZone(java.time.ZoneId.systemDefault()).year
                        val deathYear = if (!m.isLiving && m.dateOfDeath != null) {
                            java.time.Instant.ofEpochMilli(m.dateOfDeath).atZone(java.time.ZoneId.systemDefault()).year
                        } else {
                            currentYear
                        }
                        totalAge += (deathYear - birthYear)
                        ageCount++
                    }
                }
                val avgAge = if (ageCount > 0) totalAge.toDouble() / ageCount else 0.0

                val childEdges = edges.filter { it.type == RelationshipType.PARENT }
                val parentToChildren = mutableMapOf<String, MutableList<String>>()
                childEdges.forEach { edge ->
                    parentToChildren.getOrPut(edge.subjectId) { mutableListOf() }.add(edge.targetId)
                }
                
                val allChildren = childEdges.map { it.targetId }.toSet()
                val roots = members.map { it.id }.filter { !allChildren.contains(it) }
                
                var maxDepth = 0
                fun dfs(nodeId: String, depth: Int) {
                    if (depth > maxDepth) maxDepth = depth
                    val children = parentToChildren[nodeId] ?: emptyList()
                    for (child in children) {
                        dfs(child, depth + 1)
                    }
                }
                
                for (root in roots) {
                    dfs(root, 1)
                }

                AnalyticsState(
                    totalMembers = total,
                    livingMembers = living,
                    deceasedMembers = deceased,
                    maleMembers = males,
                    femaleMembers = females,
                    averageAge = avgAge,
                    maxGenerationDepth = maxDepth
                )
            }.collect {
                _state.value = it
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_analytics)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.demographics), style = MaterialTheme.typography.titleMedium)
                    Divider()
                    Text("${stringResource(R.string.total_members)}: ${state.totalMembers}")
                    Text("${stringResource(R.string.living)}: ${state.livingMembers}", color = MaterialTheme.colorScheme.primary)
                    Text("${stringResource(R.string.deceased)}: ${state.deceasedMembers}", color = MaterialTheme.colorScheme.error)
                    Text("${stringResource(R.string.male)}: ${state.maleMembers}")
                    Text("${stringResource(R.string.female)}: ${state.femaleMembers}")
                }
            }
            
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.insights), style = MaterialTheme.typography.titleMedium)
                    Divider()
                    Text("${stringResource(R.string.average_age)}: ${String.format("%.1f", state.averageAge)} ${stringResource(R.string.years)}")
                    Text("${stringResource(R.string.max_generation_depth)}: ${state.maxGenerationDepth} ${stringResource(R.string.generations)}")
                }
            }
        }
    }
}
