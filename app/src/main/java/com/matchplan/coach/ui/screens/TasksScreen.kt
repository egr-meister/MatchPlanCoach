package com.matchplan.coach.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.matchplan.coach.data.model.MatchTask
import com.matchplan.coach.data.model.TaskCategory
import com.matchplan.coach.ui.AppViewModel
import com.matchplan.coach.ui.components.AppTextField
import com.matchplan.coach.ui.components.EmptyState
import com.matchplan.coach.ui.components.EnumDropdown
import com.matchplan.coach.ui.components.MissingEntityFallback
import com.matchplan.coach.ui.components.PlannerScaffold
import com.matchplan.coach.ui.components.StatusChip
import com.matchplan.coach.ui.theme.FieldGreen

@Composable
fun TasksScreen(vm: AppViewModel, nav: NavController, matchId: String?) {
    val data by vm.appData.collectAsState()
    val match = ScreenHelpers.matchById(data, matchId)
    var editing by remember { mutableStateOf<MatchTask?>(null) }
    var showDialog by remember { mutableStateOf(false) }

    PlannerScaffold(
        title = "Match Tasks",
        onBack = { nav.popBackStack() },
        floatingActionButton = {
            if (match != null) {
                FloatingActionButton(onClick = { editing = null; showDialog = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add task")
                }
            }
        }
    ) { padding ->
        if (match == null) {
            MissingEntityFallback("This match could not be found.") { nav.popBackStack() }
            return@PlannerScaffold
        }
        val tasks = ScreenHelpers.tasksFor(data, match.id)
        val done = tasks.count { it.completed }

        if (tasks.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                EmptyState("No match tasks yet.", "Add tasks to prepare your match.")
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text("Progress: $done / ${tasks.size}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { if (tasks.isEmpty()) 0f else done.toFloat() / tasks.size },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                }
                items(tasks, key = { it.id }) { task ->
                    TaskRow(
                        task = task,
                        onToggle = { vm.toggleTask(task.id) },
                        onEdit = { editing = task; showDialog = true },
                        onDelete = { vm.deleteTask(task.id) }
                    )
                }
            }
        }
    }

    if (showDialog && match != null) {
        TaskDialog(
            initial = editing,
            onDismiss = { showDialog = false },
            onSave = { title, category, note ->
                val toSave = (editing ?: MatchTask()).copy(
                    id = editing?.id ?: "",
                    matchId = match.id,
                    title = title,
                    category = category,
                    note = note
                )
                vm.saveTask(toSave)
                showDialog = false
            }
        )
    }
}

@Composable
private fun TaskRow(
    task: MatchTask,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.completed, onCheckedChange = { onToggle() })
            Column(Modifier.weight(1f).clickable { onEdit() }) {
                Text(
                    task.title.ifBlank { "Untitled task" },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (task.completed) TextDecoration.LineThrough else null
                )
                if (task.note.isNotBlank()) {
                    Text(task.note.take(60),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(4.dp))
                StatusChip(task.category.label, FieldGreen)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun TaskDialog(
    initial: MatchTask?,
    onDismiss: () -> Unit,
    onSave: (String, TaskCategory, String) -> Unit
) {
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var category by remember { mutableStateOf(initial?.category ?: TaskCategory.Custom) }
    var showError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add task" else "Edit task") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                AppTextField(
                    value = title, onValueChange = { title = it },
                    label = "Task title *",
                    isError = showError && title.isBlank(),
                    errorText = "Task title must not be empty."
                )
                EnumDropdown(
                    label = "Category",
                    options = TaskCategory.entries,
                    selected = category,
                    optionLabel = { it.label },
                    onSelected = { category = it }
                )
                AppTextField(
                    value = note, onValueChange = { note = it },
                    label = "Note (optional)", singleLine = false, minLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                showError = true
                if (title.isNotBlank()) onSave(title.trim(), category, note)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
