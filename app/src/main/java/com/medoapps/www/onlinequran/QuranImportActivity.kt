package com.medoapps.www.onlinequran

import android.app.Dialog
import android.os.Bundle
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.medoapps.www.onlinequran.R
import com.quran.data.model.bookmark.BookmarkData
import com.medoapps.www.onlinequran.presenter.QuranImportPresenter
import com.medoapps.www.onlinequran.ui.util.ToastCompat
import com.medoapps.www.onlinequran.util.AppBottomSheet
import javax.inject.Inject

class QuranImportActivity : AppCompatActivity(), ActivityCompat.OnRequestPermissionsResultCallback {

  private var alertDialog: Dialog? = null

  @Inject
  lateinit var presenter: QuranImportPresenter

  override fun onCreate(savedInstanceState: Bundle?) {
    val quranApp = application as QuranApplication
    quranApp.refreshLocale(this, false)
    super.onCreate(savedInstanceState)
    quranApp.applicationComponent.inject(this)
  }

  override fun onResume() {
    super.onResume()
    presenter.bind(this)
  }

  override fun onPause() {
    presenter.unbind(this)
    super.onPause()
  }

  override fun onDestroy() {
    presenter.unbind(this)
    alertDialog?.dismiss()
    super.onDestroy()
  }

  override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
  ) {
    presenter.onPermissionsResult(requestCode, grantResults)
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  }

  fun isShowingDialog(): Boolean = alertDialog != null

  fun showImportConfirmationDialog(bookmarkData: BookmarkData) {
    val dialogMessage = getString(
      R.string.import_data_and_override,
      bookmarkData.bookmarks.size,
      bookmarkData.tags.size
    )
    alertDialog = AppBottomSheet.showConfirmation(
        this,
        "",
        dialogMessage,
        getString(R.string.import_data),
        getString(android.R.string.cancel),
        { presenter.importData(bookmarkData) },
        { finish() }
    )
  }

  fun showImportComplete() {
    ToastCompat.makeText(this, R.string.import_successful, Toast.LENGTH_LONG).show()
    finish()
  }

  fun showError() {
    showErrorInternal(R.string.import_data_error)
  }

  fun showPermissionsError() {
    showErrorInternal(R.string.import_data_permissions_error)
  }

  private fun showErrorInternal(@StringRes messageId: Int) {
    alertDialog = AppBottomSheet.showMessage(this, "", getString(messageId))
  }
}
