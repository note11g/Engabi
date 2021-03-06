package com.note11.engabi.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.StatFs
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object GetFilesUtil {
    fun getFiles(context: Context, extension: List<String>? = null) : List<File> {
        var files = File(context.filesDir.absolutePath).listFiles()?.toList()
        extension?.let { ext ->
            files = files?.filter { file ->
                ext.contains(file.extension)
            }
        }

        return files ?: listOf()
    }

    fun getFileFromPath(path: String) = File(path)

    /**
     * play audio
     * @param file must internal file(private file)
      */
    @SuppressLint("Range")
    fun playAudioIntent(context: Context, file: File) {
        val intent = Intent()
        intent.action = Intent.ACTION_VIEW
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        intent.setDataAndType(uri, "audio/*")
        context.startActivity(intent)
    }

    private fun getAvailableMemory(context: Context) : Long {
        val stat = StatFs(context.filesDir.absolutePath)

        return stat.availableBlocksLong * stat.blockSizeLong
    }

    private fun getCanRecordInSec(context: Context, audioBitRate: Int = 96000, videoBitRate: Int = 6_000_000) : Long {
        val available = getAvailableMemory(context)
        val bitrate = audioBitRate + videoBitRate
        return available / bitrate
    }

    fun getCanRecordFormat(context: Context, pattern: String = "HH시간 mm분", audioBitRate: Int = 96000, videoBitRate: Int = 6_000_000) : String {
        val sec = getCanRecordInSec(context, audioBitRate, videoBitRate)
        val date = Date(sec)
        val format = SimpleDateFormat(pattern)

        return format.format(date)
    }

    fun moveFile2PrivateFolder(context: Context, file: File) : Boolean = file.renameTo(File(context.filesDir.absolutePath, file.name))
}