package com.vansh.familytree.ui.member

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.RelationshipType
import com.vansh.familytree.data.entity.RelationshipSubtype
import com.vansh.familytree.ui.components.DatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipFormDialog(
    currentMemberId: String,
    allMembers: List<Member>,
    onDismiss: () -> Unit,
    onSave: (targetId: String, type: RelationshipType, subtype: RelationshipSubtype?, startDate: Long?, endDate: Long?, location: String?) -> Unit
) {
    var selectedTargetMember by remember { mutableStateOf<Member?>(null) }
    var selectedType by remember { mutableStateOf(RelationshipType.SPOUSE) }
    var selectedSubtype by remember { mutableStateOf<RelationshipSubtype?>(null) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    var location by remember { mutableStateOf("") }

    var memberDropdownExpanded by remember { mutableStateOf(false) }

    val availableMembers = allMembers.filter { it.id != currentMemberId }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Add Relationship", style = MaterialTheme.typography.titleLarge)

                ExposedDropdownMenuBox(
                    expanded = memberDropdownExpanded,
                    onExpandedChange = { memberDropdownExpanded = !memberDropdownExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedTargetMember?.let { "${it.firstName} ${it.lastName}" } ?: "Select Member",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Related Member") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = memberDropdownExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = memberDropdownExpanded,
                        onDismissRequest = { memberDropdownExpanded = false }
                    ) {
                        availableMembers.forEach { member ->
                            DropdownMenuItem(
                                text = { Text("${member.firstName} ${member.lastName}") },
                                onClick = {
                                    selectedTargetMember = member
                                    memberDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Text("Relationship Type", style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RelationshipType.entries.forEach { type ->
                        FilterChip(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            label = { Text(type.name) }
                        )
                    }
                }

                Text("Subtype", style = MaterialTheme.typography.titleSmall)
                // Filter subtypes based on type
                val applicableSubtypes = when (selectedType) {
                    RelationshipType.SPOUSE -> listOf(RelationshipSubtype.MARRIED, RelationshipSubtype.DIVORCED, RelationshipSubtype.SEPARATED, null)
                    RelationshipType.PARENT, RelationshipType.CHILD -> listOf(RelationshipSubtype.BIOLOGICAL, RelationshipSubtype.ADOPTED, RelationshipSubtype.STEP, null)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    applicableSubtypes.forEach { subtype ->
                        FilterChip(
                            selected = selectedSubtype == subtype,
                            onClick = { selectedSubtype = subtype },
                            label = { Text(subtype?.name ?: "NONE") }
                        )
                    }
                }

                if (selectedType == RelationshipType.SPOUSE) {
                    DatePickerField(
                        label = "Marriage Date",
                        selectedDateMillis = startDate,
                        onDateSelected = { startDate = it }
                    )
                    if (selectedSubtype == RelationshipSubtype.DIVORCED) {
                        DatePickerField(
                            label = "Divorce Date",
                            selectedDateMillis = endDate,
                            onDateSelected = { endDate = it }
                        )
                    }
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Marriage Location") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            selectedTargetMember?.let { target ->
                                onSave(
                                    target.id,
                                    selectedType,
                                    selectedSubtype,
                                    startDate,
                                    endDate,
                                    location.takeIf { it.isNotBlank() }
                                )
                                onDismiss()
                            }
                        },
                        enabled = selectedTargetMember != null
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}
