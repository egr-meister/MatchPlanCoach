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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.matchplan.coach.data.model.MatchPlan
import com.matchplan.coach.data.model.MatchStatus
import com.matchplan.coach.data.model.MatchType
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.AppTextField
import com.matchplan.coach.ui.components.ConfirmDialog
import com.matchplan.coach.ui.components.EnumDropdown
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.util.DateUtils

@Composable
fun AddEditMatchScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val existing = ScreenHelpers.matchById(data, matchId)
    val isEdit = existing != null

    var opponent by remember(existing) { mutableStateOf(existing?.opponent ?: "") }
    var matchDate by remember(existing) {
        mutableStateOf(existing?.matchDate ?: DateUtils.today())
    }
    var matchTime by remember(existing) { mutableStateOf(existing?.matchTime ?: "") }
    var venue by remember(existing) { mutableStateOf(existing?.venue ?: "") }
    var teamName by remember(existing) { mutableStateOf(existing?.teamName ?: "") }
    var notes by remember(existing) { mutableStateOf(existing?.preMatchNotes ?: "") }
    var matchType by remember(existing) {
        mutableStateOf(existing?.matchType ?: MatchType.Friendly)
    }
    var status by remember(existing) {
        mutableStateOf(existing?.status ?: MatchStatus.Planned)
    }

    var showValidation by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val opponentError = opponent.isBlank()
    val dateError = !DateUtils.isValidDate(matchDate)
    val timeError = !DateUtils.isValidTime(matchTime)

    PlannerScaffold(
        title = if (isEdit) "Edit Match" else "Add Match",
        onBack = { nav.popBackStack() },
        actions = {
            if (isEdit) {
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete match")
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
                value = opponent, onValueChange = { opponent = it },
                label = "Opponent *",
                isError = showValidation && opponentError,
                errorText = "Opponent must not be empty."
            )
            AppTextField(
                value = matchDate, onValueChange = { matchDate = it },
                label = "Match date (YYYY-MM-DD) *",
                placeholder = "2026-07-09",
                isError = showValidation && dateError,
                errorText = "Enter a valid date as YYYY-MM-DD."
            )
            AppTextField(
                value = matchTime, onValueChange = { matchTime = it },
                label = "Match time (HH:mm, optional)",
                placeholder = "18:30",
                isError = showValidation && timeError,
                errorText = "Time must be empty or a valid HH:mm."
            )
            AppTextField(
                value = venue, onValueChange = { venue = it },
                label = "Venue / location (optional)"
            )
            AppTextField(
                value = teamName, onValueChange = { teamName = it },
                label = "Your team name (optional)"
            )
            EnumDropdown(
                label = "Match type",
                options = MatchType.entries,
                selected = matchType,
                optionLabel = { it.label },
                onSelected = { matchType = it }
            )
            EnumDropdown(
                label = "Status",
                options = MatchStatus.entries,
                selected = status,
                optionLabel = { it.label },
                onSelected = { status = it }
            )
            AppTextField(
                value = notes, onValueChange = { notes = it },
                label = "General notes (optional)",
                singleLine = false, minLines = 3
            )

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    showValidation = true
                    if (!opponentError && !dateError && !timeError) {
                        val toSave = (existing ?: MatchPlan()).copy(
                            id = existing?.id ?: "",
                            opponent = opponent.trim(),
                            matchDate = matchDate.trim(),
                            matchTime = matchTime.trim(),
                            venue = venue.trim(),
                            teamName = teamName.trim(),
                            matchType = matchType,
                            status = status,
                            preMatchNotes = notes
                        )
                        vm.saveMatch(toSave) { id ->
                            if (isEdit) {
                                nav.popBackStack()
                            } else {
                                // Go straight to detail of the newly created match.
                                nav.popBackStack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(if (isEdit) "Save changes" else "Save match")
            }
            if (isEdit) {
                OutlinedButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete match") }
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteDialog && existing != null) {
        ConfirmDialog(
            title = "Delete match?",
            message = "This will remove the match plan, its squad, lineup and tasks. This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                showDeleteDialog = false
                vm.deleteMatch(existing.id)
                nav.popBackStack(com.matchplan.coach.ui.navigation.Routes.HOME, inclusive = false)
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}
