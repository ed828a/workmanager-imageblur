/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.background;

import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.text.TextUtils;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanupWorker;
import com.example.background.workers.SaveImageToFileWorker;

import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;

import static com.example.background.Constants.KEY_IMAGE_URI;

public class BlurViewModel extends ViewModel {

//    private String mStringUri = "http://www.maremma.nl/images/Cato/clubblad%202.jpg";

    private Uri mImageUri;
    private WorkManager mWorkManager;

    public BlurViewModel() {
        mWorkManager = WorkManager.getInstance();

    }

    private Data createInputDataForUri() {
// mImageUri was set in BlurActivity       mImageUri = Uri.parse(mStringUri);
        Data.Builder builder = new Data.Builder();
        if (mImageUri != null) {
            builder.putString(KEY_IMAGE_URI, mImageUri.toString());
        }

        return builder.build();
    }

    /**
     * Create the WorkRequest to apply the blur and save the resulting image
     *
     * @param blurLevel The amount to blur the image
     */
    void applyBlur(int blurLevel) {
//        OneTimeWorkRequest cleanupRequest =
//                new OneTimeWorkRequest.Builder(CleanupWorker.class).build();
        WorkContinuation continuation =
                mWorkManager.beginWith(OneTimeWorkRequest.from(CleanupWorker.class));

        for (int i = 0; i < blurLevel; i++) {
            OneTimeWorkRequest.Builder blurRequestBuilder =
                    new OneTimeWorkRequest.Builder(BlurWorker.class);

            if (i == 0) {
                blurRequestBuilder.setInputData(createInputDataForUri());
            }

            continuation = continuation.then(blurRequestBuilder.build());
        }

        continuation = continuation.then(OneTimeWorkRequest.from(SaveImageToFileWorker.class));

        continuation.enqueue(); // actually start the work

        // Add WorkRequest to blur the image
//        OneTimeWorkRequest blurRequest = new OneTimeWorkRequest.Builder(BlurWorker.class)
//                .setInputData(createInputDataForUri())
//                .build();
//        continuation = continuation.then(blurRequest);

//        OneTimeWorkRequest saveImageToFileRequest =
//                new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class).build();

//        continuation.then(blurRequest)
//                .then(saveImageToFileRequest)
//                .enqueue();

//        mWorkManager.enqueue(blurRequest);
    }

    private Uri uriOrNull(String uriString) {
        if (!TextUtils.isEmpty(uriString)) {
            return Uri.parse(uriString);
        }
        return null;
    }

    /**
     * Setters
     */
    void setImageUri(String uri) {
        mImageUri = uriOrNull(uri);
    }

    /**
     * Getters
     */
    Uri getImageUri() {
        return mImageUri;
    }

}