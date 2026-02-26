/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.medoapps.www.onlinequran;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;

import java.io.IOException;
import java.io.InputStream;

/**
 * Service to handle downloading files from Firebase Storage.
 */
public class MyDownloadService extends MyBaseTaskService {

    private static final String TAG = "DlServiceUserPhoto";

    /** Actions **/
    public static final String ACTION_DOWNLOAD_UserPhoto = "action_download_UserPhoto";
    public static final String DOWNLOAD_COMPLETED_UserPhoto = "download_completed_UserPhoto";
    public static final String DOWNLOAD_ERROR_UserPhoto = "download_error_UserPhoto";

    /** Extras **/
    public static final String EXTRA_DOWNLOAD_PATH_UserPhoto = "extra_download_path_UserPhoto";
    public static final String EXTRA_BYTES_DOWNLOADED_UserPhoto = "extra_bytes_downloaded_UserPhoto";

    private StorageReference mStorageRef;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize Storage
        mStorageRef = FirebaseStorage.getInstance().getReference();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:" + intent + ":" + startId);

        if (ACTION_DOWNLOAD_UserPhoto.equals(intent.getAction())) {
            // Get the path to download from the intent
            String downloadPath = intent.getStringExtra(EXTRA_DOWNLOAD_PATH_UserPhoto);
            downloadFromPath(downloadPath);
        }

        return START_REDELIVER_INTENT;
    }

    private void downloadFromPath(final String downloadPath) {
        Log.d(TAG, "downloadFromPath:" + downloadPath);

        // Mark task started
        taskStarted();
        showProgressNotification(getString(R.string.progress_downloading), 0, 0);

        // Download and get total bytes
        mStorageRef.child(downloadPath).getStream(
                new StreamDownloadTask.StreamProcessor() {
                    @Override
                    public void doInBackground(StreamDownloadTask.TaskSnapshot taskSnapshot,
                                               InputStream inputStream) throws IOException {
                        long totalBytes = taskSnapshot.getTotalByteCount();
                        long bytesDownloaded = 0;

                        byte[] buffer = new byte[1024];
                        int size;

                        while ((size = inputStream.read(buffer)) != -1) {
                            bytesDownloaded += size;
                            showProgressNotification(getString(R.string.progress_downloading),
                                    bytesDownloaded, totalBytes);
                        }

                        // Close the stream at the end of the Task
                        inputStream.close();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<StreamDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(StreamDownloadTask.TaskSnapshot taskSnapshot) {
                        Log.d(TAG, "download:SUCCESS");

                        // Send success broadcast with number of bytes downloaded
                        broadcastDownloadFinished(downloadPath, taskSnapshot.getTotalByteCount());
                        showDownloadFinishedNotification(downloadPath, (int) taskSnapshot.getTotalByteCount());

                        // Mark task completed
                        taskCompleted();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.w(TAG, "download:FAILURE", exception);

                        // Send failure broadcast
                        broadcastDownloadFinished(downloadPath, -1);
                        showDownloadFinishedNotification(downloadPath, -1);

                        // Mark task completed
                        taskCompleted();
                    }
                });
    }

    /**
     * Broadcast finished download (success or failure).
     * @return true if a running receiver received the broadcast.
     */
    private boolean broadcastDownloadFinished(String downloadPath, long bytesDownloaded) {
        boolean success = bytesDownloaded != -1;
        String action = success ? DOWNLOAD_COMPLETED_UserPhoto : DOWNLOAD_ERROR_UserPhoto;

        Intent broadcast = new Intent(action)
                .putExtra(EXTRA_DOWNLOAD_PATH_UserPhoto, downloadPath)
                .putExtra(EXTRA_BYTES_DOWNLOADED_UserPhoto, bytesDownloaded);
        return LocalBroadcastManager.getInstance(getApplicationContext())
                .sendBroadcast(broadcast);
    }

    /**
     * Show a notification for a finished download.
     */
    private void showDownloadFinishedNotification(String downloadPath, int bytesDownloaded) {
        // Hide the progress notification
        dismissProgressNotification();

        // Make Intent to MainActivity
        Intent intent = new Intent(this, MainActivity.class)
                .putExtra(EXTRA_DOWNLOAD_PATH_UserPhoto, downloadPath)
                .putExtra(EXTRA_BYTES_DOWNLOADED_UserPhoto, bytesDownloaded)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        boolean success = bytesDownloaded != -1;
        String caption = success ? getString(R.string.download_success) : getString(R.string.download_failure);
        showFinishedNotification(caption, intent, true);
    }


    public static IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(DOWNLOAD_COMPLETED_UserPhoto);
        filter.addAction(DOWNLOAD_ERROR_UserPhoto);

        return filter;
    }
}
