package com.note11.engabi.util

import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

class GetFilesUtil {
    companion object {
        fun getFiles(context: Context) = File(context.filesDir.absolutePath).listFiles()

        fun getFileFromPath(path: String) = File(path)

        fun playAudioIntent(context: Context, file: File) {
            val intent = Intent()
            intent.action = Intent.ACTION_VIEW
            intent.setDataAndType(Uri.fromFile(file), "audio/*")
            context.startActivity(intent)
        }

        fun moveFile2PrivateFolder(context: Context, file: File) : Boolean = file.renameTo(File(context.filesDir.absolutePath, file.name))
    }
}