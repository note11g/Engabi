package com.note11.engabi.util

import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.note11.engabi.foreground.RecordService

class RecordDestroyService : IntentService("RecordDestroyService") {

    override fun onHandleIntent(intent: Intent?) {
        RecordService.instance?.destroy()
        Log.i("MediaRecorder", (RecordService.instance == null).toString())
    }
}