package com.matchplan.coach.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import com.matchplan.coach.data.model.MatchPlan
import com.matchplan.coach.data.model.MatchStatus
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.ConfirmDialog
import com.matchplan.coach.ui.components.DateBadge
import com.matchplan.coach.ui.components.EmptyState
import com.matchplan.coach.ui.components.MatchStatusChip
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.ui.navigation.Routes

@Composable
fun HistoryScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    var filter by remember { mutableStateOf<MatchStatus?>(null) } // null = All
    var toDelete by remember { mutableStateOf<MatchPlan?>(null) }

    val all = ScreenHelpers.matchesNewestFirst(data)
    val filtered = if (filter == null) all else all.filter { it.status == filter }

    PlannerScaffold(title = "Match History", onBack = { nav.popBackStack() }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Filter chips
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(selected = filter == null, onClick = { filter = null },
                    label = { Text("All") })
                MatchStatus.entries.forEach { s ->
                    FilterChip(selected = filter == s, onClick = { filter = s },
                        label = { Text(s.label) })
                }
            }

            if (filtered.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyState(
                        if (all.isEmpty()) "No match plans yet." else "No matches for this filter.",
                        if (all.isEmpty()) "Create your first match plan." else "Try another status filter."
                    )
                }
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filtered, key = { it.id }) { match ->
                        val (done, total) = ScreenHelpers.taskProgress(data, match.id)
                        val squad = ScreenHelpers.squadCount(data, match.id)
                        HistoryCard(
                            match = match,
                            squadCount = squad,
                            tasksDone = done,
                            tasksTotal = total,
                            onOpen = { nav.navigate(Routes.matchDetail(match.id)) },
                            onDuplicate = { vm.duplicateMatch(match.id) },
                            onDelete = { toDelete = match }
                        )
                    }
                }
            }
        }
    }

    toDelete?.let { m ->
        ConfirmDialog(
            title = "Delete match?",
            message = "This removes the match plan and everything attached to it.",
            confirmLabel = "Delete",
            onConfirm = { vm.deleteMatch(m.id); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun HistoryCard(
    match: MatchPlan,
    squadCount: Int,
    tasksDone: Int,
    tasksTotal: Int,
    onOpen: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable { onOpen() }
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            DateBadge(ScreenHelpers.dayNumber(match.matchDate),
                ScreenHelpers.monthShort(match.matchDate))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("vs ${match.opponent.ifBlank { "Unknown opponent" }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                if (match.venue.isNotBlank()) {
                    Text("@ ${match.venue}", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Squad $squadCount • Tasks $tasksDone/$tasksTotal",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(4.dp))
                MatchStatusChip(match.status)
            }
            Column {
                IconButton(onClick = onDuplicate) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Duplicate")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
