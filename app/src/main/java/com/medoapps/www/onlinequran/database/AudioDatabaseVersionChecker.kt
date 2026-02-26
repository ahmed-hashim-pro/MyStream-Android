package com.medoapps.www.onlinequran.database

import com.medoapps.www.onlinequran.feature.audio.VersionableDatabaseChecker
import javax.inject.Inject

class AudioDatabaseVersionChecker @Inject constructor() : VersionableDatabaseChecker {
  override fun getVersionForDatabase(path: String): Int {
    return SuraTimingDatabaseHandler.getDatabaseHandler(path).getVersion()
  }
}
