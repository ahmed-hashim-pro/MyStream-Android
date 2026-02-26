package com.medoapps.www.onlinequran.dao.bookmark

import com.quran.data.model.bookmark.Tag
import com.medoapps.www.onlinequran.ui.helpers.QuranRow

data class BookmarkResult(val rows: List<QuranRow>, val tagMap: Map<Long, Tag>)
