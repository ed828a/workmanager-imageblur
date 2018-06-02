package com.example.background.workers

import android.util.Log
import androidx.work.Worker
import com.example.background.Constants
import java.io.File

/*
 * Created by Edward on 6/2/2018.
 */

class CleanupWorker: Worker() {

    override fun doWork(): WorkerResult {
        WorkerUtils.makeStatusNotification("Doing <CleanupWorker>", applicationContext);
        WorkerUtils.sleep();
        try {
            val outputDirectory = File(applicationContext.filesDir, Constants.OUTPUT_PATH)
            if (outputDirectory.exists()){
                val entries = outputDirectory.listFiles()
                entries?.forEach {
                    if (it.name.endsWith(".png") ){
                        val deleted = it.delete()
                        Log.i("CleanupWorker", String.format("Deleted %s - %s", it.name, deleted))
                    }
                }
            }

            return WorkerResult.SUCCESS
        } catch (e: Exception){
            e.printStackTrace()
            Log.e("CleanupWorker", e.toString())
            return WorkerResult.FAILURE
        }
    }
}