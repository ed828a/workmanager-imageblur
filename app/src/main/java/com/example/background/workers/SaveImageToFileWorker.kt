package com.example.background.workers

import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import com.example.background.Constants
import java.text.SimpleDateFormat
import java.util.*

/*
 * Created by Edward on 6/2/2018.
 */

class SaveImageToFileWorker: Worker() {

    val TITLE = "Blurred Image"
    val DATE_FORMATTER = SimpleDateFormat("yyyy.MM.dd 'at' HH:MM:SS z", Locale.getDefault())

    override fun doWork(): WorkerResult {
        WorkerUtils.makeStatusNotification("Doing <SaveImageToFileWorker>", applicationContext)
        WorkerUtils.sleep()

        val resolver = applicationContext.contentResolver
        val resourceUri = inputData.getString(Constants.KEY_IMAGE_URI, null)

        try {
            val bitmap = BitmapFactory.decodeStream(
                    resolver.openInputStream(Uri.parse(resourceUri)))

            val imageUrl = MediaStore.Images.Media.insertImage(resolver,
                    bitmap, TITLE, DATE_FORMATTER.format(Date()))
            if (imageUrl.isEmpty()){
                Log.e("SaveImageToFileWorker", "Writing to MediaStore failed.")
                return WorkerResult.FAILURE
            }

            outputData = Data.Builder().putString(Constants.KEY_IMAGE_URI, imageUrl).build()

            return WorkerResult.SUCCESS
        } catch (e: Throwable){
            e.printStackTrace()
            Log.e("SaveImageToFileWorker", "Unable to save image to Gallery ${e.toString()}")
            return WorkerResult.FAILURE
        }
    }
}