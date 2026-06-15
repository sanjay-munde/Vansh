package com.vansh.familytree.ui.member

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.vansh.familytree.R
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Gender
import com.vansh.familytree.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemberListScreen(
    onNavigateToMemberForm: (String?) -> Unit,
    onNavigateToMemberProfile: (String) -> Unit,
    onNavigateToRelationshipFinder: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: MemberViewModel = hiltViewModel()
) {
    val members by viewModel.members.collectAsState()
    val filterCriteria by viewModel.filterCriteria.collectAsState()
    val profilePhotos by viewModel.profilePhotos.collectAsState()
    
    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.family_members), 
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = SerifFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    var expandedMenu by remember { mutableStateOf(false) }
                    Box {
                        IconButton(onClick = { expandedMenu = true }) {
                            Text(
                                "A/अ", 
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
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
                        Icon(
                            Icons.Filled.Person, 
                            contentDescription = stringResource(R.string.relationship_finder)
                        )
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Filled.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToMemberForm(null) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_member))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            
            // Search and Filters Bar (Glassmorphic look)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = filterCriteria.query,
                    onValueChange = { viewModel.updateFilterCriteria(filterCriteria.copy(query = it)) },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text(stringResource(R.string.search_by_name)) },
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
                
                Button(
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                ) {
                    Text(stringResource(R.string.filters), style = MaterialTheme.typography.labelLarge)
                }
            }

            if (members.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "📍", 
                            style = MaterialTheme.typography.displayMedium
                        )
                        Text(
                            stringResource(R.string.no_family_members_found), 
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray,
                            fontFamily = SerifFontFamily
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(members, key = { it.id }) { member ->
                        MemberCard(
                            member = member,
                            profilePhotoUri = profilePhotos[member.id],
                            onClick = { onNavigateToMemberProfile(member.id) }
                        )
                    }
                }
            }
        }
        
        if (showFilterSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFilterSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Text(
                        stringResource(R.string.filters), 
                        style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

                    Text(stringResource(R.string.status), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Pair(stringResource(R.string.all), null),
                            Pair(stringResource(R.string.living), true),
                            Pair(stringResource(R.string.deceased), false)
                        ).forEach { (label, isLivingVal) ->
                            val isSelected = filterCriteria.isLiving == isLivingVal
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateFilterCriteria(filterCriteria.copy(isLiving = isLivingVal)) },
                                label = { Text(label) }
                            )
                        }
                    }

                    Text(stringResource(R.string.gender), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            Pair(stringResource(R.string.all), null),
                            Pair(stringResource(R.string.male), Gender.MALE),
                            Pair(stringResource(R.string.female), Gender.FEMALE),
                            Pair(stringResource(R.string.other), Gender.OTHER)
                        ).forEach { (label, genderVal) ->
                            val isSelected = filterCriteria.gender == genderVal
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.updateFilterCriteria(filterCriteria.copy(gender = genderVal)) },
                                label = { Text(label) }
                            )
                        }
                    }
                    
                    OutlinedTextField(
                        value = filterCriteria.placeOfBirth ?: "",
                        onValueChange = { viewModel.updateFilterCriteria(filterCriteria.copy(placeOfBirth = it.ifBlank { null })) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.place_of_birth)) },
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun MemberCard(member: Member, profilePhotoUri: String?, onClick: () -> Unit) {
    val parsedColor = member.cardColor?.let { hex ->
        try {
            Color(android.graphics.Color.parseColor(hex))
        } catch (e: Exception) {
            null
        }
    } ?: MaterialTheme.colorScheme.surface

    val accentColor = when (member.gender) {
        Gender.MALE -> MaleAccent
        Gender.FEMALE -> FemaleAccent
        Gender.OTHER -> OtherAccent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = parsedColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Gender Accent Stripe
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(accentColor)
            )
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // Avatar / Profile photo
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(2.dp, MaterialTheme.colorScheme.background, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUri != null) {
                    AsyncImage(
                        model = profilePhotoUri,
                        contentDescription = "Profile Photo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    val initials = if (member.firstName.isNotEmpty()) member.firstName.take(1).uppercase() else "?"
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = accentColor,
                        fontFamily = SerifFontFamily
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Details Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp)
            ) {
                val fullName = listOfNotNull(member.firstName, member.middleName, member.lastName).joinToString(" ")
                Text(
                    text = fullName, 
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = SerifFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                val subtitleText = buildString {
                    if (member.dateOfBirth != null) {
                        val birthYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(Date(member.dateOfBirth))
                        append("Born: $birthYear")
                        
                        val endMillis = if (!member.isLiving && member.dateOfDeath != null) member.dateOfDeath else System.currentTimeMillis()
                        val years = (endMillis - member.dateOfBirth) / (1000L * 60 * 60 * 24 * 365.25)
                        append(" (${years.toInt()} yrs)")
                    } else {
                        append("Status Unknown")
                    }
                    if (!member.placeOfBirth.isNullOrBlank()) {
                        append(" • ${member.placeOfBirth}")
                    }
                }
                
                Text(
                    text = subtitleText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Status Badge
            Box(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (member.isLiving) LivingAccent.copy(alpha = 0.15f) 
                        else DeceasedAccent.copy(alpha = 0.15f)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (member.isLiving) stringResource(R.string.living) else stringResource(R.string.deceased),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (member.isLiving) LivingAccent else DeceasedAccent,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
