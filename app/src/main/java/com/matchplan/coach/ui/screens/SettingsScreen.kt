package com.matchplan.coach.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.Copy
import com.matchplan.coach.ui.components.ConfirmDialog
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.ui.components.SectionCard
import com.matchplan.coach.ui.navigation.Routes

private enum class ConfirmAction { ClearCache, DeleteMatches, DeletePlayers, DeleteTasks, ResetAll }

@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    var confirm by remember { mutableStateOf<ConfirmAction?>(null) }

    PlannerScaffold(title = "Settings", onBack = { nav.popBackStack() }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Preferences
            SectionCard {
                Text("Preferences", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Compact mode", style = MaterialTheme.typography.bodyLarge)
                        Text("Slightly denser match and player lists.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = data.settings.compactMode,
                        onCheckedChange = { vm.setCompactMode(it) })
                }
                Spacer(Modifier.height(8.dp))
                ClickableRow("Match Schedule settings") {
                    nav.navigate(Routes.SCHEDULE_SETTINGS)
                }
                ClickableRow("Player list") { nav.navigate(Routes.PLAYER_LIST) }
                ClickableRow("Show onboarding again") {
                    vm.showOnboardingAgain()
                    nav.navigate(Routes.ONBOARDING)
                }
            }

            // API status
            SectionCard {
                Text("Match Schedule API", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("API token detected: ${if (vm.hasApiToken()) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodyMedium)
                Text("API enabled: ${if (data.settings.matchSchedule.apiEnabled) "Yes" else "No"}",
                    style = MaterialTheme.typography.bodyMedium)
                val cached = data.matchScheduleCache.cachedMatches.size
                Text("Cached matches: $cached",
                    style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                ClickableRow("Clear match cache") { confirm = ConfirmAction.ClearCache }
            }

            // Data management
            SectionCard {
                Text("Local data", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                DangerRow("Delete all match plans") { confirm = ConfirmAction.DeleteMatches }
                DangerRow("Delete all players") { confirm = ConfirmAction.DeletePlayers }
                DangerRow("Delete all tasks") { confirm = ConfirmAction.DeleteTasks }
                DangerRow("Reset all local data") { confirm = ConfirmAction.ResetAll }
            }

            // App information + disclaimers
            SectionCard {
                Text("App information", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(4.dp))
                Text("MatchPlan Coach — amateur football match planner",
                    style = MaterialTheme.typography.bodyMedium)
                Text("Version 1.0.0", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(10.dp))
                DisclaimerBlock("Match planning", Copy.PLANNING_DISCLAIMER)
                Spacer(Modifier.height(8.dp))
                DisclaimerBlock("Match schedule data", Copy.SCHEDULE_DISCLAIMER)
                Spacer(Modifier.height(8.dp))
                DisclaimerBlock("Privacy", Copy.PRIVACY_NOTE)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    confirm?.let { action ->
        val (title, message, onConfirm) = when (action) {
            ConfirmAction.ClearCache -> Triple("Clear match cache?",
                "Cached match schedule data will be removed.", { vm.clearScheduleCache() })
            ConfirmAction.DeleteMatches -> Triple("Delete all match plans?",
                "All match plans, squads, lineups and tasks will be removed.",
                { vm.deleteAllMatches() })
            ConfirmAction.DeletePlayers -> Triple("Delete all players?",
                "All players will be removed and cleared from squads and lineups.",
                { vm.deleteAllPlayers() })
            ConfirmAction.DeleteTasks -> Triple("Delete all tasks?",
                "All match tasks will be removed.", { vm.deleteAllTasks() })
            ConfirmAction.ResetAll -> Triple("Reset all local data?",
                "This permanently deletes everything: matches, players, tasks, notes, " +
                    "settings and cached data.", { vm.resetAll() })
        }
        ConfirmDialog(
            title = title,
            message = message,
            confirmLabel = "Confirm",
            onConfirm = { onConfirm(); confirm = null },
            onDismiss = { confirm = null }
        )
    }
}

@Composable
private fun ClickableRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun DangerRow(label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable { onClick() }.padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error)
        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
            tint = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun DisclaimerBlock(title: String, body: String) {
    Column {
        Text(title, style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold)
        Text(body, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
