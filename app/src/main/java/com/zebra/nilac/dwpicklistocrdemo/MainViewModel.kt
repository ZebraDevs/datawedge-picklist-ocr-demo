package com.zebra.nilac.dwpicklistocrdemo

import android.app.Application
import android.content.ContentResolver
import android.database.Cursor
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.zebra.nilac.dwpicklistocrdemo.util.AppConstants
import com.zebra.nilac.dwpicklistocrdemo.util.DWUtil
import com.zebra.nilac.dwpicklistocrdemo.util.ImageProcessing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.Date

class MainViewModel(private var application: Application) : AndroidViewModel(application) {

    val processedOutputResult: MutableLiveData<OutputResult> by lazy {
        MutableLiveData<OutputResult>()
    }

    fun parseOCRResult(json: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val jsonArray = JSONArray(json)
            val jsonObject = jsonArray.getJSONObject(0)

            val uri = if (jsonObject.has(AppConstants.KEY_STRING_URI)) {
                jsonObject.getString("uri")
            } else {
                ""
            }

            if (uri.isEmpty() || jsonArray.length() == 1) {
                processedOutputResult.postValue(
                    OutputResult(
                        DWUtil.extractStringDataFromJson(jsonArray[0] as JSONObject), Date(), null
                    )
                )
                return@launch
            }

            //Extract image from provided URI
            val baos = ByteArrayOutputStream()
            var nextURI: String? = uri

            val contentResolver: ContentResolver = application.contentResolver

            // Loop to collect all the data from the URIs
            while (!nextURI.isNullOrEmpty()) {
                val cursor = contentResolver.query(Uri.parse(nextURI), null, null, null, null)
                cursor?.use {
                    nextURI = if (it.moveToFirst()) {
                        val rawData = it.getBlob(it.getColumnIndex(AppConstants.RAW_DATA))
                        baos.write(rawData)
                        it.getString(it.getColumnIndex(AppConstants.DATA_NEXT_URI))
                    } else {
                        null
                    }
                }
            }

            // Extract image data from the JSON object
            val width = jsonObject.getInt(AppConstants.IMAGE_WIDTH)
            val height = jsonObject.getInt(AppConstants.IMAGE_HEIGHT)
            val stride = jsonObject.getInt(AppConstants.STRIDE)
            val orientation = jsonObject.getInt(AppConstants.ORIENTATION)
            val imageFormat = jsonObject.getString(AppConstants.IMAGE_FORMAT)

            // Decode the image
            val bitmap: Bitmap = ImageProcessing.getInstance().getBitmap(
                baos.toByteArray(), imageFormat, orientation, stride, width, height
            )

            processedOutputResult.postValue(
                OutputResult(
                    DWUtil.extractStringDataFromJson(jsonArray[1] as JSONObject), Date(), bitmap
                )
            )
        }
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}