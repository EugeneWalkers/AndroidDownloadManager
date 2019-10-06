package ew.broadcastreceiverexample

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import ew.broadcastreceiverexample.PermissionCode.REQUEST_PERMISSION_EXTERNAL_READ_WRITE
import ew.broadcastreceiverexample.PermissionCode.REQUEST_PERMISSION_INTERNET

object PermissionUtil {

    fun checkInternetPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.INTERNET
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun checkPermissionForExternalReadWrite(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissionForExternalReadWrite(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ),
            REQUEST_PERMISSION_EXTERNAL_READ_WRITE
        )
    }

    fun requestInternetPermission(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.INTERNET),
            REQUEST_PERMISSION_INTERNET
        )
    }
}