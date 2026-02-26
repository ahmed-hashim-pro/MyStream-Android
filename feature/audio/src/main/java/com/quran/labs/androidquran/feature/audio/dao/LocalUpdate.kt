package com.medoapps.www.onlinequran.feature.audio.dao

import com.medoapps.www.onlinequran.common.audio.QariItem

data class LocalUpdate(val qari: QariItem,
                       val files: List<String> = emptyList(),
                       val needsDatabaseUpgrade: Boolean = false)
