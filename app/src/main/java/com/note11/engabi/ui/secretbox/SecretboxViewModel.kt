package com.note11.engabi.ui.secretbox

import android.app.Application
import android.media.MediaScannerConnection
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import com.note11.engabi.util.GetFilesUtil
import java.io.File

class SecretboxViewModel(application: Application) : AndroidViewModel(application) {

    val fileList = mutableStateOf<List<File>>(listOf())

    val selectFiles = mutableStateListOf<File>()

    fun loadFiles() {
        val list = GetFilesUtil.getFiles(getApplication(), extension = listOf("mp3", "mp4"))
        fileList.value = list.sortedWith(compareBy { it.lastModified() }).reversed()
        for (file in list) {
            Log.d("SecretboxViewModel", "${file.name} (${file.absolutePath})")
        }
    }

    fun deleteSelectedFiles() {
        try {
            for (file in selectFiles) {
                Log.d("[delete] SecretboxViewModel", "${file.name} (${file.absolutePath})")
                file.delete()
            }
            loadFiles()
        } catch (e: Exception) {
            Log.e("SecretboxViewModel", e.toString())
        }
    }

}