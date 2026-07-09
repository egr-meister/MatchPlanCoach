package com.matchplan.coach.data.model

/**
 * Predefined, stable slot coordinates for each formation.
 *
 * Coordinate system (relative, 0.0 .. 1.0):
 *   x = 0.5  -> center of the pitch
 *   y = 0.08 -> near the opponent goal (top of the board)
 *   y = 0.92 -> near our own goal (bottom of the board, where the GK stands)
 *
 * There is no physics or drag behaviour — slots are fixed positions and the
 * user simply taps a slot to assign a player.
 */
object Formations {

    private fun slot(id: String, label: String, line: String, x: Float, y: Float) =
        FormationSlot(slotId = id, label = label, line = line, x = x, y = y)

    fun defaultSlots(formation: FormationType): List<FormationSlot> = when (formation) {
        FormationType.FourFourTwo -> listOf(
            slot("gk", "GK", "Goalkeeper", 0.50f, 0.90f),
            slot("d1", "LB", "Defence", 0.16f, 0.72f),
            slot("d2", "CB", "Defence", 0.38f, 0.74f),
            slot("d3", "CB", "Defence", 0.62f, 0.74f),
            slot("d4", "RB", "Defence", 0.84f, 0.72f),
            slot("m1", "LM", "Midfield", 0.16f, 0.50f),
            slot("m2", "CM", "Midfield", 0.38f, 0.52f),
            slot("m3", "CM", "Midfield", 0.62f, 0.52f),
            slot("m4", "RM", "Midfield", 0.84f, 0.50f),
            slot("f1", "ST", "Attack", 0.38f, 0.25f),
            slot("f2", "ST", "Attack", 0.62f, 0.25f)
        )
        FormationType.FourThreeThree -> listOf(
            slot("gk", "GK", "Goalkeeper", 0.50f, 0.90f),
            slot("d1", "LB", "Defence", 0.16f, 0.72f),
            slot("d2", "CB", "Defence", 0.38f, 0.74f),
            slot("d3", "CB", "Defence", 0.62f, 0.74f),
            slot("d4", "RB", "Defence", 0.84f, 0.72f),
            slot("m1", "CM", "Midfield", 0.28f, 0.52f),
            slot("m2", "CM", "Midfield", 0.50f, 0.54f),
            slot("m3", "CM", "Midfield", 0.72f, 0.52f),
            slot("f1", "LW", "Attack", 0.20f, 0.26f),
            slot("f2", "ST", "Attack", 0.50f, 0.22f),
            slot("f3", "RW", "Attack", 0.80f, 0.26f)
        )
        FormationType.ThreeFiveTwo -> listOf(
            slot("gk", "GK", "Goalkeeper", 0.50f, 0.90f),
            slot("d1", "CB", "Defence", 0.28f, 0.74f),
            slot("d2", "CB", "Defence", 0.50f, 0.76f),
            slot("d3", "CB", "Defence", 0.72f, 0.74f),
            slot("m1", "LWB", "Midfield", 0.12f, 0.52f),
            slot("m2", "CM", "Midfield", 0.35f, 0.54f),
            slot("m3", "CM", "Midfield", 0.50f, 0.56f),
            slot("m4", "CM", "Midfield", 0.65f, 0.54f),
            slot("m5", "RWB", "Midfield", 0.88f, 0.52f),
            slot("f1", "ST", "Attack", 0.38f, 0.24f),
            slot("f2", "ST", "Attack", 0.62f, 0.24f)
        )
        FormationType.FiveThreeTwo -> listOf(
            slot("gk", "GK", "Goalkeeper", 0.50f, 0.90f),
            slot("d1", "LWB", "Defence", 0.12f, 0.70f),
            slot("d2", "CB", "Defence", 0.32f, 0.74f),
            slot("d3", "CB", "Defence", 0.50f, 0.76f),
            slot("d4", "CB", "Defence", 0.68f, 0.74f),
            slot("d5", "RWB", "Defence", 0.88f, 0.70f),
            slot("m1", "CM", "Midfield", 0.30f, 0.50f),
            slot("m2", "CM", "Midfield", 0.50f, 0.52f),
            slot("m3", "CM", "Midfield", 0.70f, 0.50f),
            slot("f1", "ST", "Attack", 0.38f, 0.24f),
            slot("f2", "ST", "Attack", 0.62f, 0.24f)
        )
        FormationType.FourTwoThreeOne -> listOf(
            slot("gk", "GK", "Goalkeeper", 0.50f, 0.90f),
            slot("d1", "LB", "Defence", 0.16f, 0.72f),
            slot("d2", "CB", "Defence", 0.38f, 0.74f),
            slot("d3", "CB", "Defence", 0.62f, 0.74f),
            slot("d4", "RB", "Defence", 0.84f, 0.72f),
            slot("m1", "CDM", "Midfield", 0.38f, 0.58f),
            slot("m2", "CDM", "Midfield", 0.62f, 0.58f),
            slot("a1", "LAM", "Attack Mid", 0.20f, 0.40f),
            slot("a2", "CAM", "Attack Mid", 0.50f, 0.38f),
            slot("a3", "RAM", "Attack Mid", 0.80f, 0.40f),
            slot("f1", "ST", "Attack", 0.50f, 0.20f)
        )
    }

    /** Returns a fresh lineup for a match with empty slots for the formation. */
    fun newLineup(matchId: String, formation: FormationType): StartingLineup =
        StartingLineup(
            matchId = matchId,
            formation = formation,
            slots = defaultSlots(formation)
        )
}
