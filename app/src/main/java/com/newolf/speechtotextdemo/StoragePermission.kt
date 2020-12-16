package com.newolf.speechtotextdemo

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object  StoragePermission {
    private val REQUEST_PERMISSIONS = 1

    private val PERMISSIONS_ALL = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO
    )

    fun getAllPermission(activity: Activity) {
        val permissionRead: Int = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val permissionWrite: Int = ContextCompat.checkSelfPermission(
            activity,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permissionRecord: Int =
            ContextCompat.checkSelfPermission(activity, Manifest.permission.RECORD_AUDIO)
        if (permissionRead != PackageManager.PERMISSION_GRANTED || permissionWrite != PackageManager.PERMISSION_GRANTED || permissionRecord != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_ALL, REQUEST_PERMISSIONS)
        }
    }
}
