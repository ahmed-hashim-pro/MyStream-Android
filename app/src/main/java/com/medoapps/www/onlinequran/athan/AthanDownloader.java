package com.medoapps.www.onlinequran.athan;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Downloads a {@link AthanSound.Type#DOWNLOADABLE} sound to the app's files
 * dir. Callbacks are delivered on the main thread. The download writes to a
 * temp file and renames on success so a partial file never looks complete.
 */
public final class AthanDownloader {

    public interface Callback {
        void onProgress(int percent);
        void onComplete(File file);
        void onError(String message);
    }

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private AthanDownloader() {
    }

    public static void download(Context appContext, AthanSound sound, Callback cb) {
        final Handler main = new Handler(Looper.getMainLooper());
        EXECUTOR.execute(() -> {
            HttpURLConnection conn = null;
            File target = sound.localFile(appContext);
            File tmp = new File(target.getAbsolutePath() + ".part");
            try {
                conn = (HttpURLConnection) new URL(sound.url).openConnection();
                conn.setRequestProperty("User-Agent", "MyStream-Athan/1.0");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(20000);
                conn.setInstanceFollowRedirects(true);
                if (conn.getResponseCode() != 200) {
                    fail(main, cb, "HTTP " + conn.getResponseCode());
                    return;
                }
                int total = conn.getContentLength();
                try (InputStream in = conn.getInputStream();
                     FileOutputStream out = new FileOutputStream(tmp)) {
                    byte[] buf = new byte[8192];
                    int read, sofar = 0, lastPct = -1;
                    while ((read = in.read(buf)) != -1) {
                        out.write(buf, 0, read);
                        sofar += read;
                        if (total > 0) {
                            int pct = (int) (sofar * 100L / total);
                            if (pct != lastPct) {
                                lastPct = pct;
                                main.post(() -> cb.onProgress(pct));
                            }
                        }
                    }
                }
                if (tmp.length() == 0 || !tmp.renameTo(target)) {
                    tmp.delete();
                    fail(main, cb, "empty/incomplete download");
                    return;
                }
                main.post(() -> cb.onComplete(target));
            } catch (Exception e) {
                tmp.delete();
                fail(main, cb, e.getMessage() == null ? "download failed" : e.getMessage());
            } finally {
                if (conn != null) conn.disconnect();
            }
        });
    }

    private static void fail(Handler main, Callback cb, String msg) {
        main.post(() -> cb.onError(msg));
    }
}
