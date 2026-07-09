package com.matchplan.coach.ui.navigation

/**
 * Central place for navigation routes. Screens that need a matchId use a path
 * argument. Navigation is written so that missing / invalid arguments simply
 * show a friendly fallback or pop back — never crash.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"

    const val ADD_MATCH = "addMatch"                       // create new
    const val EDIT_MATCH = "editMatch/{matchId}"           // edit existing
    fun editMatch(matchId: String) = "editMatch/$matchId"

    const val MATCH_DETAIL = "matchDetail/{matchId}"
    fun matchDetail(matchId: String) = "matchDetail/$matchId"

    const val PLAYER_LIST = "playerList"
    const val ADD_PLAYER = "addPlayer"
    const val EDIT_PLAYER = "editPlayer/{playerId}"
    fun editPlayer(playerId: String) = "editPlayer/$playerId"

    const val SQUAD = "squad/{matchId}"
    fun squad(matchId: String) = "squad/$matchId"

    const val LINEUP = "lineup/{matchId}"
    fun lineup(matchId: String) = "lineup/$matchId"

    const val TASKS = "tasks/{matchId}"
    fun tasks(matchId: String) = "tasks/$matchId"

    const val NOTES = "notes/{matchId}"
    fun notes(matchId: String) = "notes/$matchId"

    const val HISTORY = "history"
    const val SCHEDULE = "schedule"
    const val SCHEDULE_SETTINGS = "scheduleSettings"
    const val SETTINGS = "settings"

    const val ARG_MATCH_ID = "matchId"
    const val ARG_PLAYER_ID = "playerId"
}
