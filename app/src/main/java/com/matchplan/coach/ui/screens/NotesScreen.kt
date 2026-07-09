package com.matchplan.coach.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
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
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.AppTextField
import com.matchplan.coach.ui.components.MissingEntityFallback
import com.matchplan.coach.ui.components.PlannerScaffold

@Composable
fun NotesScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val match = ScreenHelpers.matchById(data, matchId)

    PlannerScaffold(title = "Match Notes", onBack = { nav.popBackStack() }) { padding ->
        if (match == null) {
            MissingEntityFallback("This match could not be found.") { nav.popBackStack() }
            return@PlannerScaffold
        }

        var pre by remember(match.id) { mutableStateOf(match.preMatchNotes) }
        var tactical by remember(match.id) { mutableStateOf(match.tacticalNotes) }
        var players by remember(match.id) { mutableStateOf(match.playerNotes) }
        var post by remember(match.id) { mutableStateOf(match.postMatchNotes) }
        var result by remember(match.id) { mutableStateOf(match.resultNotes) }

        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppTextField(pre, { pre = it }, "Pre-match notes",
                singleLine = false, minLines = 3)
            AppTextField(tactical, { tactical = it }, "Tactical notes",
                singleLine = false, minLines = 3)
            AppTextField(players, { players = it }, "Player notes",
                singleLine = false, minLines = 3)
            AppTextField(post, { post = it }, "Post-match notes",
                singleLine = false, minLines = 3)
            AppTextField(result, { result = it }, "Result notes",
                singleLine = false, minLines = 2)

            Spacer(Modifier.height(4.dp))
            Button(
                onClick = {
                    vm.saveNotes(match.id, pre, tactical, players, post, result)
                    nav.popBackStack()
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Save notes") }
            OutlinedButton(
                onClick = {
                    pre = ""; tactical = ""; players = ""; post = ""; result = ""
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Clear all notes") }
            Spacer(Modifier.height(24.dp))
        }
    }
}
