package com.matchplan.coach.data.remote

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.matchplan.coach.BuildConfig
import com.matchplan.coach.data.local.DemoData
import com.matchplan.coach.data.model.FootballApiResult
import com.matchplan.coach.data.model.MatchSource
import com.matchplan.coach.data.model.NormalizedMatch
import com.matchplan.coach.data.remote.dto.MatchDto
import com.matchplan.coach.util.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * Isolated integration with football-data.org. All network access for the app
 * lives here. It NEVER throws into the UI — every path returns a
 * [FootballApiResult]. The API token is read from [BuildConfig] and attached
 * via an interceptor; it is never written to logs.
 */
class FootballDataRepository {

    private val placeholderToken = "your_api_token_here"

    private val token: String = BuildConfig.FOOTBALL_DATA_API_TOKEN.trim()
    private val baseUrl: String = normalizeBaseUrl(BuildConfig.FOOTBALL_API_BASE_URL)

    private fun normalizeBaseUrl(raw: String): String {
        val safe = raw.ifBlank { "https://api.football-data.org/v4" }
        return if (safe.endsWith("/")) safe else "$safe/"
    }

    /** True when a usable token has been configured. */
    fun hasToken(): Boolean =
        token.isNotBlank() && token != placeholderToken

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    private val service: FootballDataApiService by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val builder = chain.request().newBuilder()
                if (hasToken()) {
                    builder.header("X-Auth-Token", token)
                }
                chain.proceed(builder.build())
            }
            .build()

        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(FootballDataApiService::class.java)
    }

    /**
     * Fetch matches for a date window. Defaults to today .. today+9 days when
     * either bound is blank. Returns demo data if no token is configured, and
     * a friendly error (never an exception) on any failure.
     */
    suspend fun fetchMatches(
        dateFrom: String,
        dateTo: String,
        competitionCode: String
    ): FootballApiResult = withContext(Dispatchers.IO) {
        val from = if (DateUtils.isValidDate(dateFrom)) dateFrom else DateUtils.today()
        val to = if (DateUtils.isValidDate(dateTo)) dateTo else DateUtils.todayPlusDays(9)
        val comp = competitionCode.trim().ifBlank { null }

        if (!hasToken()) {
            return@withContext FootballApiResult(
                ok = true,
                matches = DemoData.demoMatches(),
                error = "API token is not configured. Showing demo matches.",
                usedDemoData = true,
                source = MatchSource.Demo
            )
        }

        try {
            val response = service.getMatches(from, to, comp)
            if (response.isSuccessful) {
                val body = response.body()
                val normalized = (body?.matches ?: emptyList()).mapNotNull { normalize(it) }
                FootballApiResult(
                    ok = true,
                    matches = normalized,
                    error = "",
                    usedDemoData = false,
                    source = MatchSource.Api
                )
            } else {
                FootballApiResult(
                    ok = false,
                    matches = emptyList(),
                    error = friendlyHttpError(response.code()),
                    usedDemoData = false,
                    source = MatchSource.Api
                )
            }
        } catch (e: Exception) {
            FootballApiResult(
                ok = false,
                matches = emptyList(),
                error = "Could not load matches. Check your internet connection and try again.",
                usedDemoData = false,
                source = MatchSource.Api
            )
        }
    }

    private fun friendlyHttpError(code: Int): String = when (code) {
        400 -> "The request could not be understood. Try resetting the date window."
        401, 403 -> "The API token was rejected or this data is not available on your API plan."
        404 -> "No matching data was found for this request."
        429 -> "The API request limit was reached. Please try again later."
        in 500..599 -> "The match data service is temporarily unavailable."
        else -> "Could not load the latest matches (error $code)."
    }

    /** Safely convert a raw API match into a [NormalizedMatch]. */
    private fun normalize(dto: MatchDto): NormalizedMatch {
        val utc = dto.utcDate.orEmpty()
        return NormalizedMatch(
            id = dto.id?.toString() ?: ("api-" + utc.hashCode()),
            utcDate = utc,
            date = DateUtils.dateFromUtc(utc),
            time = DateUtils.timeFromUtc(utc),
            competitionName = dto.competition?.name.orEmpty(),
            competitionCode = dto.competition?.code.orEmpty(),
            homeTeam = pickTeamName(dto.homeTeam?.shortName, dto.homeTeam?.name),
            awayTeam = pickTeamName(dto.awayTeam?.shortName, dto.awayTeam?.name),
            status = dto.status.orEmpty(),
            homeScore = dto.score?.fullTime?.home,
            awayScore = dto.score?.fullTime?.away,
            winner = dto.score?.winner.orEmpty(),
            source = MatchSource.Api
        )
    }

    private fun pickTeamName(shortName: String?, name: String?): String =
        shortName?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: "Unknown"
}
