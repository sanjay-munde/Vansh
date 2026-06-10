package com.vansh.familytree.ui.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vansh.familytree.R
import com.vansh.familytree.data.entity.Gender
import com.vansh.familytree.data.entity.Member

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberFormScreen(
    memberId: String?,
    onNavigateBack: () -> Unit,
    viewModel: MemberViewModel = hiltViewModel()
) {
    // In a full implementation, we'd fetch the existing member if memberId is not null
    // For now, we setup the basic form state
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var isLiving by remember { mutableStateOf(true) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (memberId == null) stringResource(R.string.add_member) else stringResource(R.string.edit)) },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("${stringResource(R.string.first_name)} *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("${stringResource(R.string.last_name)} *") },
                modifier = Modifier.fillMaxWidth()
            )

            Text(stringResource(R.string.gender), style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Gender.entries.forEach { g ->
                    FilterChip(
                        selected = gender == g,
                        onClick = { gender = g },
                        label = { Text(g.name) }
                    )
                }
            }

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = isLiving, onCheckedChange = { isLiving = it })
                Text(stringResource(R.string.is_living))
            }

            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank()) {
                        val newMember = Member(
                            firstName = firstName.trim(),
                            lastName = lastName.trim(),
                            gender = gender,
                            isLiving = isLiving
                        )
                        viewModel.saveMember(newMember)
                        onNavigateBack()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
