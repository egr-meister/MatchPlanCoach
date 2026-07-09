package com.matchplan.coach.ui.screens

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
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.ConfirmDialog
import com.matchplan.coach.ui.components.MatchStatusChip
import com.matchplan.coach.ui.components.MissingEntityFallback
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.ui.components.SectionCard
import com.matchplan.coach.ui.navigation.Routes
import com.matchplan.coach.util.DateUtils

@Composable
fun MatchDetailScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val match = ScreenHelpers.matchById(data, matchId)
    var showDelete by remember { mutableStateOf(false) }

    PlannerScaffold(
        title = "Match Detail",
        onBack = { nav.popBackStack() },
        actions = {
            if (match != null) {
                IconButton(onClick = { nav.navigate(Routes.editMatch(match.id)) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = {
                    vm.duplicateMatch(match.id) { newId -> nav.navigate(Routes.matchDetail(newId)) }
                }) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate")
                }
            }
        }
    ) { padding ->
        if (match == null) {
            MissingEntityFallback("This match could not be found.") { nav.popBackStack() }
            return@PlannerScaffold
        }

        val (done, total) = ScreenHelpers.taskProgress(data, match.id)
        val squadCount = ScreenHelpers.squadCount(data, match.id)
        val lineup = data.startingLineups.firstOrNull { it.matchId == match.id }
        val assigned = lineup?.slots?.count { it.assignedPlayerId != null } ?: 0

        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionCard {
                Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("vs ${match.opponent.ifBlank { "Unknown opponent" }}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                        Text(DateUtils.prettyDate(match.matchDate),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    MatchStatusChip(match.status)
                }
                Spacer(Modifier.height(8.dp))
                DetailRow("Time", match.matchTime.ifBlank { "Not set" })
                DetailRow("Venue", match.venue.ifBlank { "Not set" })
                DetailRow("Match type", match.matchType.label)
                DetailRow("Team", match.teamName.ifBlank { "Not set" })
            }

            SectionCard {
                Text("Preparation", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                DetailRow("Squad selected", "$squadCount player(s)")
                DetailRow("Starting lineup", "$assigned assigned" +
                    (lineup?.let { " (${it.formation.label})" } ?: ""))
                DetailRow("Tasks", "$done / $total completed")
                val notesPreview = listOf(
                    match.preMatchNotes, match.tacticalNotes,
                    match.postMatchNotes, match.resultNotes
                ).firstOrNull { it.isNotBlank() } ?: "No notes yet"
                DetailRow("Notes", notesPreview.take(60))
            }

            Text("Actions", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)

            Button(onClick = { nav.navigate(Routes.squad(match.id)) },
                modifier = Modifier.fillMaxWidth()) { Text("Match Squad") }
            Button(onClick = { nav.navigate(Routes.lineup(match.id)) },
                modifier = Modifier.fillMaxWidth()) { Text("Starting Lineup") }
            Button(onClick = { nav.navigate(Routes.tasks(match.id)) },
                modifier = Modifier.fillMaxWidth()) { Text("Match Tasks") }
            Button(onClick = { nav.navigate(Routes.notes(match.id)) },
                modifier = Modifier.fillMaxWidth()) { Text("Match Notes") }
            OutlinedButton(onClick = { nav.navigate(Routes.editMatch(match.id)) },
                modifier = Modifier.fillMaxWidth()) { Text("Edit Match") }
            OutlinedButton(onClick = { showDelete = true },
                modifier = Modifier.fillMaxWidth()) { Text("Delete Match") }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDelete && match != null) {
        ConfirmDialog(
            title = "Delete match?",
            message = "This removes the match plan and everything attached to it.",
            confirmLabel = "Delete",
            onConfirm = {
                showDelete = false
                vm.deleteMatch(match.id)
                nav.popBackStack()
            },
            onDismiss = { showDelete = false }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.4f))
    }
}
