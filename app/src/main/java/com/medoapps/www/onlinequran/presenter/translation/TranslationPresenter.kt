package com.medoapps.www.onlinequran.presenter.translation

import com.quran.data.core.QuranInfo
import com.quran.data.di.QuranPageScope
import com.medoapps.www.onlinequran.common.LocalTranslation
import com.medoapps.www.onlinequran.common.QuranAyahInfo
import com.medoapps.www.onlinequran.database.TranslationsDBAdapter
import com.medoapps.www.onlinequran.model.translation.TranslationModel
import com.medoapps.www.onlinequran.util.QuranSettings
import com.medoapps.www.onlinequran.util.TranslationUtil
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.observers.DisposableObserver
import javax.inject.Inject

@QuranPageScope
internal class TranslationPresenter @Inject internal constructor(translationModel: TranslationModel,
                     private val quranSettings: QuranSettings,
                     translationsAdapter: TranslationsDBAdapter,
                     translationUtil: TranslationUtil,
                     private val quranInfo: QuranInfo,
                     private val pages: IntArray) :
    BaseTranslationPresenter<TranslationPresenter.TranslationScreen>(
        translationModel, translationsAdapter, translationUtil, quranInfo) {

  fun refresh() {
    disposable?.dispose()

    disposable = Observable.fromArray(*pages.toTypedArray())
        .flatMap { page ->
          getVerses(quranSettings.wantArabicInTranslationView(),
              getTranslations(quranSettings), quranInfo.getVerseRangeForPage(page))
              .toObservable()
        }
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(object : DisposableObserver<ResultHolder>() {
          override fun onNext(result: ResultHolder) {
            val screen = translationScreen
            if (screen != null && result.ayahInformation.isNotEmpty()) {
              screen.setVerses(
                  getPage(result.ayahInformation), result.translations,
                  result.ayahInformation)
              screen.updateScrollPosition()
            }
          }

          override fun onError(e: Throwable) {}

          override fun onComplete() {}
        })
  }

  private fun getPage(result: List<QuranAyahInfo>): Int {
    val firstPage = pages.firstOrNull()
    return if (pages.size == 1 && firstPage != null) {
      firstPage
    } else {
      quranInfo.getPageFromSuraAyah(result[0].sura, result[0].ayah)
    }
  }

  interface TranslationScreen {
    fun setVerses(page: Int,
                  translations: Array<LocalTranslation>,
                  verses: List<@JvmSuppressWildcards QuranAyahInfo>)
    fun updateScrollPosition()
  }
}
