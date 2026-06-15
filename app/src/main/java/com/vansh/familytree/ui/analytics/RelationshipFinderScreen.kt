package com.vansh.familytree.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import com.vansh.familytree.R
import androidx.lifecycle.viewModelScope
import com.vansh.familytree.data.entity.Member
import com.vansh.familytree.data.entity.Relationship
import com.vansh.familytree.data.entity.RelationshipType
import com.vansh.familytree.data.entity.Gender
import com.vansh.familytree.data.repository.FamilyTreeRepository
import com.vansh.familytree.domain.kinship.KinshipCalculator
import com.vansh.familytree.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.ui.text.style.TextOverflow

@HiltViewModel
class RelationshipFinderViewModel @Inject constructor(
    private val repository: FamilyTreeRepository,
    private val kinshipCalculator: KinshipCalculator
) : ViewModel() {
    private val _members = MutableStateFlow<List<Member>>(emptyList())
    val members: StateFlow<List<Member>> = _members
    
    private val _selectedMemberA = MutableStateFlow<Member?>(null)
    val selectedMemberA: StateFlow<Member?> = _selectedMemberA
    
    private val _selectedMemberB = MutableStateFlow<Member?>(null)
    val selectedMemberB: StateFlow<Member?> = _selectedMemberB
    
    private val _kinshipPath = MutableStateFlow<String?>(null)
    val kinshipPath: StateFlow<String?> = _kinshipPath

    private val _visualPath = MutableStateFlow<List<Member>>(emptyList())
    val visualPath: StateFlow<List<Member>> = _visualPath

    private val _allRelationships = MutableStateFlow<List<Relationship>>(emptyList())
    val allRelationships: StateFlow<List<Relationship>> = _allRelationships

    private val _profilePhotos = MutableStateFlow<Map<String, String>>(emptyMap())
    val profilePhotos: StateFlow<Map<String, String>> = _profilePhotos

    init {
        viewModelScope.launch {
            repository.getAllMembers().collect {
                _members.value = it
            }
        }
        viewModelScope.launch {
            repository.getAllRelationships().collect {
                _allRelationships.value = it
            }
        }
        viewModelScope.launch {
            repository.getAllProfilePhotos().collect { photos ->
                _profilePhotos.value = photos.associate { it.memberId to it.uri }
            }
        }
    }
    
    fun selectMemberA(member: Member?) {
        _selectedMemberA.value = member
        calculate()
    }
    
    fun selectMemberB(member: Member?) {
        _selectedMemberB.value = member
        calculate()
    }
    
    private fun calculate() {
        val a = _selectedMemberA.value
        val b = _selectedMemberB.value
        if (a != null && b != null) {
            viewModelScope.launch {
                _kinshipPath.value = kinshipCalculator.calculateKinship(a.id, b.id)
                
                // Perform BFS to find shortest path of member nodes
                val edges = repository.getAllRelationships().firstOrNull() ?: emptyList()
                val queue = ArrayDeque<Pair<String, List<String>>>()
                val visited = mutableSetOf<String>()
                
                queue.add(Pair(a.id, listOf(a.id)))
                var foundPath: List<String>? = null
                
                while (queue.isNotEmpty()) {
                    val (currentId, currentPath) = queue.removeFirst()
                    if (currentId == b.id) {
                        foundPath = currentPath
                        break
                    }
                    visited.add(currentId)
                    
                    val neighbors = edges.filter { it.subjectId == currentId || it.targetId == currentId }
                        .map { if (it.subjectId == currentId) it.targetId else it.subjectId }
                    
                    for (neighbor in neighbors) {
                        if (!visited.contains(neighbor) && !currentPath.contains(neighbor)) {
                            queue.add(Pair(neighbor, currentPath + neighbor))
                        }
                    }
                }
                
                val membersList = _members.value
                _visualPath.value = foundPath?.mapNotNull { id -> membersList.find { it.id == id } } ?: emptyList()
            }
        } else {
            _kinshipPath.value = null
            _visualPath.value = emptyList()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelationshipFinderScreen(
    onNavigateBack: () -> Unit,
    viewModel: RelationshipFinderViewModel = hiltViewModel()
) {
    val members by viewModel.members.collectAsState()
    val memberA by viewModel.selectedMemberA.collectAsState()
    val memberB by viewModel.selectedMemberB.collectAsState()
    val path by viewModel.kinshipPath.collectAsState()
    val visualPath by viewModel.visualPath.collectAsState()
    val allRelationships by viewModel.allRelationships.collectAsState()
    val profilePhotos by viewModel.profilePhotos.collectAsState()
    
    var expandedA by remember { mutableStateOf(false) }
    var expandedB by remember { mutableStateOf(false) }

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.relationship_finder), style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold)) },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Selector Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Select Family Members",
                        style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    // Dropdown Member A
                    ExposedDropdownMenuBox(expanded = expandedA, onExpandedChange = { expandedA = it }) {
                        OutlinedTextField(
                            value = memberA?.let { "${it.firstName} ${it.lastName}" } ?: stringResource(R.string.select_member_1),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text(stringResource(R.string.member_1)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedA) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedA, onDismissRequest = { expandedA = false }) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.firstName} ${m.lastName}") },
                                    onClick = {
                                        viewModel.selectMemberA(m)
                                        expandedA = false
                                    }
                                )
                            }
                        }
                    }

                    // Dropdown Member B
                    ExposedDropdownMenuBox(expanded = expandedB, onExpandedChange = { expandedB = it }) {
                        OutlinedTextField(
                            value = memberB?.let { "${it.firstName} ${it.lastName}" } ?: stringResource(R.string.select_member_2),
                            onValueChange = {},
                            readOnly = true,
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth(),
                            label = { Text(stringResource(R.string.member_2)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedB) },
                            shape = RoundedCornerShape(10.dp)
                        )
                        ExposedDropdownMenu(expanded = expandedB, onDismissRequest = { expandedB = false }) {
                            members.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.firstName} ${m.lastName}") },
                                    onClick = {
                                        viewModel.selectMemberB(m)
                                        expandedB = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
            
            // Result Cards
            if (memberA != null && memberB != null) {
                if (path != null) {
                    // Kinship Text Description Card (Terracotta/Jade gradient)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(primaryGradient)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Calculated Relationship",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontFamily = SerifFontFamily
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = path ?: "Unrelated",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // Pedigree Path Visualization Card
                    if (visualPath.isNotEmpty()) {
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
                                    text = "Pedigree Path Visualization",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                                
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    visualPath.forEachIndexed { index, m ->
                                        // Render Member Node
                                        PedigreeNode(member = m, profilePhotoUri = profilePhotos[m.id])

                                        // Render Link Arrow if not last
                                        if (index < visualPath.size - 1) {
                                            val current = m
                                            val next = visualPath[index + 1]
                                            val relationLabel = getVisualRelationLabel(current, next, allRelationships)
                                            
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center,
                                                modifier = Modifier.padding(horizontal = 4.dp)
                                            ) {
                                                Text(
                                                    text = relationLabel, 
                                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold),
                                                    color = MaterialTheme.colorScheme.secondary
                                                )
                                                Icon(
                                                    Icons.Filled.KeyboardArrowRight, 
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}

@Composable
fun PedigreeNode(member: Member, profilePhotoUri: String?) {
    val initials = if (member.firstName.isNotEmpty()) member.firstName.take(1).uppercase() else "?"
    
    val accentColor = when(member.gender) {
        Gender.MALE -> MaleAccent
        Gender.FEMALE -> FemaleAccent
        Gender.OTHER -> OtherAccent
    }
    
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        modifier = Modifier.width(130.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f))
                    .border(1.5.dp, accentColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (profilePhotoUri != null) {
                    AsyncImage(
                        model = profilePhotoUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = initials, 
                        style = MaterialTheme.typography.titleSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        fontFamily = SerifFontFamily
                    )
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${member.firstName} ${member.lastName.take(1)}.",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, fontFamily = SerifFontFamily),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private fun getVisualRelationLabel(current: Member, next: Member, edges: List<Relationship>): String {
    val edge = edges.find { 
        (it.subjectId == current.id && it.targetId == next.id) || 
        (it.subjectId == next.id && it.targetId == current.id) 
    } ?: return "Related"

    return when (edge.type) {
        RelationshipType.SPOUSE -> "Spouse"
        RelationshipType.PARENT -> {
            if (edge.subjectId == current.id) "Parent Of" else "Child Of"
        }
        RelationshipType.CHILD -> {
            if (edge.subjectId == current.id) "Child Of" else "Parent Of"
        }
    }
}
