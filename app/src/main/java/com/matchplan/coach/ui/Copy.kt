package com.matchplan.coach.ui

/** Central place for the required disclaimer / privacy strings. */
object Copy {
    const val PLANNING_DISCLAIMER =
        "MatchPlan Coach is a manual football match planning app for amateur " +
        "teams. Matches, players, lineups, tasks, and notes are created by the " +
        "user. The app is not an official football tool and does not provide " +
        "professional coaching advice."

    const val SCHEDULE_DISCLAIMER =
        "Match data is provided by football-data.org. Availability, accuracy, " +
        "competitions, and update frequency depend on the API provider and the " +
        "current API plan."

    const val SCHEDULE_SHORT_NOTE =
        "Match data is provided by football-data.org and may depend on your API plan."

    const val PRIVACY_NOTE =
        "MatchPlan Coach stores match plans, player lists, lineups, tasks, " +
        "notes, settings, and cached match data on this device. The app uses " +
        "internet only to load football match data from football-data.org. No " +
        "account, no ads, no analytics, no payments, no Firebase, no location, " +
        "no notifications, no sensors, no Google Fit, and no Health Connect."
}
