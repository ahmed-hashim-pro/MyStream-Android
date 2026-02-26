package com.medoapps.www.onlinequran.feature.audio

interface VersionableDatabaseChecker {
  fun getVersionForDatabase(path: String): Int
}
