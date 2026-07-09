package com.matchplan.coach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChecklistRtl
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.outlined.Stadium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.DateBadge
import com.matchplan.coach.ui.components.MatchStatusChip
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.ui.components.SectionCard
import com.matchplan.coach.ui.navigation.Routes
import com.matchplan.coach.util.DateUtils

@Composable
fun HomeScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    val next = ScreenHelpers.nextMatch(data)
    val upcoming = data.matchPlans
        .filter { it.status.name == "Planned" || it.status.name == "Ready" }
        .sortedBy { it.matchDate }

    fun openTasksOrAdd() {
        if (next != null) nav.navigate(Routes.tasks(next.id)) else nav.navigate(Routes.ADD_MATCH)
    }
    fun openLineupOrAdd() {
        if (next != null) nav.navigate(Routes.lineup(next.id)) else nav.navigate(Routes.ADD_MATCH)
    }

    PlannerScaffold(
        title = "MatchPlan Coach",
        actions = {
            IconButton(onClick = { nav.navigate(Routes.SETTINGS) }) {
                Icon(Icons.Filled.Settings, contentDescription = "Settings")
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Text(
                "Amateur football match planner",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Today • ${DateUtils.prettyDate(DateUtils.today())}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            // Next planned match highlight.
            Text("Next match", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            if (next != null) {
                NextMatchCard(
                    opponent = next.opponent,
                    date = next.matchDate,
                    time = next.matchTime,
                    venue = next.venue,
                    statusChip = { MatchStatusChip(next.status) },
                    onClick = { nav.navigate(Routes.matchDetail(next.id)) }
                )
            } else {
                SectionCard {
                    Text("No upcoming matches.", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Add a match plan and prepare your team.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
            Text("Quick actions", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(Icons.Filled.Add, "Add Match",
                    Modifier.weight(1f)) { nav.navigate(Routes.ADD_MATCH) }
                QuickAction(Icons.Filled.Groups, "Player List",
                    Modifier.weight(1f)) { nav.navigate(Routes.PLAYER_LIST) }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(Icons.Filled.History, "Match History",
                    Modifier.weight(1f)) { nav.navigate(Routes.HISTORY) }
                QuickAction(Icons.Filled.ChecklistRtl, "Match Tasks",
                    Modifier.weight(1f)) { openTasksOrAdd() }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickAction(Icons.Filled.SportsSoccer, "Starting Lineup",
                    Modifier.weight(1f)) { openLineupOrAdd() }
                Spacer(Modifier.weight(1f))
            }

            if (upcoming.size > 1) {
                Spacer(Modifier.height(20.dp))
                Text("More upcoming", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                upcoming.drop(1).take(4).forEach { m ->
                    UpcomingRow(
                        opponent = m.opponent,
                        date = m.matchDate,
                        onClick = { nav.navigate(Routes.matchDetail(m.id)) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(20.dp))
            // Secondary, visually calm Match Schedule card.
            ScheduleTeaser(onClick = { nav.navigate(Routes.SCHEDULE) })
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun NextMatchCard(
    opponent: String,
    date: String,
    time: String,
    venue: String,
    statusChip: @Composable () -> Unit,
    onClick: () -> Unit
) {
    SectionCard(modifier = Modifier.clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            DateBadge(ScreenHelpers.dayNumber(date), ScreenHelpers.monthShort(date))
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "vs ${opponent.ifBlank { "Unknown opponent" }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val sub = buildString {
                    append(DateUtils.prettyDate(date))
                    if (time.isNotBlank()) append(" • $time")
                }
                Text(sub, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (venue.isNotBlank()) {
                    Text("@ $venue", style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            statusChip()
        }
    }
}

@Composable
private fun UpcomingRow(opponent: String, date: String, onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateBadge(ScreenHelpers.dayNumber(date), ScreenHelpers.monthShort(date))
            Spacer(Modifier.width(12.dp))
            Text("vs ${opponent.ifBlank { "Unknown opponent" }}",
                style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.height(96.dp).clickable { onClick() },
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            Modifier.padding(12.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    icon, contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(8.dp).size(24.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun ScheduleTeaser(onClick: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Outlined.Stadium, contentDescription = null,
                tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text("Match Schedule", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(
                    "Extra reference — view football matches (secondary feature).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.Filled.CalendarMonth, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
