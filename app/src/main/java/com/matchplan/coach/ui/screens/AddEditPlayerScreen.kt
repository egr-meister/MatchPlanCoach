package com.matchplan.coach.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.matchplan.coach.data.model.Player
import com.matchplan.coach.data.model.PlayerAvailability
import com.matchplan.coach.data.model.PlayerPosition
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.AppTextField
import com.matchplan.coach.ui.components.ConfirmDialog
import com.matchplan.coach.ui.components.EnumDropdown
import com.matchplan.coach.ui.components.PlannerScaffold

@Composable
fun AddEditPlayerScreen(vm: AppViewModel, nav: NavController, playerId: String?) {
    val data by vm.appData.collectAsState()
    val existing = ScreenHelpers.playerById(data, playerId)
    val isEdit = existing != null

    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var numberText by remember(existing) {
        mutableStateOf(existing?.number?.toString() ?: "")
    }
    var note by remember(existing) { mutableStateOf(existing?.note ?: "") }
    var position by remember(existing) {
        mutableStateOf(existing?.position ?: PlayerPosition.Midfielder)
    }
    var availability by remember(existing) {
        mutableStateOf(existing?.availability ?: PlayerAvailability.Available)
    }
    var showValidation by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }

    val nameError = name.isBlank()
    val parsedNumber = numberText.trim().toIntOrNull()
    val numberError = numberText.isNotBlank() && (parsedNumber == null || parsedNumber !in 1..99)

    PlannerScaffold(
        title = if (isEdit) "Edit Player" else "Add Player",
        onBack = { nav.popBackStack() },
        actions = {
            if (isEdit) {
                IconButton(onClick = { showDelete = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete player")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(
                value = name, onValueChange = { name = it },
                label = "Player name *",
                isError = showValidation && nameError,
                errorText = "Player name must not be empty."
            )
            AppTextField(
                value = numberText,
                onValueChange = { input -> numberText = input.filter { it.isDigit() }.take(2) },
                label = "Shirt number (1-99, optional)",
                isError = showValidation && numberError,
                errorText = "Number must be between 1 and 99."
            )
            EnumDropdown(
                label = "Position",
                options = PlayerPosition.entries,
                selected = position,
                optionLabel = { it.label },
                onSelected = { position = it }
            )
            EnumDropdown(
                label = "Availability",
                options = PlayerAvailability.entries,
                selected = availability,
                optionLabel = { it.label },
                onSelected = { availability = it }
            )
            AppTextField(
                value = note, onValueChange = { note = it },
                label = "Note (optional)", singleLine = false, minLines = 2
            )

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    showValidation = true
                    if (!nameError && !numberError) {
                        val toSave = (existing ?: Player()).copy(
                            id = existing?.id ?: "",
                            name = name.trim(),
                            number = parsedNumber,
                            position = position,
                            availability = availability,
                            note = note
                        )
                        vm.savePlayer(toSave) { nav.popBackStack() }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (isEdit) "Save changes" else "Save player")
            }
            if (isEdit) {
                OutlinedButton(onClick = { showDelete = true },
                    modifier = Modifier.fillMaxWidth()) { Text("Delete player") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDelete && existing != null) {
        ConfirmDialog(
            title = "Delete player?",
            message = "This player will be removed from all squads and lineups.",
            confirmLabel = "Delete",
            onConfirm = { showDelete = false; vm.deletePlayer(existing.id); nav.popBackStack() },
            onDismiss = { showDelete = false }
        )
    }
}
