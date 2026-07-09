package com.matchplan.coach.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.matchplan.coach.data.local.DemoData
import com.matchplan.coach.data.model.AppData
import com.matchplan.coach.data.model.FormationType
import com.matchplan.coach.data.model.Formations
import com.matchplan.coach.data.model.MatchPlan
import com.matchplan.coach.data.model.MatchScheduleSettings
import com.matchplan.coach.data.model.MatchSource
import com.matchplan.coach.data.model.MatchSquad
import com.matchplan.coach.data.model.MatchTask
import com.matchplan.coach.data.model.NormalizedMatch
import com.matchplan.coach.data.model.Player
import com.matchplan.coach.data.model.Settings
import com.matchplan.coach.data.model.StartingLineup
import com.matchplan.coach.data.remote.FootballDataRepository
import com.matchplan.coach.data.repository.AppRepository
import com.matchplan.coach.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** UI state for the secondary Match Schedule screen. */
data class ScheduleUiState(
    val isLoading: Boolean = false,
    val matches: List<NormalizedMatch> = emptyList(),
    val source: MatchSource = MatchSource.Demo,
    val message: String = "",
    val lastUpdatedAt: String = "",
    val activeDateFrom: String = "",
    val activeDateTo: String = "",
    val isDefaultWindow: Boolean = true,
    val loadedOnce: Boolean = false
)

/**
 * Single shared ViewModel for the whole app. It exposes the persisted
 * [AppData] as a StateFlow and provides every mutating action, plus the
 * (isolated) Match Schedule loading logic. Actions never throw.
 */
class AppViewModel(
    private val repo: AppRepository,
    private val footballRepo: FootballDataRepository
) : ViewModel() {

    val appData: StateFlow<AppData> = repo.appData
        .stateIn(viewModelScope, SharingStarted.Eagerly, AppData())

    /** Becomes true after the first real emission from DataStore, so the UI can
     *  avoid flashing the onboarding screen for returning users. */
    val ready: StateFlow<Boolean> = repo.appData
        .map { true }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    private val _schedule = MutableStateFlow(ScheduleUiState())
    val schedule: StateFlow<ScheduleUiState> = _schedule.asStateFlow()

    fun hasApiToken(): Boolean = footballRepo.hasToken()

    // ----- Match plans ------------------------------------------------------
    fun saveMatch(match: MatchPlan, onSaved: (String) -> Unit = {}) = viewModelScope.launch {
        val id = repo.upsertMatch(match)
        onSaved(id)
    }

    fun deleteMatch(id: String) = viewModelScope.launch { repo.deleteMatch(id) }

    fun duplicateMatch(id: String, onDone: (String) -> Unit = {}) = viewModelScope.launch {
        val newId = repo.duplicateMatch(id)
        onDone(newId)
    }

    // ----- Players ----------------------------------------------------------
    fun savePlayer(player: Player, onSaved: (String) -> Unit = {}) = viewModelScope.launch {
        val id = repo.upsertPlayer(player)
        onSaved(id)
    }

    fun deletePlayer(id: String) = viewModelScope.launch { repo.deletePlayer(id) }

    // ----- Squad ------------------------------------------------------------
    fun saveSquad(squad: MatchSquad) = viewModelScope.launch { repo.saveSquad(squad) }

    // ----- Lineup -----------------------------------------------------------
    fun saveLineup(lineup: StartingLineup) = viewModelScope.launch { repo.saveLineup(lineup) }

    // ----- Tasks ------------------------------------------------------------
    fun saveTask(task: MatchTask) = viewModelScope.launch { repo.upsertTask(task) }
    fun toggleTask(id: String) = viewModelScope.launch { repo.toggleTask(id) }
    fun deleteTask(id: String) = viewModelScope.launch { repo.deleteTask(id) }

    // ----- Notes ------------------------------------------------------------
    fun saveNotes(
        matchId: String, pre: String, tactical: String, players: String,
        post: String, result: String
    ) = viewModelScope.launch {
        repo.saveNotes(matchId, pre, tactical, players, post, result)
    }

    // ----- Settings ---------------------------------------------------------
    fun completeOnboarding() = viewModelScope.launch { repo.setOnboardingCompleted(true) }
    fun showOnboardingAgain() = viewModelScope.launch { repo.setOnboardingCompleted(false) }
    fun setCompactMode(enabled: Boolean) = viewModelScope.launch { repo.setCompactMode(enabled) }
    fun updateSettings(settings: Settings) = viewModelScope.launch { repo.updateSettings(settings) }
    fun updateScheduleSettings(s: MatchScheduleSettings) =
        viewModelScope.launch { repo.updateScheduleSettings(s) }

    // ----- Destructive actions ---------------------------------------------
    fun deleteAllMatches() = viewModelScope.launch { repo.deleteAllMatches() }
    fun deleteAllPlayers() = viewModelScope.launch { repo.deleteAllPlayers() }
    fun deleteAllTasks() = viewModelScope.launch { repo.deleteAllTasks() }
    fun clearScheduleCache() = viewModelScope.launch {
        repo.clearScheduleCache()
        _schedule.value = ScheduleUiState()
    }
    fun resetAll() = viewModelScope.launch {
        repo.resetAll()
        _schedule.value = ScheduleUiState()
    }

    // ----- Helpers to create defaults --------------------------------------
    fun freshLineupFor(matchId: String, formation: FormationType): StartingLineup =
        Formations.newLineup(matchId, formation)

    // ----- Match Schedule (secondary API feature) ---------------------------

    /**
     * Called when the Match Schedule screen opens. Shows cached matches first
     * if present; otherwise loads once (demo or API depending on settings).
     */
    fun loadScheduleIfNeeded() = viewModelScope.launch {
        if (_schedule.value.loadedOnce) return@launch
        val data = repo.appData.first()
        val cache = data.matchScheduleCache
        val settings = data.settings.matchSchedule
        val window = resolveWindow(settings)

        if (cache.cachedMatches.isNotEmpty()) {
            _schedule.value = ScheduleUiState(
                isLoading = false,
                matches = cache.cachedMatches,
                source = cache.cachedMatches.firstOrNull()?.source ?: MatchSource.Cache,
                message = cache.lastError,
                lastUpdatedAt = cache.lastUpdatedAt,
                activeDateFrom = window.first,
                activeDateTo = window.second,
                isDefaultWindow = isDefaultWindow(settings),
                loadedOnce = true
            )
            return@launch
        }

        // No cache yet.
        if (!settings.apiEnabled || settings.useDemoData || !footballRepo.hasToken()) {
            val demo = DemoData.demoMatches()
            _schedule.value = ScheduleUiState(
                matches = demo,
                source = MatchSource.Demo,
                message = if (!footballRepo.hasToken())
                    "API token is not configured. Showing demo matches."
                else "Showing demo matches.",
                lastUpdatedAt = DateUtils.nowTimestamp(),
                activeDateFrom = window.first,
                activeDateTo = window.second,
                isDefaultWindow = isDefaultWindow(settings),
                loadedOnce = true
            )
        } else {
            refreshSchedule()
        }
    }

    /** Manual refresh button. Uses the current settings window. */
    fun refreshSchedule() = viewModelScope.launch {
        val data = repo.appData.first()
        val settings = data.settings.matchSchedule
        val window = resolveWindow(settings)
        val prev = _schedule.value

        _schedule.value = prev.copy(
            isLoading = true,
            activeDateFrom = window.first,
            activeDateTo = window.second,
            isDefaultWindow = isDefaultWindow(settings)
        )

        // Demo mode / API disabled -> just show demo data (no network call).
        if (!settings.apiEnabled || settings.useDemoData) {
            val demo = DemoData.demoMatches()
            repo.saveScheduleCache(demo, window.first, window.second, "Showing demo matches.")
            _schedule.value = ScheduleUiState(
                matches = demo, source = MatchSource.Demo,
                message = "Showing demo matches.",
                lastUpdatedAt = DateUtils.nowTimestamp(),
                activeDateFrom = window.first, activeDateTo = window.second,
                isDefaultWindow = isDefaultWindow(settings), loadedOnce = true
            )
            return@launch
        }

        val result = footballRepo.fetchMatches(
            window.first, window.second, settings.competitionCode
        )

        if (result.ok) {
            repo.saveScheduleCache(result.matches, window.first, window.second, result.error)
            _schedule.value = ScheduleUiState(
                matches = result.matches,
                source = result.source,
                message = result.error,
                lastUpdatedAt = DateUtils.nowTimestamp(),
                activeDateFrom = window.first, activeDateTo = window.second,
                isDefaultWindow = isDefaultWindow(settings), loadedOnce = true
            )
        } else {
            // Failure: fall back to cache, then demo.
            val cache = data.matchScheduleCache
            if (cache.cachedMatches.isNotEmpty()) {
                repo.saveScheduleError(result.error)
                _schedule.value = ScheduleUiState(
                    matches = cache.cachedMatches, source = MatchSource.Cache,
                    message = result.error + " Showing cached matches.",
                    lastUpdatedAt = cache.lastUpdatedAt,
                    activeDateFrom = window.first, activeDateTo = window.second,
                    isDefaultWindow = isDefaultWindow(settings), loadedOnce = true
                )
            } else {
                val demo = DemoData.demoMatches()
                _schedule.value = ScheduleUiState(
                    matches = demo, source = MatchSource.Demo,
                    message = result.error + " Showing demo matches.",
                    lastUpdatedAt = DateUtils.nowTimestamp(),
                    activeDateFrom = window.first, activeDateTo = window.second,
                    isDefaultWindow = isDefaultWindow(settings), loadedOnce = true
                )
            }
        }
    }

    /** Force a reload after settings change. */
    fun reloadScheduleAfterSettingsChange() {
        _schedule.value = _schedule.value.copy(loadedOnce = false)
        loadScheduleIfNeeded()
    }

    private fun resolveWindow(settings: MatchScheduleSettings): Pair<String, String> {
        val from = settings.dateFrom.takeIf { DateUtils.isValidDate(it) } ?: DateUtils.today()
        val to = settings.dateTo.takeIf { DateUtils.isValidDate(it) } ?: DateUtils.todayPlusDays(9)
        // Guard against reversed range.
        return if (DateUtils.isRangeOrdered(from, to)) from to to
        else DateUtils.today() to DateUtils.todayPlusDays(9)
    }

    private fun isDefaultWindow(settings: MatchScheduleSettings): Boolean =
        settings.dateFrom.isBlank() && settings.dateTo.isBlank()

    class Factory(
        private val repo: AppRepository,
        private val footballRepo: FootballDataRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AppViewModel(repo, footballRepo) as T
        }
    }
}
