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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import com.matchplan.coach.data.model.MatchScheduleSettings
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.Copy
import com.matchplan.coach.ui.components.AppTextField
import com.matchplan.coach.ui.components.InfoBanner
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.ui.components.SectionCard
import com.matchplan.coach.util.DateUtils

@Composable
fun ScheduleSettingsScreen(vm: AppViewModel, nav: NavController) {
    val data by vm.appData.collectAsState()
    val current = data.settings.matchSchedule

    var apiEnabled by remember(current) { mutableStateOf(current.apiEnabled) }
    var useDemo by remember(current) { mutableStateOf(current.useDemoData) }
    var dateFrom by remember(current) { mutableStateOf(current.dateFrom) }
    var dateTo by remember(current) { mutableStateOf(current.dateTo) }
    var competitionCode by remember(current) { mutableStateOf(current.competitionCode) }
    var showValidation by remember { mutableStateOf(false) }

    val fromError = dateFrom.isNotBlank() && !DateUtils.isValidDate(dateFrom)
    val toError = dateTo.isNotBlank() && !DateUtils.isValidDate(dateTo)
    val orderError = !fromError && !toError && dateFrom.isNotBlank() && dateTo.isNotBlank() &&
        !DateUtils.isRangeOrdered(dateFrom, dateTo)
    val hasError = fromError || toError || orderError

    PlannerScaffold(title = "Match Schedule Settings", onBack = { nav.popBackStack() }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            InfoBanner(Copy.SCHEDULE_DISCLAIMER)

            SectionCard {
                ToggleRow("Match Schedule API enabled", apiEnabled) { apiEnabled = it }
                Spacer(Modifier.height(6.dp))
                ToggleRow("Use demo data", useDemo) { useDemo = it }
                Spacer(Modifier.height(4.dp))
                Text(
                    "API token detected: ${if (vm.hasApiToken()) "Yes" else "No (demo data will be used)"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            SectionCard {
                Text("Date window", style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold)
                Text("Leave both empty to use the default: today through today + 9 days.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(8.dp))
                AppTextField(dateFrom, { dateFrom = it }, "Date from (YYYY-MM-DD, optional)",
                    placeholder = DateUtils.today(),
                    isError = showValidation && fromError,
                    errorText = "Enter a valid date as YYYY-MM-DD.")
                Spacer(Modifier.height(8.dp))
                AppTextField(dateTo, { dateTo = it }, "Date to (YYYY-MM-DD, optional)",
                    placeholder = DateUtils.todayPlusDays(9),
                    isError = showValidation && (toError || orderError),
                    errorText = if (orderError) "Date to must not be earlier than date from."
                        else "Enter a valid date as YYYY-MM-DD.")
                Spacer(Modifier.height(8.dp))
                AppTextField(competitionCode, { competitionCode = it },
                    "Competition code filter (optional)",
                    placeholder = "e.g. PL, PD, BL1")
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { dateFrom = ""; dateTo = ""; showValidation = false },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset to default 10-day window") }
            }

            Button(
                onClick = {
                    showValidation = true
                    if (!hasError) {
                        vm.updateScheduleSettings(
                            MatchScheduleSettings(
                                apiEnabled = apiEnabled,
                                useDemoData = useDemo,
                                dateFrom = dateFrom.trim(),
                                dateTo = dateTo.trim(),
                                competitionCode = competitionCode.trim()
                            )
                        )
                        vm.reloadScheduleAfterSettingsChange()
                        nav.popBackStack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) { Text("Save settings") }

            OutlinedButton(
                onClick = { vm.clearScheduleCache() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Clear match cache") }

            Text(
                "API availability may depend on the selected competitions, the date " +
                    "range, and your current football-data.org API plan.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(checked = checked, onCheckedChange = onChange)
    }
}
