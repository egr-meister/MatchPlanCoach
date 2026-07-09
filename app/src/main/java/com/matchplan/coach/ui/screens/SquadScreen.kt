package com.matchplan.coach.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.matchplan.coach.data.model.MatchSquad
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.InfoBanner
import com.matchplan.coach.ui.components.MissingEntityFallback
import com.matchplan.coach.ui.components.PlannerScaffold

private enum class SquadRole { None, Starter, Bench, Unavailable }

@Composable
fun SquadScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val match = ScreenHelpers.matchById(data, matchId)

    PlannerScaffold(title = "Match Squad", onBack = { nav.popBackStack() }) { padding ->
        if (match == null) {
            MissingEntityFallback("This match could not be found.") { nav.popBackStack() }
            return@PlannerScaffold
        }
        if (data.players.isEmpty()) {
            Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                InfoBanner("Add players first on the Player List screen, then build your squad here.")
            }
            return@PlannerScaffold
        }

        val existing = data.squads.firstOrNull { it.matchId == match.id }
        // Local editable role map, seeded from the saved squad.
        val roles = remember(match.id, data.players.size) {
            val map = mutableStateMapOf<String, SquadRole>()
            data.players.forEach { p ->
                map[p.id] = when {
                    existing?.startingLineupPlayerIds?.contains(p.id) == true -> SquadRole.Starter
                    existing?.benchPlayerIds?.contains(p.id) == true -> SquadRole.Bench
                    existing?.unavailablePlayerIds?.contains(p.id) == true -> SquadRole.Unavailable
                    else -> SquadRole.None
                }
            }
            map
        }

        val starters = roles.count { it.value == SquadRole.Starter }

        LazyColumn(
            Modifier.fillMaxSize().padding(padding),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text("vs ${match.opponent.ifBlank { "Unknown opponent" }}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("Starters: $starters (up to 11) • Tap a role for each player.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (starters > 11) {
                    Spacer(Modifier.height(6.dp))
                    InfoBanner("You have more than 11 starters. Only the first 11 count as the starting lineup, the rest still stay in the squad.")
                }
                Spacer(Modifier.height(4.dp))
            }
            items(data.players.sortedBy { it.name.lowercase() }, key = { it.id }) { player ->
                Surface(color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(12.dp)) {
                        Text(
                            (player.number?.let { "#$it " } ?: "") +
                                player.name.ifBlank { "Unnamed player" },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(player.position.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        RoleChips(
                            current = roles[player.id] ?: SquadRole.None,
                            onSelect = { roles[player.id] = it }
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.Button(
                    onClick = {
                        val squad = buildSquad(match.id, roles)
                        vm.saveSquad(squad)
                        nav.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Save squad") }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun RoleChips(current: SquadRole, onSelect: (SquadRole) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            SquadRole.None to "None",
            SquadRole.Starter to "Starter",
            SquadRole.Bench to "Bench",
            SquadRole.Unavailable to "Out"
        ).forEach { (role, label) ->
            FilterChip(
                selected = current == role,
                onClick = { onSelect(role) },
                label = { Text(label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

private fun buildSquad(
    matchId: String,
    roles: SnapshotStateMap<String, SquadRole>
): MatchSquad {
    val starters = roles.filter { it.value == SquadRole.Starter }.keys.toList()
    val bench = roles.filter { it.value == SquadRole.Bench }.keys.toList()
    val unavailable = roles.filter { it.value == SquadRole.Unavailable }.keys.toList()
    return MatchSquad(
        matchId = matchId,
        selectedPlayerIds = starters + bench,
        startingLineupPlayerIds = starters,
        benchPlayerIds = bench,
        unavailablePlayerIds = unavailable
    )
}
