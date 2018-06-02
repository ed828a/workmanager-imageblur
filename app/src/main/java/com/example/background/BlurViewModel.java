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

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.net.Uri;
import android.text.TextUtils;

import com.example.background.workers.BlurWorker;
import com.example.background.workers.CleanupWorker;
import com.example.background.workers.SaveImageToFileWorker;

import java.util.List;

import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkContinuation;
import androidx.work.WorkManager;
import androidx.work.WorkStatus;

import static com.example.background.Constants.IMAGE_MANIPULATION_WORK_NAME;
import static com.example.background.Constants.KEY_IMAGE_URI;
import static com.example.background.Constants.TAG_OUTPUT;

public class BlurViewModel extends ViewModel {

    private Uri mImageUri;
    private WorkManager mWorkManager;

    private LiveData<List<WorkStatus>> mSavedWorkStatus;
    // new Instatnce variable for the workStatus
    private Uri mOutputUri;


    public Uri getOutputUri() {
        return mOutputUri;
    }

    public void setOutputUri(String outputUri) {
        this.mOutputUri = uriOrNull(outputUri);
    }

    // cancel work using tag
    void cancelWork(){
        mWorkManager.cancelUniqueWork(IMAGE_MANIPULATION_WORK_NAME);
    }

    //

    public LiveData<List<WorkStatus>> getOutputStatus() {
        return mSavedWorkStatus;
    }

    public BlurViewModel() {
        mWorkManager = WorkManager.getInstance();
        mSavedWorkStatus = WorkManager.getInstance().getStatusesByTag(TAG_OUTPUT);

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

        // it will now only ever blur one picture at a time.
        WorkContinuation continuation =
                mWorkManager.beginUniqueWork(IMAGE_MANIPULATION_WORK_NAME,
                        ExistingWorkPolicy.REPLACE,
                        OneTimeWorkRequest.from(CleanupWorker.class));

        for (int i = 0; i < blurLevel; i++) {
            OneTimeWorkRequest.Builder blurRequestBuilder =
                    new OneTimeWorkRequest.Builder(BlurWorker.class);

            if (i == 0) {
                blurRequestBuilder.setInputData(createInputDataForUri());
            }

            continuation = continuation.then(blurRequestBuilder.build());
        }

        OneTimeWorkRequest saveImageToFileRequest =
                new OneTimeWorkRequest.Builder(SaveImageToFileWorker.class)
                        .addTag(TAG_OUTPUT)
                        .build();
        continuation = continuation.then(saveImageToFileRequest);
//        continuation = continuation.then(OneTimeWorkRequest.from(SaveImageToFileWorker.class));

        continuation.enqueue(); // actually start the work

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