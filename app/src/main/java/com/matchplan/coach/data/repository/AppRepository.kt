package com.matchplan.coach.data.repository

import com.matchplan.coach.data.local.AppDataStore
import com.matchplan.coach.data.model.AppData
import com.matchplan.coach.data.model.FormationType
import com.matchplan.coach.data.model.Formations
import com.matchplan.coach.data.model.MatchPlan
import com.matchplan.coach.data.model.MatchScheduleCache
import com.matchplan.coach.data.model.MatchScheduleSettings
import com.matchplan.coach.data.model.MatchSquad
import com.matchplan.coach.data.model.MatchStatus
import com.matchplan.coach.data.model.MatchTask
import com.matchplan.coach.data.model.NormalizedMatch
import com.matchplan.coach.data.model.Player
import com.matchplan.coach.data.model.Settings
import com.matchplan.coach.data.model.StartingLineup
import com.matchplan.coach.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.util.UUID

/**
 * The one repository for all LOCAL app data. It owns the CRUD logic for match
 * plans, players, squads, lineups, tasks and settings, plus the match schedule
 * cache. Every operation is defensive and never crashes on missing entities.
 */
class AppRepository(private val store: AppDataStore) {

    val appData: Flow<AppData> = store.appDataFlow

    private fun newId(): String = UUID.randomUUID().toString()
    private fun now(): String = DateUtils.nowTimestamp()

    // -----------------------------------------------------------------------
    // Match plans
    // -----------------------------------------------------------------------

    suspend fun upsertMatch(match: MatchPlan): String {
        var resultId = match.id
        store.update { data ->
            val existing = data.matchPlans.firstOrNull { it.id == match.id }
            if (existing == null || match.id.isBlank()) {
                val id = match.id.ifBlank { newId() }
                resultId = id
                val toAdd = match.copy(
                    id = id,
                    createdAt = now(),
                    updatedAt = now()
                )
                data.copy(matchPlans = data.matchPlans + toAdd)
            } else {
                resultId = match.id
                val updated = match.copy(
                    createdAt = existing.createdAt.ifBlank { now() },
                    updatedAt = now()
                )
                data.copy(matchPlans = data.matchPlans.map {
                    if (it.id == match.id) updated else it
                })
            }
        }
        return resultId
    }

    suspend fun deleteMatch(matchId: String) {
        store.update { data ->
            data.copy(
                matchPlans = data.matchPlans.filterNot { it.id == matchId },
                squads = data.squads.filterNot { it.matchId == matchId },
                startingLineups = data.startingLineups.filterNot { it.matchId == matchId },
                tasks = data.tasks.filterNot { it.matchId == matchId }
            )
        }
    }

    /** Duplicate a match plan (with its squad, lineup and tasks) as a new Planned match. */
    suspend fun duplicateMatch(matchId: String): String {
        var newMatchId = matchId
        store.update { data ->
            val src = data.matchPlans.firstOrNull { it.id == matchId } ?: return@update data
            val copyId = newId()
            newMatchId = copyId
            val copyMatch = src.copy(
                id = copyId,
                opponent = src.opponent + " (copy)",
                status = MatchStatus.Planned,
                postMatchNotes = "",
                resultNotes = "",
                createdAt = now(),
                updatedAt = now()
            )
            val srcSquad = data.squads.firstOrNull { it.matchId == matchId }
            val newSquad = srcSquad?.copy(matchId = copyId)
            val srcLineup = data.startingLineups.firstOrNull { it.matchId == matchId }
            val newLineup = srcLineup?.copy(matchId = copyId)
            val newTasks = data.tasks.filter { it.matchId == matchId }.map {
                it.copy(id = newId(), matchId = copyId, completed = false,
                    createdAt = now(), updatedAt = now())
            }
            data.copy(
                matchPlans = data.matchPlans + copyMatch,
                squads = if (newSquad != null) data.squads + newSquad else data.squads,
                startingLineups = if (newLineup != null) data.startingLineups + newLineup else data.startingLineups,
                tasks = data.tasks + newTasks
            )
        }
        return newMatchId
    }

    suspend fun deleteAllMatches() {
        store.update { data ->
            data.copy(
                matchPlans = emptyList(),
                squads = emptyList(),
                startingLineups = emptyList(),
                tasks = emptyList()
            )
        }
    }

    // -----------------------------------------------------------------------
    // Players
    // -----------------------------------------------------------------------

    suspend fun upsertPlayer(player: Player): String {
        var resultId = player.id
        store.update { data ->
            val existing = data.players.firstOrNull { it.id == player.id }
            if (existing == null || player.id.isBlank()) {
                val id = player.id.ifBlank { newId() }
                resultId = id
                data.copy(players = data.players + player.copy(
                    id = id, createdAt = now(), updatedAt = now()
                ))
            } else {
                resultId = player.id
                data.copy(players = data.players.map {
                    if (it.id == player.id) player.copy(
                        createdAt = existing.createdAt.ifBlank { now() },
                        updatedAt = now()
                    ) else it
                })
            }
        }
        return resultId
    }

    suspend fun deletePlayer(playerId: String) {
        store.update { data ->
            data.copy(
                players = data.players.filterNot { it.id == playerId },
                // Remove the player from any squad / lineup references.
                squads = data.squads.map { sq ->
                    sq.copy(
                        selectedPlayerIds = sq.selectedPlayerIds - playerId,
                        startingLineupPlayerIds = sq.startingLineupPlayerIds - playerId,
                        benchPlayerIds = sq.benchPlayerIds - playerId,
                        unavailablePlayerIds = sq.unavailablePlayerIds - playerId
                    )
                },
                startingLineups = data.startingLineups.map { lu ->
                    lu.copy(slots = lu.slots.map { slot ->
                        if (slot.assignedPlayerId == playerId) slot.copy(assignedPlayerId = null)
                        else slot
                    })
                }
            )
        }
    }

    suspend fun deleteAllPlayers() {
        store.update { data ->
            data.copy(
                players = emptyList(),
                squads = data.squads.map {
                    it.copy(
                        selectedPlayerIds = emptyList(),
                        startingLineupPlayerIds = emptyList(),
                        benchPlayerIds = emptyList(),
                        unavailablePlayerIds = emptyList()
                    )
                },
                startingLineups = data.startingLineups.map { lu ->
                    lu.copy(slots = lu.slots.map { it.copy(assignedPlayerId = null) })
                }
            )
        }
    }

    // -----------------------------------------------------------------------
    // Squad
    // -----------------------------------------------------------------------

    suspend fun saveSquad(squad: MatchSquad) {
        store.update { data ->
            val exists = data.squads.any { it.matchId == squad.matchId }
            val squads = if (exists) {
                data.squads.map { if (it.matchId == squad.matchId) squad else it }
            } else {
                data.squads + squad
            }
            data.copy(squads = squads)
        }
    }

    // -----------------------------------------------------------------------
    // Starting lineup
    // -----------------------------------------------------------------------

    suspend fun saveLineup(lineup: StartingLineup) {
        store.update { data ->
            val exists = data.startingLineups.any { it.matchId == lineup.matchId }
            val lineups = if (exists) {
                data.startingLineups.map { if (it.matchId == lineup.matchId) lineup else it }
            } else {
                data.startingLineups + lineup
            }
            data.copy(startingLineups = lineups)
        }
    }

    // -----------------------------------------------------------------------
    // Tasks
    // -----------------------------------------------------------------------

    suspend fun upsertTask(task: MatchTask): String {
        var resultId = task.id
        store.update { data ->
            val existing = data.tasks.firstOrNull { it.id == task.id }
            if (existing == null || task.id.isBlank()) {
                val id = task.id.ifBlank { newId() }
                resultId = id
                data.copy(tasks = data.tasks + task.copy(
                    id = id, createdAt = now(), updatedAt = now()
                ))
            } else {
                resultId = task.id
                data.copy(tasks = data.tasks.map {
                    if (it.id == task.id) task.copy(
                        createdAt = existing.createdAt.ifBlank { now() },
                        updatedAt = now()
                    ) else it
                })
            }
        }
        return resultId
    }

    suspend fun toggleTask(taskId: String) {
        store.update { data ->
            data.copy(tasks = data.tasks.map {
                if (it.id == taskId) it.copy(completed = !it.completed, updatedAt = now()) else it
            })
        }
    }

    suspend fun deleteTask(taskId: String) {
        store.update { data ->
            data.copy(tasks = data.tasks.filterNot { it.id == taskId })
        }
    }

    suspend fun deleteAllTasks() {
        store.update { data -> data.copy(tasks = emptyList()) }
    }

    // -----------------------------------------------------------------------
    // Notes (stored on the MatchPlan itself)
    // -----------------------------------------------------------------------

    suspend fun saveNotes(
        matchId: String,
        preMatchNotes: String,
        tacticalNotes: String,
        playerNotes: String,
        postMatchNotes: String,
        resultNotes: String
    ) {
        store.update { data ->
            data.copy(matchPlans = data.matchPlans.map {
                if (it.id == matchId) it.copy(
                    preMatchNotes = preMatchNotes,
                    tacticalNotes = tacticalNotes,
                    playerNotes = playerNotes,
                    postMatchNotes = postMatchNotes,
                    resultNotes = resultNotes,
                    updatedAt = now()
                ) else it
            })
        }
    }

    // -----------------------------------------------------------------------
    // Settings
    // -----------------------------------------------------------------------

    suspend fun setOnboardingCompleted(done: Boolean) {
        store.update { data ->
            data.copy(settings = data.settings.copy(onboardingCompleted = done))
        }
    }

    suspend fun setCompactMode(enabled: Boolean) {
        store.update { data ->
            data.copy(settings = data.settings.copy(compactMode = enabled))
        }
    }

    suspend fun updateSettings(settings: Settings) {
        store.update { data -> data.copy(settings = settings) }
    }

    suspend fun updateScheduleSettings(scheduleSettings: MatchScheduleSettings) {
        store.update { data ->
            data.copy(settings = data.settings.copy(matchSchedule = scheduleSettings))
        }
    }

    // -----------------------------------------------------------------------
    // Match schedule cache
    // -----------------------------------------------------------------------

    suspend fun saveScheduleCache(
        matches: List<NormalizedMatch>,
        dateFrom: String,
        dateTo: String,
        error: String = ""
    ) {
        store.update { data ->
            data.copy(matchScheduleCache = MatchScheduleCache(
                cachedMatches = matches,
                lastUpdatedAt = now(),
                lastError = error,
                lastDateFrom = dateFrom,
                lastDateTo = dateTo
            ))
        }
    }

    suspend fun saveScheduleError(error: String) {
        store.update { data ->
            data.copy(matchScheduleCache = data.matchScheduleCache.copy(lastError = error))
        }
    }

    suspend fun clearScheduleCache() {
        store.update { data -> data.copy(matchScheduleCache = MatchScheduleCache()) }
    }

    // -----------------------------------------------------------------------
    // Reset everything
    // -----------------------------------------------------------------------

    suspend fun resetAll() {
        store.clearAll()
    }

    companion object {
        /** Helper to build a fresh empty lineup for a match. */
        fun freshLineup(matchId: String, formation: FormationType): StartingLineup =
            Formations.newLineup(matchId, formation)
    }
}
