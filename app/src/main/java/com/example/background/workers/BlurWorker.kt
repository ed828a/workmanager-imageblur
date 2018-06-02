package com.example.background.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build.VERSION_CODES.O
import android.util.Log
import android.widget.ImageView
import androidx.work.Data
import androidx.work.Worker
import com.bumptech.glide.Glide
import com.example.background.Constants
import com.example.background.Constants.TAG
import com.example.background.R

/*
 * Created by Edward on 6/1/2018.
 */

class BlurWorker: Worker() {

    override fun doWork(): WorkerResult {

        val context = applicationContext
        var resourceUri = inputData.getString(Constants.KEY_IMAGE_URI, null)
        if (resourceUri.isEmpty()){
            throw IllegalArgumentException("Invalid input uri") as Throwable
        } else {
            Log.e(TAG, "input Uri: $resourceUri" )
        }


        return try {

//            val tempBitmap = GlideApp.with(applicationContext)
//                    .asBitmap().load(Uri.parse(resourceUri)).into(400, 600).get()
//            val outputUri2 = WorkerUtils.writeBitmapToFile(applicationContext, tempBitmap)
//            Log.e(Constants.TAG, outputUri2.toString())
//            val temp = WorkerUtils.blurBitmap(tempBitmap, applicationContext)
//            val outputUri3 =WorkerUtils.writeBitmapToFile(applicationContext, temp)
//            WorkerUtils.makeStatusNotification(outputUri3.toString(), applicationContext)
//            Log.e(Constants.TAG, outputUri3.toString())

//            val picture = BitmapFactory.decodeResource(applicationContext.resources, R.drawable.maremma)

            val resolver = applicationContext.contentResolver
            val picture = BitmapFactory.decodeStream(resolver.openInputStream(Uri.parse(resourceUri)))
            val temp = WorkerUtils.blurBitmap(picture, applicationContext)
            val outputUri = WorkerUtils.writeBitmapToFile(applicationContext, temp)
            WorkerUtils.makeStatusNotification(outputUri.toString(), applicationContext)
            Log.e(Constants.TAG, outputUri.toString())

            val output = Data.Builder()
                    .putString(Constants.KEY_IMAGE_URI, outputUri.toString())
                    .build()
            outputData = output

            WorkerResult.SUCCESS
        } catch (e: Throwable){
            Log.e(Constants.TAG, "Error applying blur ${e.toString()}")
            e.printStackTrace()

            WorkerResult.FAILURE
        }

    }


}