package com.note11.engabi.util

import android.content.Context
import android.content.Intent
import android.os.Build
import com.note11.engabi.foreground.RecordService
import java.io.File

object ForegroundServiceUtils {
    fun runForegroundService(context: Context, type: ForegroundServiceType) {
        val target = when (type) {
            ForegroundServiceType.SOUND_RECORD_SERVICE -> RecordService::class.java
            else -> null
        } ?: return

        val intent = Intent(context, target)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            context.startForegroundService(intent)
        else
            context.startService(intent)
    }

    fun getAudioFiles(context: Context): ArrayList<File> {
        val fileFolder = context.getExternalFilesDir(null)
        val lists = ArrayList<File>()
        if (fileFolder == null) return lists

        for (file in fileFolder.listFiles()!!) {
            if (file.isDirectory || !file.name.lowercase().endsWith(".mp3")) continue
            lists.add(file)
        }

        return lists
    }

    enum class ForegroundServiceType {
        STT_SERVICE, SOUND_RECORD_SERVICE
    }
}

