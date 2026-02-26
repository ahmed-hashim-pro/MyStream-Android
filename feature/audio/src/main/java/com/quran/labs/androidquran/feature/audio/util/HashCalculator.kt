package com.medoapps.www.onlinequran.feature.audio.util

import java.io.File

interface HashCalculator {
  fun calculateHash(file: File): String
}
