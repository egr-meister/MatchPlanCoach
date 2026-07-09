package com.matchplan.coach

import android.app.Application
import com.matchplan.coach.data.local.AppDataStore
import com.matchplan.coach.data.remote.FootballDataRepository
import com.matchplan.coach.data.repository.AppRepository

/**
 * Application-scoped container for the two repositories. No dependency
 * injection framework is used — plain singletons keep the app simple and
 * stable.
 */
class MatchPlanApplication : Application() {

    lateinit var appRepository: AppRepository
        private set

    lateinit var footballRepository: FootballDataRepository
        private set

    override fun onCreate() {
        super.onCreate()
        appRepository = AppRepository(AppDataStore(applicationContext))
        footballRepository = FootballDataRepository()
    }
}
