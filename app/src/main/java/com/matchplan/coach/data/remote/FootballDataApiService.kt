package com.matchplan.coach.data.remote

import com.matchplan.coach.data.remote.dto.MatchesResponseDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the football-data.org v4 matches endpoint.
 *
 * Only the /matches endpoint is used. NO odds, predictions, bookmaker or
 * betting endpoints are ever called. The X-Auth-Token header is attached by
 * an OkHttp interceptor in [FootballDataRepository] (not here) so the token is
 * never logged as part of the method signature.
 */
interface FootballDataApiService {

    @GET("matches")
    suspend fun getMatches(
        @Query("dateFrom") dateFrom: String,
        @Query("dateTo") dateTo: String,
        @Query("competitions") competitions: String? = null
    ): Response<MatchesResponseDto>
}
