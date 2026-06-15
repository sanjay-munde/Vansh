package com.vansh.familytree.ui.member

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vansh.familytree.R
import com.vansh.familytree.data.entity.Gender
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.ui.components.DatePickerField
import kotlinx.coroutines.flow.firstOrNull

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberFormScreen(
    memberId: String?,
    onNavigateBack: () -> Unit,
    viewModel: MemberViewModel = hiltViewModel()
) {
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
    
    // Fix: Load the existing member details when editing!
    LaunchedEffect(memberId) {
        if (memberId != null) {
            val member = viewModel.getMemberById(memberId).firstOrNull()
            member?.let {
                firstName = it.firstName
                middleName = it.middleName ?: ""
                lastName = it.lastName
                nickname = it.nickname ?: ""
                gender = it.gender
                isLiving = it.isLiving
                dateOfBirth = it.dateOfBirth
                placeOfBirth = it.placeOfBirth ?: ""
                dateOfDeath = it.dateOfDeath
                biography = it.biography ?: ""
                cardColor = it.cardColor
            }
        }
    }

    val availableColors = listOf(
        Pair("Default", null),
        Pair("Soft Blue", "#E8F0FE"),
        Pair("Soft Pink", "#FCE8E6"),
        Pair("Soft Green", "#E6F4EA"),
        Pair("Soft Yellow", "#FEF7E0"),
        Pair("Soft Charcoal", "#F1F3F4")
    )
    
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
                title = { 
                    Text(
                        if (memberId == null) stringResource(R.string.add_member) else stringResource(R.string.edit),
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            
            // Section 1: Personal Information
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Personal Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("${stringResource(R.string.first_name)} *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = middleName,
                        onValueChange = { middleName = it },
                        label = { Text(stringResource(R.string.middle_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("${stringResource(R.string.last_name)} *") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("Nickname") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )

                    Text(stringResource(R.string.gender), style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Gender.entries.forEach { g ->
                            val isSelected = gender == g
                            val label = when(g) {
                                Gender.MALE -> stringResource(R.string.male)
                                Gender.FEMALE -> stringResource(R.string.female)
                                Gender.OTHER -> stringResource(R.string.other)
                            }
                            FilterChip(
                                selected = isSelected,
                                onClick = { gender = g },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }

                    // Card Color Picker
                    Text("Card Theme Highlight", style = MaterialTheme.typography.titleSmall)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        availableColors.forEach { (name, hex) ->
                            val color = hex?.let { Color(android.graphics.Color.parseColor(it)) } ?: MaterialTheme.colorScheme.surfaceVariant
                            val isSelected = cardColor == hex
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray.copy(alpha = 0.5f),
                                        shape = CircleShape
                                    )
                                    .clickable { cardColor = hex }
                            )
                        }
                    }
                }
            }

            // Section 2: Life Events
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Life Events",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = isLiving,
                            onCheckedChange = { isLiving = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Text(stringResource(R.string.is_living), style = MaterialTheme.typography.bodyLarge)
                    }

                    DatePickerField(
                        label = stringResource(R.string.birth_date),
                        selectedDateMillis = dateOfBirth,
                        onDateSelected = { dateOfBirth = it }
                    )
                    
                    OutlinedTextField(
                        value = placeOfBirth,
                        onValueChange = { placeOfBirth = it },
                        label = { Text(stringResource(R.string.place_of_birth)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                    
                    if (!isLiving) {
                        DatePickerField(
                            label = stringResource(R.string.death_date),
                            selectedDateMillis = dateOfDeath,
                            onDateSelected = { dateOfDeath = it }
                        )
                    }
                }
            }

            // Section 3: Biography & Memories
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Biography & Memories",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = biography,
                        onValueChange = { biography = it },
                        label = { Text("Biography, historical details, or memories") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 4,
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = firstName.isNotBlank() && lastName.isNotBlank()
            ) {
                Text(stringResource(R.string.save), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
