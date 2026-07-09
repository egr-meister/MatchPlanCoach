package com.matchplan.coach.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
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
import com.matchplan.coach.data.model.Player
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.AvailabilityChip
import com.matchplan.coach.ui.components.ConfirmDialog
import com.matchplan.coach.ui.components.EmptyState
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.ui.navigation.Routes

@Composable
fun PlayerListScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    val players = data.players.sortedBy { it.name.lowercase() }
    var toDelete by remember { mutableStateOf<Player?>(null) }

    PlannerScaffold(
        title = "Player List",
        onBack = { nav.popBackStack() },
        floatingActionButton = {
            FloatingActionButton(onClick = { nav.navigate(Routes.ADD_PLAYER) }) {
                Icon(Icons.Filled.Add, contentDescription = "Add player")
            }
        }
    ) { padding ->
        if (players.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No players yet.", "Add players to build your match squad.")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(players, key = { it.id }) { player ->
                    PlayerRow(
                        player = player,
                        onClick = { nav.navigate(Routes.editPlayer(player.id)) },
                        onDelete = { toDelete = player }
                    )
                }
            }
        }
    }

    toDelete?.let { p ->
        ConfirmDialog(
            title = "Delete player?",
            message = "${p.name.ifBlank { "This player" }} will be removed from all squads and lineups.",
            confirmLabel = "Delete",
            onConfirm = { vm.deletePlayer(p.id); toDelete = null },
            onDismiss = { toDelete = null }
        )
    }
}

@Composable
private fun PlayerRow(player: Player, onClick: () -> Unit, onDelete: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = MaterialTheme.colorScheme.primary, shape = CircleShape,
                modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        player.number?.toString() ?: "-",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(player.name.ifBlank { "Unnamed player" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text(player.position.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (player.note.isNotBlank()) {
                    Text(player.note.take(40),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                AvailabilityChip(player.availability)
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}
