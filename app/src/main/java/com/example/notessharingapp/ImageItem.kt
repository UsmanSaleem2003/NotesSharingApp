package com.example.notessharingapp

import android.graphics.Bitmap
import android.net.Uri

data class ImageItem(
    val bitmap: Bitmap,
    val title: String,
    val dateTime: String,
    val uri: Uri // new field to store actual saved uri
)
