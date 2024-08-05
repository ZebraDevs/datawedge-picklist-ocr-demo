package com.zebra.nilac.dwpicklistocrdemo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zebra.nilac.dwpicklistocrdemo.util.DWUtil

class MainActivity : AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sendBroadcast(DWUtil.generateDWBaseProfile(this))
        sendBroadcast(DWUtil.enablePickListOCR())
    }
}