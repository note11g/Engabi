package com.note11.engabi.foreground

import android.app.Service
import android.content.Intent
import android.os.IBinder

class RecordDestroyService : Service() {
    override fun onCreate() {
        RecordService.instance?.destroy()
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }
}