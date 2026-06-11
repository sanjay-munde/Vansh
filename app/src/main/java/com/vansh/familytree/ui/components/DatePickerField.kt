package com.vansh.familytree.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    label: String,
    selectedDateMillis: Long?,
    onDateSelected: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDateMillis
    )

    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val displayDate = remember(selectedDateMillis) {
        if (selectedDateMillis != null) {
            dateFormat.format(Date(selectedDateMillis))
        } else {
            ""
        }
    }

    OutlinedTextField(
        value = displayDate,
        onValueChange = { },
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            Icon(Icons.Default.DateRange, contentDescription = "Select Date")
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDialog = true },
        enabled = false,
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = MaterialTheme.colorScheme.onSurface,
            disabledBorderColor = MaterialTheme.colorScheme.outline,
            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    )

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    onDateSelected(datePickerState.selectedDateMillis)
                    showDialog = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
