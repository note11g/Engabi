package com.note11.engabi.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.StatFs
import androidx.core.content.FileProvider
import java.io.File

object GetFilesUtil {
    fun getFiles(context: Context) : Array<File> = File(context.filesDir.absolutePath).listFiles()

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

    fun getAvailableMemory(context: Context) : Long {
        val stat = StatFs(context.filesDir.absolutePath)

        return stat.availableBlocksLong * stat.blockSizeLong
    }

    fun moveFile2PrivateFolder(context: Context, file: File) : Boolean = file.renameTo(File(context.filesDir.absolutePath, file.name))
}