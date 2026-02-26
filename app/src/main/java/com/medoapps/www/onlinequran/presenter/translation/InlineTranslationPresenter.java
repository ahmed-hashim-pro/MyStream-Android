package com.medoapps.www.onlinequran.presenter.translation;

import com.quran.data.core.QuranInfo;
import com.medoapps.www.onlinequran.common.LocalTranslation;
import com.medoapps.www.onlinequran.common.QuranAyahInfo;
import com.quran.data.model.VerseRange;
import com.medoapps.www.onlinequran.database.TranslationsDBAdapter;
import com.medoapps.www.onlinequran.model.translation.TranslationModel;
import com.medoapps.www.onlinequran.util.QuranSettings;
import com.medoapps.www.onlinequran.util.TranslationUtil;

import java.util.List;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.observers.DisposableSingleObserver;

public class InlineTranslationPresenter extends
    BaseTranslationPresenter<InlineTranslationPresenter.TranslationScreen> {
  private final QuranSettings quranSettings;

  @Inject
  InlineTranslationPresenter(TranslationModel translationModel,
                             TranslationsDBAdapter dbAdapter,
                             TranslationUtil translationUtil,
                             QuranSettings quranSettings,
                             QuranInfo quranInfo) {
    super(translationModel, dbAdapter, translationUtil, quranInfo);
    this.quranSettings = quranSettings;
  }

  public void refresh(VerseRange verseRange) {
    if (getDisposable() != null) {
      getDisposable().dispose();
    }

    setDisposable(getVerses(false, getTranslations(quranSettings), verseRange)
        .observeOn(AndroidSchedulers.mainThread())
        .subscribeWith(new DisposableSingleObserver<ResultHolder>() {
          @Override
          public void onSuccess(@NonNull ResultHolder result) {
            if (getTranslationScreen() != null) {
              getTranslationScreen()
                  .setVerses(result.getTranslations(), result.getAyahInformation());
            }
          }

          @Override
          public void onError(@NonNull Throwable e) {
          }
        }));
  }

  public interface TranslationScreen {
    void setVerses(@NonNull LocalTranslation[] translations, @NonNull List<QuranAyahInfo> verses);
  }
}
