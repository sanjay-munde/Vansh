package com.vansh.familytree.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vansh.familytree.data.local.BackupManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val backupManager: BackupManager
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    fun exportBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            val success = backupManager.exportBackup(uri)
            _message.value = if (success) "Backup exported successfully!" else "Failed to export backup."
        }
    }

    fun importBackup(uri: android.net.Uri) {
        viewModelScope.launch {
            val success = backupManager.importBackup(uri)
            _message.value = if (success) "Backup imported successfully!" else "Failed to import backup."
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val message by viewModel.message.collectAsState()

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
        onResult = { uri ->
            uri?.let { viewModel.exportBackup(it) }
        }
    )

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.importBackup(it) }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings & Backup") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
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
            message?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(it, modifier = Modifier.padding(16.dp))
                }
                LaunchedEffect(it) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessage()
                }
            }

            Text("Data Backup", style = MaterialTheme.typography.titleLarge)
            Text("Export your family tree data to a JSON file to keep it safe, or import an existing backup.", style = MaterialTheme.typography.bodyMedium)

            Button(
                onClick = { exportLauncher.launch("family_tree_backup.json") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Backup (JSON)")
            }

            Button(
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Import Backup (JSON)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Language", style = MaterialTheme.typography.titleLarge)
            Text("Switch between English and Marathi.", style = MaterialTheme.typography.bodyMedium)

            val currentLang = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            var expanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = if (currentLang == "mr") "मराठी (Marathi)" else "English",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    label = { Text("App Language") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("English") },
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("en"))
                            expanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("मराठी (Marathi)") },
                        onClick = {
                            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("mr"))
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
