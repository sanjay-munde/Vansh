package com.vansh.familytree.ui.member

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.res.stringResource
import com.vansh.familytree.R
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberProfileScreen(
    memberId: String,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (String) -> Unit,
    viewModel: MemberProfileViewModel = hiltViewModel()
) {
    val member by viewModel.member.collectAsState()
    val mediaList by viewModel.media.collectAsState()
    val timelineEvents by viewModel.timeline.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.addProfilePhoto(it) } }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri -> uri?.let { viewModel.addDocument(it) } }
    )

    LaunchedEffect(memberId) {
        viewModel.loadMember(memberId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(memberId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
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
            member?.let { m ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val profilePhoto = mediaList.find { it.isProfilePhoto }
                    if (profilePhoto != null) {
                        AsyncImage(
                            model = profilePhoto.uri,
                            contentDescription = "Profile Photo",
                            modifier = Modifier.size(80.dp).clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(modifier = Modifier.size(80.dp).clip(CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(48.dp))
                        }
                    }
                    
                    Column {
                        val fullName = listOfNotNull(m.firstName, m.middleName, m.lastName).joinToString(" ")
                        Text(text = fullName, style = MaterialTheme.typography.headlineMedium)
                        Button(onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                            Text(stringResource(R.string.set_photo))
                        }
                    }
                }
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("${stringResource(R.string.gender)}: ${m.gender.name}")
                        Text("${stringResource(R.string.status)}: ${if (m.isLiving) stringResource(R.string.living) else stringResource(R.string.deceased)}")
                        // Add more details like DOB, POB, Bio here
                    }
                }
                
                Text(stringResource(R.string.media_documents), style = MaterialTheme.typography.titleLarge)
                if (mediaList.filter { !it.isProfilePhoto }.isEmpty()) {
                    Text(stringResource(R.string.no_media_attached), style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        mediaList.filter { !it.isProfilePhoto }.forEach { media ->
                            Text("- ${media.type.name}: ${stringResource(R.string.attached_file)}")
                        }
                    }
                }

                Button(onClick = { documentPickerLauncher.launch(arrayOf("*/*")) }) {
                    Text(stringResource(R.string.attach_file))
                }

                Text(stringResource(R.string.timeline), style = MaterialTheme.typography.titleLarge)
                if (timelineEvents.isEmpty()) {
                    Text(stringResource(R.string.no_events_found), style = MaterialTheme.typography.bodyMedium)
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        timelineEvents.forEach { event ->
                            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = event.year?.toString() ?: stringResource(R.string.unknown_date), style = MaterialTheme.typography.labelMedium)
                                    Text(text = event.title, style = MaterialTheme.typography.titleMedium)
                                    Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                    }
                }
            } ?: run {
                CircularProgressIndicator()
            }
        }
    }
}
