package com.vansh.familytree.ui.member

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.vansh.familytree.R
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Gender

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberListScreen(
    onNavigateToMemberForm: (String?) -> Unit,
    onNavigateToMemberProfile: (String) -> Unit,
    onNavigateToRelationshipFinder: () -> Unit,
    viewModel: MemberViewModel = hiltViewModel()
) {
    val members by viewModel.members.collectAsState()
    val filterCriteria by viewModel.filterCriteria.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_members)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                actions = {
                    var expandedMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expandedMenu = true }) {
                            Text("A/अ", style = MaterialTheme.typography.titleMedium)
                        }
                        DropdownMenu(expanded = expandedMenu, onDismissRequest = { expandedMenu = false }) {
                            DropdownMenuItem(
                                text = { Text("English") },
                                onClick = {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                                    expandedMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("मराठी (Marathi)") },
                                onClick = {
                                    AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("mr"))
                                    expandedMenu = false
                                }
                            )
                        }
                    }
                    IconButton(onClick = onNavigateToRelationshipFinder) {
                        Icon(Icons.Filled.Person, contentDescription = stringResource(R.string.relationship_finder))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onNavigateToMemberForm(null) }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_member))
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = filterCriteria.query,
                    onValueChange = { viewModel.updateFilterCriteria(filterCriteria.copy(query = it)) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_by_name)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true
                )
                
                Button(onClick = { showFilterSheet = true }, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.filters))
                }
            }

            if (members.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text(stringResource(R.string.no_family_members_found), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(members, key = { it.id }) { member ->
                        MemberCard(member = member, onClick = { onNavigateToMemberProfile(member.id) })
                    }
                }
            }
        }
        
        if (showFilterSheet) {
            ModalBottomSheet(onDismissRequest = { showFilterSheet = false }) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.filters), style = MaterialTheme.typography.titleLarge)
                    
                    Text(stringResource(R.string.status))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = filterCriteria.isLiving == null, onClick = { viewModel.updateFilterCriteria(filterCriteria.copy(isLiving = null)) }, label = { Text(stringResource(R.string.all)) })
                        FilterChip(selected = filterCriteria.isLiving == true, onClick = { viewModel.updateFilterCriteria(filterCriteria.copy(isLiving = true)) }, label = { Text(stringResource(R.string.living)) })
                        FilterChip(selected = filterCriteria.isLiving == false, onClick = { viewModel.updateFilterCriteria(filterCriteria.copy(isLiving = false)) }, label = { Text(stringResource(R.string.deceased)) })
                    }

                    Text(stringResource(R.string.gender))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = filterCriteria.gender == null, onClick = { viewModel.updateFilterCriteria(filterCriteria.copy(gender = null)) }, label = { Text(stringResource(R.string.all)) })
                        FilterChip(selected = filterCriteria.gender == Gender.MALE, onClick = { viewModel.updateFilterCriteria(filterCriteria.copy(gender = Gender.MALE)) }, label = { Text(stringResource(R.string.male)) })
                        FilterChip(selected = filterCriteria.gender == Gender.FEMALE, onClick = { viewModel.updateFilterCriteria(filterCriteria.copy(gender = Gender.FEMALE)) }, label = { Text(stringResource(R.string.female)) })
                        FilterChip(selected = filterCriteria.gender == Gender.OTHER, onClick = { viewModel.updateFilterCriteria(filterCriteria.copy(gender = Gender.OTHER)) }, label = { Text(stringResource(R.string.other)) })
                    }
                    
                    OutlinedTextField(
                        value = filterCriteria.placeOfBirth ?: "",
                        onValueChange = { viewModel.updateFilterCriteria(filterCriteria.copy(placeOfBirth = it.ifBlank { null })) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.place_of_birth)) },
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun MemberCard(member: Member, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val fullName = listOfNotNull(member.firstName, member.middleName, member.lastName).joinToString(" ")
            Text(text = fullName, style = MaterialTheme.typography.titleMedium)
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = if (member.isLiving) stringResource(R.string.living) else stringResource(R.string.deceased), 
                style = MaterialTheme.typography.bodySmall,
                color = if (member.isLiving) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}
