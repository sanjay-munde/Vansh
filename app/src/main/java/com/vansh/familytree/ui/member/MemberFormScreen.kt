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
import com.vansh.familytree.ui.components.DatePickerField

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
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(Gender.MALE) }
    var isLiving by remember { mutableStateOf(true) }
    var dateOfBirth by remember { mutableStateOf<Long?>(null) }
    var placeOfBirth by remember { mutableStateOf("") }
    var dateOfDeath by remember { mutableStateOf<Long?>(null) }
    var biography by remember { mutableStateOf("") }
    var cardColor by remember { mutableStateOf<String?>(null) }

    val duplicateWarning by viewModel.duplicateWarning.collectAsState()
    
    duplicateWarning?.let { warning ->
        AlertDialog(
            onDismissRequest = { viewModel.clearDuplicateWarning() },
            title = { Text("Potential Duplicate") },
            text = { Text(warning) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearDuplicateWarning()
                    val newMember = Member(
                        id = memberId ?: java.util.UUID.randomUUID().toString(),
                        firstName = firstName.trim(),
                        middleName = middleName.trim().ifEmpty { null },
                        lastName = lastName.trim(),
                        nickname = nickname.trim().ifEmpty { null },
                        gender = gender,
                        isLiving = isLiving,
                        dateOfBirth = dateOfBirth,
                        placeOfBirth = placeOfBirth.trim().ifEmpty { null },
                        dateOfDeath = if (!isLiving) dateOfDeath else null,
                        biography = biography.trim().ifEmpty { null },
                        cardColor = cardColor
                    )
                    viewModel.saveMemberWithValidation(newMember, ignoreWarning = true) {
                        onNavigateBack()
                    }
                }) {
                    Text("Save Anyway")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clearDuplicateWarning() }) {
                    Text("Cancel")
                }
            }
        )
    }

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
                value = middleName,
                onValueChange = { middleName = it },
                label = { Text("Middle Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("${stringResource(R.string.last_name)} *") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = nickname,
                onValueChange = { nickname = it },
                label = { Text("Nickname") },
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
            
            DatePickerField(
                label = "Date of Birth",
                selectedDateMillis = dateOfBirth,
                onDateSelected = { dateOfBirth = it }
            )
            
            OutlinedTextField(
                value = placeOfBirth,
                onValueChange = { placeOfBirth = it },
                label = { Text("Place of Birth") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Checkbox(checked = isLiving, onCheckedChange = { isLiving = it })
                Text(stringResource(R.string.is_living))
            }
            
            if (!isLiving) {
                DatePickerField(
                    label = "Date of Death",
                    selectedDateMillis = dateOfDeath,
                    onDateSelected = { dateOfDeath = it }
                )
            }
            
            OutlinedTextField(
                value = biography,
                onValueChange = { biography = it },
                label = { Text("Biography / Notes") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Button(
                onClick = {
                    if (firstName.isNotBlank() && lastName.isNotBlank()) {
                        val newMember = Member(
                            id = memberId ?: java.util.UUID.randomUUID().toString(),
                            firstName = firstName.trim(),
                            middleName = middleName.trim().ifEmpty { null },
                            lastName = lastName.trim(),
                            nickname = nickname.trim().ifEmpty { null },
                            gender = gender,
                            isLiving = isLiving,
                            dateOfBirth = dateOfBirth,
                            placeOfBirth = placeOfBirth.trim().ifEmpty { null },
                            dateOfDeath = if (!isLiving) dateOfDeath else null,
                            biography = biography.trim().ifEmpty { null },
                            cardColor = cardColor
                        )
                        viewModel.saveMemberWithValidation(newMember) {
                            onNavigateBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.save))
            }
        }
    }
}
