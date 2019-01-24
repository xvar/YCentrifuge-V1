package allgoritm.com.centrifuge.v1.util.log

import allgoritm.com.centrifuge.v1.BuildConfig
import allgoritm.com.centrifuge.v1.R
import allgoritm.com.centrifuge.v1.data.LOG_TAG
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Process
import android.util.Log
import android.widget.Toast

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class FileLogger(/*non null app-level context*/private val context: Context, private val logName: String) : Logger {

    private val logFolder: String = context.getExternalFilesDir("logs")!!.path + "/"

    override fun log(level: Int, tag: String, msg: String, throwable: Throwable?) {
        val df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.ENGLISH)
        val formatted = "${df.format(Date(System.currentTimeMillis()))}/ ${BuildConfig.APPLICATION_ID} ${tag(level)}/$tag:  $msg  $throwable"
        log(formatted, logName)
    }

    private fun log(t: String, logName: String) {
        var text = t
        if (!hasWritePermission()) {
            if (BuildConfig.DEBUG) {
                Log.e(LOG_TAG, "couldn't write log to file: enable writing to the external storage!")
            }
            return
        }
        text = Date().toString() + ":" + text
        val logFile = File("$logFolder$logName.file")
        if (!logFile.exists()) {
            try {
                logFile.createNewFile()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        }
        try {
            //BufferedWriter for performance, true to set append to file flag
            val buf = BufferedWriter(FileWriter(logFile, true))
            buf.append(text)
            buf.newLine()
            buf.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    private fun hasWritePermission(): Boolean {
        return context.checkPermission(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.os.Process.myPid(), Process.myUid()
        ) == PackageManager.PERMISSION_GRANTED
    }
}
