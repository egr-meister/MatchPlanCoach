package com.matchplan.coach.data.local

import com.matchplan.coach.data.model.MatchSource
import com.matchplan.coach.data.model.NormalizedMatch
import com.matchplan.coach.util.DateUtils

/**
 * Built-in demo match schedule shown when there is no API token (or the user
 * turned demo mode on, or a request failed with no cache). Team names are
 * generic placeholders — no official clubs, logos or real players.
 */
object DemoData {

    fun demoMatches(): List<NormalizedMatch> {
        val d0 = DateUtils.today()
        val d1 = DateUtils.todayPlusDays(1)
        val d2 = DateUtils.todayPlusDays(2)
        val d4 = DateUtils.todayPlusDays(4)
        val d6 = DateUtils.todayPlusDays(6)
        val d8 = DateUtils.todayPlusDays(8)

        fun m(
            id: String, date: String, time: String, comp: String, code: String,
            home: String, away: String, status: String,
            hs: Int? = null, aws: Int? = null, winner: String = ""
        ) = NormalizedMatch(
            id = id,
            utcDate = "${date}T${time}:00Z",
            date = date,
            time = time,
            competitionName = comp,
            competitionCode = code,
            homeTeam = home,
            awayTeam = away,
            status = status,
            homeScore = hs,
            awayScore = aws,
            winner = winner,
            source = MatchSource.Demo
        )

        return listOf(
            m("demo-1", d0, "18:00", "Demo League", "DL", "Riverside United", "Hillcrest Rovers", "SCHEDULED"),
            m("demo-2", d0, "20:30", "Demo League", "DL", "Meadow Town", "Old Bridge FC", "SCHEDULED"),
            m("demo-3", d1, "15:00", "Demo Cup", "DC", "Parkside Athletic", "Lakeview City", "SCHEDULED"),
            m("demo-4", d2, "19:45", "Demo League", "DL", "Northgate FC", "Southfield Wanderers", "SCHEDULED"),
            m("demo-5", d4, "16:00", "Demo Cup", "DC", "Greenwood FC", "Harbour Town", "SCHEDULED"),
            m("demo-6", d6, "14:30", "Demo League", "DL", "Ashford Rangers", "Brookside FC", "SCHEDULED"),
            m("demo-7", d8, "17:15", "Demo League", "DL", "Castleton FC", "Fairview United", "SCHEDULED")
        )
    }
}
