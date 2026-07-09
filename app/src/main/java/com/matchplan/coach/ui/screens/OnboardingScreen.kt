package com.matchplan.coach.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.matchplan.coach.ui.Copy
import com.matchplan.coach.ui.theme.SoftGreenPanel

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val points = listOf(
        "Plan your next football match." to "Add opponents, players, lineups, tasks, and notes.",
        "Prepare your team before the match." to "Build a squad, pick a starting lineup and a formation.",
        "Save post-match notes after the game." to "Keep tactical notes and results in your match history.",
        "View football matches as an extra reference." to "The Match Schedule screen is a small secondary feature.",
        "Your data stays on this device." to "No account. No ads. No betting. No official logos."
    )

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(24.dp))
            // Simple field-line badge (no external assets).
            Surface(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.size(84.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("MP", color = MaterialTheme.colorScheme.onSecondary,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                "MatchPlan Coach",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                "Amateur football match planner",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))

            points.forEach { (title, body) ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(title, style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(body, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                color = SoftGreenPanel,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    Copy.PLANNING_DISCLAIMER,
                    modifier = Modifier.padding(14.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onFinish,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("Start Planning", style = MaterialTheme.typography.titleMedium)
            }
            TextButton(onClick = onFinish) {
                Text("Skip", color = MaterialTheme.colorScheme.onPrimary)
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
