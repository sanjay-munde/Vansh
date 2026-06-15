package com.vansh.familytree.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.vansh.familytree.R
import com.vansh.familytree.ui.theme.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    val primaryGradient = Brush.horizontalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.family_analytics), style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold)) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Hero Total Members Card (Gradient fill)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(primaryGradient)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.total_members),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            fontFamily = SerifFontFamily
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = state.totalMembers.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 64.sp),
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = SerifFontFamily
                        )
                    }
                }
            }

            // Grid of Stats Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Living vs Deceased Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Vital Status",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = SerifFontFamily),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        
                        StatItem(
                            label = stringResource(R.string.living),
                            value = state.livingMembers.toString(),
                            color = LivingAccent
                        )
                        
                        StatItem(
                            label = stringResource(R.string.deceased),
                            value = state.deceasedMembers.toString(),
                            color = DeceasedAccent
                        )

                        // Visual horizontal ratio bar
                        if (state.totalMembers > 0) {
                            val livingRatio: Float = state.livingMembers.toFloat() / state.totalMembers.toFloat()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DeceasedAccent.copy(alpha = 0.15f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(livingRatio.coerceAtLeast(0.01f))
                                        .background(LivingAccent)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((1f - livingRatio).coerceAtLeast(0.01f))
                                        .background(DeceasedAccent)
                                )
                            }
                        }
                    }
                }

                // Gender Demographics Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.gender),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontFamily = SerifFontFamily),
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        
                        StatItem(
                            label = stringResource(R.string.male),
                            value = state.maleMembers.toString(),
                            color = MaleAccent
                        )
                        
                        StatItem(
                            label = stringResource(R.string.female),
                            value = state.femaleMembers.toString(),
                            color = FemaleAccent
                        )

                        // Gender ratio bar
                        if (state.totalMembers > 0) {
                            val maleRatio: Float = state.maleMembers.toFloat() / state.totalMembers.toFloat()
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight(maleRatio.coerceAtLeast(0.01f))
                                        .background(MaleAccent)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .weight((1f - maleRatio).coerceAtLeast(0.01f))
                                        .background(FemaleAccent)
                                )
                            }
                        }
                    }
                }
            }

            // Insights Card
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
                        text = stringResource(R.string.insights),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = SerifFontFamily),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                stringResource(R.string.average_age),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${String.format("%.1f", state.averageAge)} ${stringResource(R.string.years)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold)
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                stringResource(R.string.max_generation_depth),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            Text(
                                text = "${state.maxGenerationDepth} ${stringResource(R.string.generations)}",
                                style = MaterialTheme.typography.titleLarge.copy(fontFamily = SerifFontFamily, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }

            // Fun Facts Card
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
                        text = "Fun Facts & Historical Archives",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = SerifFontFamily),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    
                    if (state.longestLivingMemberName != null) {
                        ProfileFactRow("Longest Lived", "${state.longestLivingMemberName} (${state.longestLivingAge} yrs)")
                    }
                    if (state.oldestLivingMemberName != null) {
                        ProfileFactRow("Oldest Living", "${state.oldestLivingMemberName} (${state.oldestLivingAge} yrs)")
                    }
                    if (state.mostCommonBirthplace != null) {
                        ProfileFactRow("Common Birthplace", state.mostCommonBirthplace ?: "N/A")
                    }
                    if (state.mostCommonBirthMonth != null) {
                        ProfileFactRow("Common Birth Month", state.mostCommonBirthMonth ?: "N/A")
                    }
                    
                    ProfileFactRow("Marriages Count", state.marriageCount.toString())
                    ProfileFactRow("Parent-Child Links", state.parentChildLinkCount.toString())
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun StatItem(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        }
        Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
    }
}

@Composable
fun ProfileFactRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(value, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
    }
}
