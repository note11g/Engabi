package com.note11.engabi.util

import android.app.IntentService
import android.content.Intent
import com.note11.engabi.foreground.RecordService

class RecordDestoryService : IntentService("RecordDestory") {

    override fun onHandleIntent(intent: Intent?) {
        RecordService.instance?.stopSelf()
        stopSelf();
    }
}