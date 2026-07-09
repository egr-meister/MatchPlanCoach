package com.matchplan.coach.data.model

import kotlinx.serialization.Serializable

/**
 * All persisted data models for MatchPlan Coach.
 *
 * Every class is annotated with [Serializable] so it can be stored as a JSON
 * string inside DataStore Preferences. All fields have safe defaults so that
 * decoding partial / older / corrupted JSON never crashes the app.
 */

// ---------------------------------------------------------------------------
// Enums
// ---------------------------------------------------------------------------

@Serializable
enum class MatchType(val label: String) {
    Friendly("Friendly"),
    League("League"),
    Cup("Cup"),
    Training("Training"),
    Custom("Custom");

    companion object {
        fun fromNameSafe(name: String?): MatchType =
            entries.firstOrNull { it.name == name } ?: Friendly
    }
}

@Serializable
enum class MatchStatus(val label: String) {
    Planned("Planned"),
    Ready("Ready"),
    Completed("Completed"),
    Cancelled("Cancelled");

    companion object {
        fun fromNameSafe(name: String?): MatchStatus =
            entries.firstOrNull { it.name == name } ?: Planned
    }
}

@Serializable
enum class PlayerPosition(val label: String) {
    Goalkeeper("Goalkeeper"),
    Defender("Defender"),
    Midfielder("Midfielder"),
    Forward("Forward"),
    Custom("Custom");

    companion object {
        fun fromNameSafe(name: String?): PlayerPosition =
            entries.firstOrNull { it.name == name } ?: Midfielder
    }
}

@Serializable
enum class PlayerAvailability(val label: String) {
    Available("Available"),
    Doubtful("Doubtful"),
    Unavailable("Unavailable"),
    Unknown("Unknown");

    companion object {
        fun fromNameSafe(name: String?): PlayerAvailability =
            entries.firstOrNull { it.name == name } ?: Unknown
    }
}

@Serializable
enum class TaskCategory(val label: String) {
    Attack("Attack"),
    Defense("Defense"),
    Pressing("Pressing"),
    SetPieces("Set Pieces"),
    Fitness("Fitness"),
    TeamTalk("Team Talk"),
    Equipment("Equipment"),
    Custom("Custom");

    companion object {
        fun fromNameSafe(name: String?): TaskCategory =
            entries.firstOrNull { it.name == name } ?: Custom
    }
}

@Serializable
enum class FormationType(val label: String) {
    FourFourTwo("4-4-2"),
    FourThreeThree("4-3-3"),
    ThreeFiveTwo("3-5-2"),
    FiveThreeTwo("5-3-2"),
    FourTwoThreeOne("4-2-3-1");

    companion object {
        fun fromNameSafe(name: String?): FormationType =
            entries.firstOrNull { it.name == name } ?: FourFourTwo
    }
}

@Serializable
enum class MatchSource(val label: String) {
    Api("Live"),
    Cache("Cached"),
    Demo("Demo");

    companion object {
        fun fromNameSafe(name: String?): MatchSource =
            entries.firstOrNull { it.name == name } ?: Demo
    }
}

// ---------------------------------------------------------------------------
// Core planning models
// ---------------------------------------------------------------------------

@Serializable
data class MatchPlan(
    val id: String = "",
    val opponent: String = "",
    val matchDate: String = "",   // YYYY-MM-DD
    val matchTime: String = "",   // HH:mm (may be empty)
    val venue: String = "",
    val matchType: MatchType = MatchType.Friendly,
    val status: MatchStatus = MatchStatus.Planned,
    val teamName: String = "",
    val preMatchNotes: String = "",
    val tacticalNotes: String = "",
    val playerNotes: String = "",
    val postMatchNotes: String = "",
    val resultNotes: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class Player(
    val id: String = "",
    val name: String = "",
    val number: Int? = null,
    val position: PlayerPosition = PlayerPosition.Midfielder,
    val availability: PlayerAvailability = PlayerAvailability.Unknown,
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class MatchSquad(
    val matchId: String = "",
    val selectedPlayerIds: List<String> = emptyList(),
    val startingLineupPlayerIds: List<String> = emptyList(),
    val benchPlayerIds: List<String> = emptyList(),
    val unavailablePlayerIds: List<String> = emptyList()
)

@Serializable
data class FormationSlot(
    val slotId: String = "",
    val label: String = "",
    val line: String = "",
    val x: Float = 0.5f,
    val y: Float = 0.5f,
    val assignedPlayerId: String? = null
)

@Serializable
data class StartingLineup(
    val matchId: String = "",
    val formation: FormationType = FormationType.FourFourTwo,
    val slots: List<FormationSlot> = emptyList()
)

@Serializable
data class MatchTask(
    val id: String = "",
    val matchId: String = "",
    val title: String = "",
    val category: TaskCategory = TaskCategory.Custom,
    val completed: Boolean = false,
    val note: String = "",
    val createdAt: String = "",
    val updatedAt: String = ""
)

// ---------------------------------------------------------------------------
// Match Schedule (secondary feature) models
// ---------------------------------------------------------------------------

@Serializable
data class NormalizedMatch(
    val id: String = "",
    val utcDate: String = "",
    val date: String = "",
    val time: String = "",
    val competitionName: String = "",
    val competitionCode: String = "",
    val homeTeam: String = "Unknown",
    val awayTeam: String = "Unknown",
    val status: String = "",
    val homeScore: Int? = null,
    val awayScore: Int? = null,
    val winner: String = "",
    val source: MatchSource = MatchSource.Demo
)

@Serializable
data class MatchScheduleSettings(
    val apiEnabled: Boolean = true,
    val useDemoData: Boolean = false,
    val dateFrom: String = "",
    val dateTo: String = "",
    val competitionCode: String = ""
)

@Serializable
data class MatchScheduleCache(
    val cachedMatches: List<NormalizedMatch> = emptyList(),
    val lastUpdatedAt: String = "",
    val lastError: String = "",
    val lastDateFrom: String = "",
    val lastDateTo: String = ""
)

@Serializable
data class Settings(
    val onboardingCompleted: Boolean = false,
    val compactMode: Boolean = false,
    val matchSchedule: MatchScheduleSettings = MatchScheduleSettings()
)

@Serializable
data class AppData(
    val matchPlans: List<MatchPlan> = emptyList(),
    val players: List<Player> = emptyList(),
    val squads: List<MatchSquad> = emptyList(),
    val startingLineups: List<StartingLineup> = emptyList(),
    val tasks: List<MatchTask> = emptyList(),
    val settings: Settings = Settings(),
    val matchScheduleCache: MatchScheduleCache = MatchScheduleCache()
)

/** Result object returned by the football-data.org repository. Never throws. */
data class FootballApiResult(
    val ok: Boolean,
    val matches: List<NormalizedMatch>,
    val error: String,
    val usedDemoData: Boolean,
    val source: MatchSource
)
