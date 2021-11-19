package com.note11.engabi.util

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import java.io.File

class GetFilesUtil {
    companion object {
        fun getFiles(context : Context) = File(context.filesDir.absolutePath).listFiles()

        fun getFileFromPath(path : String) = File(path)

        fun playAudioFiles(file : File) : MediaPlayer {
            var mediaPlayer = MediaPlayer()
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.setDataSource(file.absolutePath)
            mediaPlayer.prepare()
            mediaPlayer.start()

            return mediaPlayer
        }
    }
}