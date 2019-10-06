package ew.broadcastreceiverexample

import android.app.Activity
import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ew.broadcastreceiverexample.PermissionCode.REQUEST_PERMISSION_EXTERNAL_READ_WRITE
import ew.broadcastreceiverexample.PermissionCode.REQUEST_PERMISSION_INTERNET
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

const val REQUEST_CODE_CHOOSE_DIRECTORY = 123

const val EMPTY_URL = "Empty url"
const val EMPTY_DESTINATION_PATH = "Empty destination path"
const val NOT_ALLOWED_TO_READ_WRITE = "Not allowed to read/write"
const val NOT_ALLOWED_TO_USE_INTERNET = "Not allowed to use internet"

class MainActivity : AppCompatActivity() {

    private lateinit var downloadUrlField: EditText
    private lateinit var downloadButton: Button
    private lateinit var destinationPathButton: Button
    private lateinit var destinationPathField: EditText
    private lateinit var limitedNetworkCheckbox: CheckBox
    private lateinit var allowNotificationCheckBox: CheckBox
    private lateinit var allowRoamingCheckbox: CheckBox

    private var lastFailedAction: LastFailedAction = LastFailedAction.CHOOSE_FILE

    private var downloadID = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        registerReceiver(onDownloadComplete, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        downloadUrlField = download_url
        downloadButton = download_button
        destinationPathButton = path_button
        destinationPathField = path_edit_text
        limitedNetworkCheckbox = limited_network_checkbox
        allowNotificationCheckBox = allow_notification_checkbox
        allowRoamingCheckbox = allow_roaming_checkbox

        destinationPathButton.setOnClickListener {
            when {
                PermissionUtil.checkPermissionForExternalReadWrite(this).not() -> {
                    PermissionUtil.requestPermissionForExternalReadWrite(this)
                    lastFailedAction = LastFailedAction.CHOOSE_FILE
                }
                else -> {
                    chooseDirectory()
                }
            }
        }

        downloadButton.setOnClickListener {
            checkAndDownload()
        }

        // val intent = Intent()
        // sendBroadcast(intent)
    }

    private fun checkAndDownload() {
        when {
            downloadUrlField.text.isEmpty() -> {
                msg(EMPTY_URL)
            }
            destinationPathField.text.isEmpty() -> {
                msg(EMPTY_DESTINATION_PATH)
            }
            PermissionUtil.checkInternetPermission(this).not() -> {
                PermissionUtil.requestInternetPermission(this)
                lastFailedAction = LastFailedAction.DOWNLOAD
            }
            PermissionUtil.checkPermissionForExternalReadWrite(this).not() -> {
                PermissionUtil.requestPermissionForExternalReadWrite(this)
                lastFailedAction = LastFailedAction.DOWNLOAD
            }
            else -> {
                proceedDownloading()
            }
        }
    }

    private fun proceedDownloading() {
        val downloadUri: Uri = Uri.parse(downloadUrlField.text.toString())
        val destinationUri: Uri = /*Uri.parse(destinationPathField.text.toString())*/Uri.fromFile(
            File(getExternalFilesDir(null), "Dummy")
        )

        val request = DownloadManager.Request(downloadUri)
            .setNotificationVisibility(getNotificationVisibility())
            .setAllowedNetworkTypes(getNetworkType())
            .setAllowedOverRoaming(allowRoamingCheckbox.isChecked)
            // .setVisibleInDownloadsUi(true)
            .setTitle("Downloading...")
            .setDescription("Try to download your file")
            // .setDestinationUri(destinationUri)
            .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "test.mp4")

        downloadID = getService<DownloadManager>(Context.DOWNLOAD_SERVICE).enqueue(request)
    }

    private fun getNotificationVisibility(): Int {
        val isNotificationOn = allowNotificationCheckBox.isChecked

        return if (isNotificationOn) {
            DownloadManager.Request.VISIBILITY_VISIBLE
        } else {
            DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        }
    }

    private fun getNetworkType(): Int {
        return if (limitedNetworkCheckbox.isChecked) {
            DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE
        } else {
            DownloadManager.Request.NETWORK_WIFI
        }
    }

    private fun msg(text: String) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show()
    }

    @Suppress("SameParameterValue", "UNCHECKED_CAST")
    private fun <T> getService(serviceName: String): T {
        return getSystemService(serviceName) as? T
            ?: throw IllegalAccessException("Wrong service name!")
    }

    private fun chooseDirectory() {
        val i = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).addCategory(Intent.CATEGORY_DEFAULT)
        startActivityForResult(
            Intent.createChooser(i, "Choose directory"),
            REQUEST_CODE_CHOOSE_DIRECTORY
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_CODE_CHOOSE_DIRECTORY -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let {
                        destinationPathField.setText(
                            Environment.getExternalStorageDirectory().toString() + File.separator + it.path.split(
                                ":"
                            )[1]
                        )
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_INTERNET -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    checkAndDownload()
                } else {
                    msg(NOT_ALLOWED_TO_USE_INTERNET)
                }
                return
            }
            REQUEST_PERMISSION_EXTERNAL_READ_WRITE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                    when (lastFailedAction) {
                        LastFailedAction.CHOOSE_FILE -> {
                            chooseDirectory()
                        }
                        LastFailedAction.DOWNLOAD -> {
                            checkAndDownload()
                        }
                    }
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {
                    msg(NOT_ALLOWED_TO_READ_WRITE)
                }
                return
            }

            // Add other 'when' lines to check for other
            // permissions this app might request.
            else -> {
                // Ignore all other requests.
            }
        }
    }

    private val onDownloadComplete = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //Fetching the download id received with the broadcast
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            //Checking if the received broadcast is for our enqueued download by matching download id
            if (downloadID == id) {
                Toast.makeText(this@MainActivity, "Download Completed", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
