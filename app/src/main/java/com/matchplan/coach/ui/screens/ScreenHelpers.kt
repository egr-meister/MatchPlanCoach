package com.matchplan.coach.ui.screens

import com.matchplan.coach.data.model.AppData
import com.matchplan.coach.data.model.MatchPlan
import com.matchplan.coach.data.model.MatchStatus
import com.matchplan.coach.data.model.MatchTask
import com.matchplan.coach.data.model.Player
import java.text.SimpleDateFormat
import java.util.Locale

/** Shared, null-safe helpers for the UI layer. */
object ScreenHelpers {

    private val monthNames = arrayOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun",
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    /** Day number from YYYY-MM-DD, or "--" on bad input. */
    fun dayNumber(date: String): String =
        date.takeIf { it.length >= 10 }?.substring(8, 10) ?: "--"

    /** Short month from YYYY-MM-DD, or "" on bad input. */
    fun monthShort(date: String): String {
        return try {
            val month = date.substring(5, 7).toInt()
            monthNames.getOrElse(month - 1) { "" }
        } catch (e: Exception) {
            ""
        }
    }

    /** The soonest upcoming (Planned or Ready) match by date, or null. */
    fun nextMatch(data: AppData): MatchPlan? =
        data.matchPlans
            .filter { it.status == MatchStatus.Planned || it.status == MatchStatus.Ready }
            .sortedBy { it.matchDate }
            .firstOrNull()

    fun matchById(data: AppData, id: String?): MatchPlan? =
        if (id == null) null else data.matchPlans.firstOrNull { it.id == id }

    fun playerById(data: AppData, id: String?): Player? =
        if (id == null) null else data.players.firstOrNull { it.id == id }

    fun playerName(data: AppData, id: String?): String =
        playerById(data, id)?.name ?: "Unknown player"

    fun tasksFor(data: AppData, matchId: String): List<MatchTask> =
        data.tasks.filter { it.matchId == matchId }

    fun taskProgress(data: AppData, matchId: String): Pair<Int, Int> {
        val list = tasksFor(data, matchId)
        return list.count { it.completed } to list.size
    }

    fun squadCount(data: AppData, matchId: String): Int =
        data.squads.firstOrNull { it.matchId == matchId }?.selectedPlayerIds?.size ?: 0

    /** History sorted newest-created first. */
    fun matchesNewestFirst(data: AppData): List<MatchPlan> =
        data.matchPlans.sortedByDescending { it.createdAt.ifBlank { it.matchDate } }
}
