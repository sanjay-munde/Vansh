package com.vansh.familytree.ui.member

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.vansh.familytree.R
import coil.compose.AsyncImage
import com.vansh.familytree.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextOverflow

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
    val relationships by viewModel.relationships.collectAsState()
    val allMembers by viewModel.allMembers.collectAsState()
    val error by viewModel.error.collectAsState()
    
    var showRelationshipDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri -> uri?.let { viewModel.addProfilePhoto(it) } }
    )

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let { viewModel.addDocument(it) }
        }
    )

    var currentCameraUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = { success ->
            if (success) {
                currentCameraUri?.let { viewModel.addCapturedPhoto(it) }
            }
        }
    )

    LaunchedEffect(memberId) {
        viewModel.loadMember(memberId)
    }

    error?.let { errorMessage ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Invalid Relationship", fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold) },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile), style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToEdit(memberId) }) {
                        Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.edit))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
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
        ) {
            member?.let { m ->
                val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                
                val genderAccent = when(m.gender) {
                    com.vansh.familytree.data.entity.Gender.MALE -> MaleAccent
                    com.vansh.familytree.data.entity.Gender.FEMALE -> FemaleAccent
                    com.vansh.familytree.data.entity.Gender.OTHER -> OtherAccent
                }
                
                val headerGradient = Brush.horizontalGradient(
                    colors = listOf(
                        genderAccent.copy(alpha = 0.85f),
                        MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                )

                // 1. Profile Header Box with Gradient & Visual Depth
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .background(headerGradient)
                ) {
                    // Floating Avatar Card (overlaps the background partially)
                    val profilePhoto = mediaList.find { it.isProfilePhoto }
                    
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (profilePhoto != null) {
                                AsyncImage(
                                    model = profilePhoto.uri,
                                    contentDescription = "Profile Photo",
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .border(3.dp, Color.White, CircleShape)
                                        .shadow(4.dp, CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.9f))
                                        .border(3.dp, Color.White, CircleShape)
                                        .shadow(4.dp, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.Person, 
                                        contentDescription = null, 
                                        modifier = Modifier.size(44.dp),
                                        tint = genderAccent
                                    )
                                }
                            }
                            
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                val fullName = listOfNotNull(m.firstName, m.middleName, m.lastName).joinToString(" ")
                                Text(
                                    text = fullName, 
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontFamily = SerifFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                )
                                if (!m.nickname.isNullOrBlank()) {
                                    Text(
                                        text = "\"${m.nickname}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.White.copy(alpha = 0.85f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Photo actions row right beneath header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { photoPickerLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.set_photo), style = MaterialTheme.typography.labelMedium)
                    }
                    OutlinedButton(
                        onClick = {
                            val uri = viewModel.generateCameraUri()
                            currentCameraUri = uri
                            cameraLauncher.launch(uri)
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Take Photo", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    
                    // Card 1: Details & Demographics
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                "Profile Details", 
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            
                            val genderText = when(m.gender) {
                                com.vansh.familytree.data.entity.Gender.MALE -> stringResource(R.string.male)
                                com.vansh.familytree.data.entity.Gender.FEMALE -> stringResource(R.string.female)
                                com.vansh.familytree.data.entity.Gender.OTHER -> stringResource(R.string.other)
                            }
                            ProfileDetailRow(stringResource(R.string.gender), genderText)
                            
                            ProfileDetailRow(
                                stringResource(R.string.status), 
                                if (m.isLiving) stringResource(R.string.living) else stringResource(R.string.deceased),
                                textColor = if (m.isLiving) LivingAccent else DeceasedAccent
                            )
                            
                            if (m.dateOfBirth != null) {
                                ProfileDetailRow("Date of Birth", dateFormat.format(Date(m.dateOfBirth)))
                                
                                val endMillis = if (!m.isLiving && m.dateOfDeath != null) m.dateOfDeath else System.currentTimeMillis()
                                val years = (endMillis - m.dateOfBirth) / (1000L * 60 * 60 * 24 * 365.25)
                                ProfileDetailRow("Calculated Age", "${years.toInt()} ${stringResource(R.string.years)}")
                            }
                            if (!m.placeOfBirth.isNullOrBlank()) {
                                ProfileDetailRow(stringResource(R.string.place_of_birth), m.placeOfBirth)
                            }
                            if (m.dateOfDeath != null && !m.isLiving) {
                                ProfileDetailRow("Date of Death", dateFormat.format(Date(m.dateOfDeath)))
                            }
                            
                            if (!m.biography.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Biography & Notes", style = MaterialTheme.typography.titleSmall.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold))
                                Text(
                                    m.biography, 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    // Card 2: Photo Gallery & Documents
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    stringResource(R.string.media_documents), 
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Button(
                                    onClick = { documentPickerLauncher.launch(arrayOf("*/*")) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(stringResource(R.string.attach_file), style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            
                            val otherMedia = mediaList.filter { !it.isProfilePhoto }
                            if (otherMedia.isEmpty()) {
                                Text(
                                    stringResource(R.string.no_media_attached), 
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            } else {
                                val photos = otherMedia.filter { it.type.name == "PHOTO" || it.type.name == "IMAGE" }
                                val documents = otherMedia.filter { it.type.name != "PHOTO" && it.type.name != "IMAGE" }
                                
                                if (photos.isNotEmpty()) {
                                    Text("Photo Collection", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    androidx.compose.foundation.lazy.grid.LazyVerticalGrid(
                                        columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3),
                                        modifier = Modifier
                                            .height(115.dp)
                                            .fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        items(photos.size) { index ->
                                            AsyncImage(
                                                model = photos[index].uri,
                                                contentDescription = "Gallery Photo",
                                                modifier = Modifier
                                                    .aspectRatio(1f)
                                                    .clip(RoundedCornerShape(10.dp))
                                                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                                if (documents.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Archived Documents", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        documents.forEach { media ->
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                                                shape = RoundedCornerShape(10.dp),
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(12.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        "📄  ${media.type.name}: ${stringResource(R.string.attached_file)}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 3: Visual Timeline
                    Text(
                        stringResource(R.string.timeline), 
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    
                    if (timelineEvents.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Text(
                                stringResource(R.string.no_events_found), 
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.Top
                        ) {
                            timelineEvents.forEachIndexed { index, event ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(IntrinsicSize.Min)
                                ) {
                                    // Year column
                                    Box(
                                        modifier = Modifier
                                            .width(64.dp)
                                            .padding(top = 2.dp),
                                        contentAlignment = Alignment.TopEnd
                                    ) {
                                        Text(
                                            text = event.year?.toString() ?: "—",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.secondary,
                                            fontFamily = SerifFontFamily
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(14.dp))
                                    
                                    // Vertical line column
                                    Column(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .width(16.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .border(2.dp, Color.White, CircleShape)
                                        )
                                        
                                        if (index < timelineEvents.size - 1) {
                                            Box(
                                                modifier = Modifier
                                                    .width(2.dp)
                                                    .weight(1f)
                                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(14.dp))
                                    
                                    // Details Card
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(bottom = 18.dp),
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    ) {
                                        Column(modifier = Modifier.padding(14.dp)) {
                                            Text(
                                                text = event.title, 
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = event.description, 
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray,
                                                lineHeight = 18.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Section 4: Relationships
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Relationships", 
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { showRelationshipDialog = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = "Add Relationship", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    
                    if (relationships.isEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        ) {
                            Text(
                                "No relationships added yet.", 
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            relationships.forEach { rel ->
                                val relatedMember = allMembers.find { it.id == rel.targetId }
                                if (relatedMember != null) {
                                    val relAccent = when (relatedMember.gender) {
                                        com.vansh.familytree.data.entity.Gender.MALE -> MaleAccent
                                        com.vansh.familytree.data.entity.Gender.FEMALE -> FemaleAccent
                                        com.vansh.familytree.data.entity.Gender.OTHER -> OtherAccent
                                    }
                                    
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                        shape = RoundedCornerShape(14.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(IntrinsicSize.Min),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .width(5.dp)
                                                    .fillMaxHeight()
                                                    .background(relAccent)
                                            )
                                            
                                            Column(modifier = Modifier.padding(14.dp).weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    modifier = Modifier.fillMaxWidth()
                                                ) {
                                                    Text(
                                                        text = "${relatedMember.firstName} ${relatedMember.lastName}", 
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                                    )
                                                    Box(
                                                        modifier = Modifier
                                                            .clip(RoundedCornerShape(6.dp))
                                                            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                                    ) {
                                                        Text(
                                                            text = rel.type.name,
                                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                            color = MaterialTheme.colorScheme.secondary
                                                        )
                                                    }
                                                }
                                                rel.subtype?.let { 
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Subtype: ${it.name}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) 
                                                }
                                                if (rel.startDate != null) {
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text("Since: ${dateFormat.format(Date(rel.startDate))}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    if (showRelationshipDialog) {
                        RelationshipFormDialog(
                            currentMemberId = memberId,
                            allMembers = allMembers,
                            onDismiss = { showRelationshipDialog = false },
                            onSave = { targetId, type, subtype, startDate, endDate, location ->
                                viewModel.addRelationship(targetId, type, subtype, startDate, endDate, location)
                            }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ProfileDetailRow(
    label: String, 
    value: String, 
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label, 
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Text(
            text = value, 
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = textColor
        )
    }
}
