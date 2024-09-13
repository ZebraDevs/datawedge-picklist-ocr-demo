package com.zebra.nilac.dwpicklistocrdemo

import android.graphics.Bitmap
import java.util.Date

data class OutputResult(
    var value: String = "",

    var date: Date,

    var image: Bitmap? = null
)