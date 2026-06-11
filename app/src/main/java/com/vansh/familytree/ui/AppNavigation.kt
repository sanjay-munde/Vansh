package com.vansh.familytree.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.vansh.familytree.R
import com.vansh.familytree.ui.analytics.AnalyticsScreen
import com.vansh.familytree.ui.analytics.RelationshipFinderScreen
import com.vansh.familytree.ui.member.MemberFormScreen
import com.vansh.familytree.ui.member.MemberListScreen
import com.vansh.familytree.ui.member.MemberProfileScreen
import com.vansh.familytree.ui.tree.MegaTreeScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route

            // Only show bottom bar on top-level destinations
            if (currentRoute == "members" || currentRoute == "mega_tree" || currentRoute == "analytics") {
                NavigationBar {
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.List, contentDescription = null) },
                        label = { Text("Members") },
                        selected = currentDestination?.hierarchy?.any { it.route == "members" } == true,
                        onClick = {
                            navController.navigate("members") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Share, contentDescription = null) },
                        label = { Text("Tree") },
                        selected = currentDestination?.hierarchy?.any { it.route == "mega_tree" } == true,
                        onClick = {
                            navController.navigate("mega_tree") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        label = { Text("Analytics") },
                        selected = currentDestination?.hierarchy?.any { it.route == "analytics" } == true,
                        onClick = {
                            navController.navigate("analytics") {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "members",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("members") {
                MemberListScreen(
                    onNavigateToMemberForm = { memberId -> 
                        if (memberId == null) {
                            navController.navigate("add_member")
                        } else {
                            navController.navigate("edit_member/$memberId")
                        }
                    },
                    onNavigateToMemberProfile = { memberId ->
                        navController.navigate("profile/$memberId")
                    },
                    onNavigateToRelationshipFinder = {
                        navController.navigate("relationship_finder")
                    }
                )
            }
            composable("add_member") {
                MemberFormScreen(
                    memberId = null,
                    onNavigateBack = { navController.navigateUp() }
                )
            }
            composable(
                route = "edit_member/{memberId}",
                arguments = listOf(navArgument("memberId") { type = NavType.StringType })
            ) { backStackEntry ->
                val memberId = backStackEntry.arguments?.getString("memberId")
                MemberFormScreen(
                    memberId = memberId,
                    onNavigateBack = { navController.navigateUp() }
                )
            }
            composable(
                route = "profile/{memberId}",
                arguments = listOf(navArgument("memberId") { type = NavType.StringType })
            ) { backStackEntry ->
                val memberId = backStackEntry.arguments?.getString("memberId") ?: ""
                MemberProfileScreen(
                    memberId = memberId,
                    onNavigateBack = { navController.navigateUp() },
                    onNavigateToEdit = { id -> navController.navigate("edit_member/$id") }
                )
            }
            composable("mega_tree") {
                MegaTreeScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }
            composable("analytics") {
                AnalyticsScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }
            composable("relationship_finder") {
                RelationshipFinderScreen(
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}
