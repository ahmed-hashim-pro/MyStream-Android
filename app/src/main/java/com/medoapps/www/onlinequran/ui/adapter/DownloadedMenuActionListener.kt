package com.medoapps.www.onlinequran.ui.adapter

import com.medoapps.www.onlinequran.dao.translation.TranslationItem

interface DownloadedMenuActionListener {
  fun startMenuAction(item: TranslationItem, downloadedItemActionListener: DownloadedItemActionListener?)
  fun finishMenuAction()
}
