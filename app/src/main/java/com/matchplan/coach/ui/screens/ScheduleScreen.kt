package com.matchplan.coach.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.matchplan.coach.data.model.MatchSource
import com.matchplan.coach.data.model.NormalizedMatch
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.Copy
import com.matchplan.coach.ui.components.EmptyState
import com.matchplan.coach.ui.components.InfoBanner
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.ui.components.StatusChip
import com.matchplan.coach.ui.navigation.Routes
import com.matchplan.coach.ui.theme.InfoBlue
import com.matchplan.coach.ui.theme.MutedLabelGray
import com.matchplan.coach.ui.theme.StatusGreen
import com.matchplan.coach.util.DateUtils

@Composable
fun ScheduleScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.schedule.collectAsState()

    LaunchedEffect(Unit) { vm.loadScheduleIfNeeded() }

    PlannerScaffold(
        title = "Match Schedule",
        onBack = { nav.popBackStack() },
        actions = {
            IconButton(onClick = { nav.navigate(Routes.SCHEDULE_SETTINGS) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Schedule settings")
            }
            IconButton(onClick = { vm.refreshSchedule() }) {
                Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                InfoBanner(Copy.SCHEDULE_SHORT_NOTE)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val windowLabel = if (state.isDefaultWindow) {
                        "Today + 9 days (${state.activeDateFrom} → ${state.activeDateTo})"
                    } else {
                        "${state.activeDateFrom} → ${state.activeDateTo}"
                    }
                    Text(
                        "Window: $windowLabel",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    SourceChip(state.source)
                }
                if (state.lastUpdatedAt.isNotBlank()) {
                    Text("Last updated: ${state.lastUpdatedAt}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (state.message.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error)
                }
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                state.matches.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        EmptyState(
                            "No matches available.",
                            "Try refreshing or check API settings."
                        )
                    }
                }
                else -> {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(state.matches, key = { it.id }) { m -> ScheduleCard(m) }
                        item { Spacer(Modifier.height(8.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SourceChip(source: MatchSource) {
    val (label, color) = when (source) {
        MatchSource.Api -> "Live" to StatusGreen
        MatchSource.Cache -> "Cached" to InfoBlue
        MatchSource.Demo -> "Demo" to MutedLabelGray
    }
    StatusChip(label, color)
}

@Composable
private fun ScheduleCard(m: NormalizedMatch) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(m.competitionName.ifBlank { "Football match" } +
                        (if (m.competitionCode.isNotBlank()) " (${m.competitionCode})" else ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text(m.homeTeam.ifBlank { "Unknown" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Text("vs ${m.awayTeam.ifBlank { "Unknown" }}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    if (m.homeScore != null && m.awayScore != null) {
                        Text("${m.homeScore} - ${m.awayScore}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold)
                    }
                    val dateLabel = m.date.ifBlank { DateUtils.dateFromUtc(m.utcDate) }
                    Text(DateUtils.prettyDate(dateLabel),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (m.time.isNotBlank()) {
                        Text(m.time, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            if (m.status.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text("Status: ${m.status}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
