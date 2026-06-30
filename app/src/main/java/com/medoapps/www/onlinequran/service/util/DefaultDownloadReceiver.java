package com.medoapps.www.onlinequran.service.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.medoapps.www.onlinequran.R;
import com.medoapps.www.onlinequran.service.QuranDownloadService;

import java.lang.ref.WeakReference;
import java.text.DecimalFormat;

public class DefaultDownloadReceiver extends BroadcastReceiver {

  private final int mDownloadType;
  private SimpleDownloadListener mListener;
  // Navy+gold first-run progress card (mockup 17 · C/D) replacing the old ProgressDialog.
  private AlertDialog mProgressDialog;
  private TextView mTitleView;
  private TextView mSubtitleView;
  private ProgressBar mProgressBar;
  private TextView mStatusView;
  private View mRetryNote;
  private TextView mRetryText;
  private TextView mCancelButton;
  private final Context mContext;
  private boolean mDidReceiveBroadcast;
  private boolean mCanCancelDownload;

  private static class DownloadReceiverHandler extends Handler {
    private final WeakReference<DefaultDownloadReceiver> mReceiverRef;

    public DownloadReceiverHandler(DefaultDownloadReceiver receiver) {
      mReceiverRef = new WeakReference<>(receiver);
    }

    @Override
    public void handleMessage(Message msg) {
      final DefaultDownloadReceiver receiver = mReceiverRef.get();
      if (receiver == null || receiver.mListener == null) {
        return;
      }

      Intent intent = (Intent) msg.obj;
      String state = intent.getStringExtra(
          QuranDownloadNotifier.ProgressIntent.STATE);
      switch (state) {
        case QuranDownloadNotifier.ProgressIntent.STATE_SUCCESS:
          receiver.dismissDialog();
          receiver.mListener.handleDownloadSuccess();
          break;
        case QuranDownloadNotifier.ProgressIntent.STATE_ERROR: {
          int msgId = ServiceIntentHelper.
              getErrorResourceFromDownloadIntent(intent, true);
          receiver.dismissDialog();
          receiver.mListener.handleDownloadFailure(msgId);
          break;
        }
        case QuranDownloadNotifier.ProgressIntent.STATE_DOWNLOADING: {
          int progress = intent.getIntExtra(
              QuranDownloadNotifier.ProgressIntent.PROGRESS, -1);
          long downloadedSize = intent.getLongExtra(
              QuranDownloadNotifier.ProgressIntent.DOWNLOADED_SIZE, -1);
          long totalSize = intent.getLongExtra(
              QuranDownloadNotifier.ProgressIntent.TOTAL_SIZE, -1);
          int sura = intent.getIntExtra(QuranDownloadNotifier.ProgressIntent.SURA, -1);
          int ayah = intent.getIntExtra(QuranDownloadNotifier.ProgressIntent.AYAH, -1);
          if (receiver.mListener instanceof DownloadListener) {
            ((DownloadListener) receiver.mListener).updateDownloadProgress(progress,
                downloadedSize, totalSize);
          } else {
            receiver.updateDownloadProgress(progress, downloadedSize, totalSize, sura, ayah);
          }
          break;
        }
        case QuranDownloadNotifier.ProgressIntent.STATE_PROCESSING: {
          int progress = intent.getIntExtra(
              QuranDownloadNotifier.ProgressIntent.PROGRESS, -1);
          int processedFiles = intent.getIntExtra(
              QuranDownloadNotifier.ProgressIntent.PROCESSED_FILES, 0);
          int totalFiles = intent.getIntExtra(
              QuranDownloadNotifier.ProgressIntent.TOTAL_FILES, 0);
          if (receiver.mListener instanceof DownloadListener) {
            ((DownloadListener) receiver.mListener).updateProcessingProgress(progress,
                processedFiles, totalFiles);
          } else {
            receiver.updateProcessingProgress(progress, processedFiles, totalFiles);
          }
          break;
        }
        case QuranDownloadNotifier.ProgressIntent
            .STATE_ERROR_WILL_RETRY: {
          int msgId = ServiceIntentHelper.
              getErrorResourceFromDownloadIntent(intent, true);
          if (receiver.mListener instanceof DownloadListener) {
            ((DownloadListener) receiver.mListener)
                .handleDownloadTemporaryError(msgId);
          } else {
            receiver.handleNonFatalError(msgId);
          }
          break;
        }
      }
    }
  }

  private final Handler mHandler;

  public DefaultDownloadReceiver(Context context, int downloadType) {
    mContext = context;
    mDownloadType = downloadType;
    mHandler = new DownloadReceiverHandler(this);
  }

  public void setCanCancelDownload(boolean canCancel) {
    mCanCancelDownload = canCancel;
  }

  @Override
  public void onReceive(Context context, Intent intent) {
    if (intent == null) {
      return;
    }
    int type = intent.getIntExtra(
        QuranDownloadNotifier.ProgressIntent.DOWNLOAD_TYPE,
        QuranDownloadService.DOWNLOAD_TYPE_UNDEF);
    String state = intent.getStringExtra(
        QuranDownloadNotifier.ProgressIntent.STATE);

    if (mDownloadType != type || state == null) {
      return;
    }

    mDidReceiveBroadcast = true;
    Message msg = mHandler.obtainMessage();
    msg.obj = intent;

    // only care about the latest download progress
    mHandler.removeCallbacksAndMessages(null);

    // send the message at the front of the queue
    mHandler.sendMessageAtFrontOfQueue(msg);
  }

  private void dismissDialog() {
    if (mProgressDialog != null) {
      try {
        mProgressDialog.dismiss();
      } catch (Exception e) {
        // no op
      }
      mProgressDialog = null;
    }
  }

  public boolean didReceiveBroadcast() {
    return mDidReceiveBroadcast;
  }

  private void makeAndShowProgressDialog() {
    makeProgressDialog();
    if (mProgressDialog != null && !mProgressDialog.isShowing()) {
      mProgressDialog.show();
    }
  }

  private void makeProgressDialog() {
    if (mProgressDialog != null) {
      return;
    }

    // Navy+gold first-run progress card (mockup 17 · C/D) — a custom view in a
    // transparent-window AlertDialog, replacing the deprecated ProgressDialog.
    View card = LayoutInflater.from(mContext)
        .inflate(R.layout.download_progress_card, null, false);
    mTitleView = card.findViewById(R.id.dpc_title);
    mSubtitleView = card.findViewById(R.id.dpc_subtitle);
    mProgressBar = card.findViewById(R.id.dpc_progress);
    mStatusView = card.findViewById(R.id.dpc_status);
    mRetryNote = card.findViewById(R.id.dpc_retry_note);
    mRetryText = card.findViewById(R.id.dpc_retry_text);
    mCancelButton = card.findViewById(R.id.dpc_cancel);

    mTitleView.setText(R.string.downloading_title);
    mSubtitleView.setText(R.string.downloading_message);

    if (mCanCancelDownload) {
      mCancelButton.setVisibility(View.VISIBLE);
      mCancelButton.setOnClickListener(v -> {
        cancelDownload();
        dismissDialog();
      });
    }

    AlertDialog dialog = new AlertDialog.Builder(mContext, R.style.ThemeOverlay_MyStream_Dialog)
        .setView(card)
        .setCancelable(mCanCancelDownload)
        .create();
    dialog.setCanceledOnTouchOutside(false);
    if (mCanCancelDownload) {
      dialog.setOnCancelListener(d -> cancelDownload());
    }
    // Transparent window so only the navy card's rounded gradient is visible.
    Window window = dialog.getWindow();
    if (window != null) {
      window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    mProgressDialog = dialog;
  }

  private void cancelDownload() {
    Intent i = new Intent(mContext, QuranDownloadService.class);
    i.setAction(QuranDownloadService.ACTION_CANCEL_DOWNLOADS);
    mContext.startService(i);
  }

  private void updateDownloadProgress(int progress,
      long downloadedSize, long totalSize, int currentSura, int currentAyah) {
    makeAndShowProgressDialog();
    if (mProgressDialog == null) {
      return;
    }

    mTitleView.setText(R.string.downloading_title);
    mSubtitleView.setText(R.string.downloading_message);
    mRetryNote.setVisibility(View.GONE);
    if (mCanCancelDownload) {
      mCancelButton.setVisibility(View.VISIBLE);
    }

    if (progress == -1) {
      mProgressBar.setIndeterminate(true);
      mStatusView.setText("");
      return;
    }

    mProgressBar.setIndeterminate(false);
    mProgressBar.setMax(100);
    mProgressBar.setProgress(progress);

    DecimalFormat df = new DecimalFormat("###.00");
    int mb = 1024 * 1024;
    String downloaded = mContext.getString(R.string.prefs_megabytes_str,
        df.format((1.0 * downloadedSize / mb)));
    String total = mContext.getString(R.string.prefs_megabytes_str,
        df.format((1.0 * totalSize / mb)));

    String message;
    if (currentSura < 1) {
      message = String.format(
          mContext.getString(R.string.download_progress),
          downloaded, total);
    } else if (currentAyah <= 0) {
      message = String.format(
          mContext.getString(R.string.download_sura_progress),
          downloaded, total, currentSura);
    } else {
      message = String.format(mContext.getString(R.string.download_sura_ayah_progress),
          currentSura, currentAyah);
    }
    mStatusView.setText(message);
  }

  private void updateProcessingProgress(int progress,
      int processedFiles, int totalFiles) {
    makeAndShowProgressDialog();
    if (mProgressDialog == null) {
      return;
    }

    mTitleView.setText(R.string.extracting_title);
    mSubtitleView.setText(R.string.extracting_message);
    mRetryNote.setVisibility(View.GONE);
    // No Cancel during extraction (mockup 17 · D) — it must finish before the reader opens.
    mCancelButton.setVisibility(View.GONE);

    if (progress == -1) {
      mProgressBar.setIndeterminate(true);
      mStatusView.setText("");
      return;
    }

    mProgressBar.setIndeterminate(false);
    mProgressBar.setMax(100);
    mProgressBar.setProgress(progress);
    mStatusView.setText(String.format(
        mContext.getString(R.string.process_progress),
        processedFiles, totalFiles));
  }

  private void handleNonFatalError(int msgId) {
    makeAndShowProgressDialog();
    if (mProgressDialog == null) {
      return;
    }
    mRetryText.setText(msgId);
    mRetryNote.setVisibility(View.VISIBLE);
  }

  public void setListener(SimpleDownloadListener listener) {
    mListener = listener;
    if (mListener == null && mProgressDialog != null) {
      mProgressDialog.dismiss();
      mProgressDialog = null;
    } else if (mListener != null) {
      makeProgressDialog();
    }
  }

  public interface SimpleDownloadListener {

    void handleDownloadSuccess();

    void handleDownloadFailure(int errId);
  }

  public interface DownloadListener extends SimpleDownloadListener {

    void updateDownloadProgress(int progress,
        long downloadedSize, long totalSize);

    void updateProcessingProgress(int progress,
        int processFiles, int totalFiles);

    void handleDownloadTemporaryError(int errorId);
  }
}
