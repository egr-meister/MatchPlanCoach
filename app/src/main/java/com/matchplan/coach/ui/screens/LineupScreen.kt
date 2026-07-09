package com.matchplan.coach.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.matchplan.coach.data.model.FormationSlot
import com.matchplan.coach.data.model.FormationType
import com.matchplan.coach.data.model.Formations
import com.matchplan.coach.data.model.Player
import com.matchplan.coach.data.model.StartingLineup
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.EnumDropdown
import com.matchplan.coach.ui.components.InfoBanner
import com.matchplan.coach.ui.components.MissingEntityFallback
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.ui.theme.PitchGreen

@Composable
fun LineupScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val match = ScreenHelpers.matchById(data, matchId)

    PlannerScaffold(title = "Starting Lineup", onBack = { nav.popBackStack() }) { padding ->
        if (match == null) {
            MissingEntityFallback("This match could not be found.") { nav.popBackStack() }
            return@PlannerScaffold
        }
        if (data.players.isEmpty()) {
            Column(Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
                InfoBanner("Add players first, then set your starting lineup here.")
            }
            return@PlannerScaffold
        }

        val saved = data.startingLineups.firstOrNull { it.matchId == match.id }
        var formation by remember(match.id) {
            mutableStateOf(saved?.formation ?: FormationType.FourFourTwo)
        }
        // Editable slots. Reset when formation changes.
        val slots: SnapshotStateList<FormationSlot> = remember(match.id, formation) {
            val base = if (saved != null && saved.formation == formation && saved.slots.isNotEmpty()) {
                saved.slots
            } else {
                Formations.defaultSlots(formation)
            }
            base.toMutableStateList()
        }

        var pickerForSlot by remember { mutableStateOf<String?>(null) }
        var editSlot by remember { mutableStateOf<String?>(null) }

        fun playerName(id: String?): String = ScreenHelpers.playerName(data, id)

        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            EnumDropdown(
                label = "Formation",
                options = FormationType.entries,
                selected = formation,
                optionLabel = { it.label },
                onSelected = { formation = it }
            )
            Spacer(Modifier.height(6.dp))
            Text("Tap a slot to assign, replace or clear a player.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(12.dp))

            // ---- Pitch board ----
            BoxWithConstraints(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.72f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(PitchGreen)
            ) {
                val w = maxWidth
                val h = maxHeight
                PitchLines()
                slots.forEachIndexed { index, slot ->
                    val markerSize = 46.dp
                    val xPos = w * slot.x - markerSize / 2
                    val yPos = h * slot.y - markerSize / 2
                    Box(
                        Modifier
                            .offset(x = xPos, y = yPos)
                            .size(markerSize)
                    ) {
                        SlotMarker(
                            slot = slot,
                            playerName = playerName(slot.assignedPlayerId),
                            assignedNumber = data.players
                                .firstOrNull { it.id == slot.assignedPlayerId }?.number,
                            onClick = {
                                if (slot.assignedPlayerId == null) pickerForSlot = slot.slotId
                                else editSlot = slot.slotId
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Assigned: ${slots.count { it.assignedPlayerId != null }} / ${slots.size}",
                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            // ---- Bench (squad players not in the lineup) ----
            val squad = data.squads.firstOrNull { it.matchId == match.id }
            val assignedIds = slots.mapNotNull { it.assignedPlayerId }.toSet()
            val benchIds = (squad?.benchPlayerIds ?: emptyList()).filterNot { it in assignedIds }
            if (benchIds.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text("Bench", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                benchIds.forEach { id ->
                    Text("• ${playerName(id)}", style = MaterialTheme.typography.bodyMedium)
                }
            }

            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    vm.saveLineup(
                        StartingLineup(match.id, formation, slots.toList())
                    )
                    nav.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save lineup") }
            Spacer(Modifier.height(24.dp))
        }

        // Player picker dialog (for empty slot or replace).
        if (pickerForSlot != null) {
            PlayerPickerDialog(
                players = data.players,
                assignedIds = slots.mapNotNull { it.assignedPlayerId }.toSet(),
                onPick = { player ->
                    assignPlayer(slots, pickerForSlot!!, player.id)
                    pickerForSlot = null
                },
                onDismiss = { pickerForSlot = null }
            )
        }

        // Edit an occupied slot: Replace or Clear.
        editSlot?.let { slotId ->
            val slot = slots.firstOrNull { it.slotId == slotId }
            AlertDialog(
                onDismissRequest = { editSlot = null },
                title = { Text("Slot ${slot?.label ?: ""}") },
                text = { Text("Currently: ${playerName(slot?.assignedPlayerId)}") },
                confirmButton = {
                    TextButton(onClick = {
                        editSlot = null
                        pickerForSlot = slotId
                    }) { Text("Replace") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            assignPlayer(slots, slotId, null)
                            editSlot = null
                        }) { Text("Clear") }
                        TextButton(onClick = { editSlot = null }) { Text("Cancel") }
                    }
                }
            )
        }
    }
}

/** Assign (or clear) a player to a slot, removing them from any other slot. */
private fun assignPlayer(
    slots: SnapshotStateList<FormationSlot>,
    slotId: String,
    playerId: String?
) {
    for (i in slots.indices) {
        val s = slots[i]
        when {
            playerId != null && s.assignedPlayerId == playerId && s.slotId != slotId ->
                slots[i] = s.copy(assignedPlayerId = null) // remove from old slot
            s.slotId == slotId ->
                slots[i] = s.copy(assignedPlayerId = playerId)
        }
    }
}

@Composable
private fun PitchLines() {
    // Simple white pitch markings drawn on a Canvas that fills the pitch box.
    androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
        val stroke = size.minDimension * 0.008f
        val white = Color.White.copy(alpha = 0.85f)
        // Outer border
        drawRectBorder(white, stroke)
        // Halfway line
        drawLineH(white, stroke, 0.5f)
        // Center circle
        drawCenterCircle(white, stroke)
        // Penalty boxes
        drawPenaltyBox(white, stroke, top = true)
        drawPenaltyBox(white, stroke, top = false)
    }
}

// ---- Canvas drawing helpers ----
private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawRectBorder(
    color: Color, stroke: Float
) {
    drawRect(color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        topLeft = androidx.compose.ui.geometry.Offset(stroke, stroke),
        size = androidx.compose.ui.geometry.Size(size.width - stroke * 2, size.height - stroke * 2))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLineH(
    color: Color, stroke: Float, yFraction: Float
) {
    val y = size.height * yFraction
    drawLine(color, androidx.compose.ui.geometry.Offset(0f, y),
        androidx.compose.ui.geometry.Offset(size.width, y), strokeWidth = stroke)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCenterCircle(
    color: Color, stroke: Float
) {
    drawCircle(color = color, radius = size.width * 0.14f,
        center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke))
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawPenaltyBox(
    color: Color, stroke: Float, top: Boolean
) {
    val boxW = size.width * 0.5f
    val boxH = size.height * 0.16f
    val left = (size.width - boxW) / 2
    val topY = if (top) 0f else size.height - boxH
    drawRect(color = color, style = androidx.compose.ui.graphics.drawscope.Stroke(width = stroke),
        topLeft = androidx.compose.ui.geometry.Offset(left, topY),
        size = androidx.compose.ui.geometry.Size(boxW, boxH))
}

@Composable
private fun SlotMarker(
    slot: FormationSlot,
    playerName: String,
    assignedNumber: Int?,
    onClick: () -> Unit
) {
    val filled = slot.assignedPlayerId != null
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = if (filled) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.25f),
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier.size(40.dp).clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    if (filled) (assignedNumber?.toString() ?: "•") else slot.label,
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (filled) {
            Text(
                playerName.take(8),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PlayerPickerDialog(
    players: List<Player>,
    assignedIds: Set<String>,
    onPick: (Player) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select player") },
        text = {
            if (players.isEmpty()) {
                Text("No players available. Add players first.")
            } else {
                LazyColumn(Modifier.fillMaxWidth().height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(players.sortedBy { it.name.lowercase() }, key = { it.id }) { p ->
                        val alreadyAssigned = p.id in assignedIds
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().clickable { onPick(p) }
                        ) {
                            Row(Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                Text((p.number?.let { "#$it " } ?: "") +
                                    p.name.ifBlank { "Unnamed" },
                                    modifier = Modifier.weight(1f))
                                if (alreadyAssigned) {
                                    Text("assigned",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
