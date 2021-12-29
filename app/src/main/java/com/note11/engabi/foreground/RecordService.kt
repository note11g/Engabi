package com.note11.engabi.foreground

import android.app.*
import android.content.Intent
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.note11.engabi.R
import com.note11.engabi.util.GetFilesUtil
import com.note11.engabi.util.RecordDestroyService
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException
import java.text.SimpleDateFormat
import java.util.*

class RecordService : Service() {
    companion object {
        var isRunnable: Boolean = false
        var instance: RecordService? = null
    }

    private var mediaRecorder: MediaRecorder? = null
    lateinit var filePath: String

    override fun onCreate() {
        super.onCreate()
        instance?.destroy()
        isRunnable = true

        //음성 녹음을 외부에서 종료시키기 위해 instance
        instance = this

        //Foreground Notification 띄우기
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startForegroundService()
        else startForeground(1, Notification())

        val file = File(
            applicationContext.filesDir,
            "REC_${
                SimpleDateFormat(
                    "yyMMdd_HHmmss_SSS",
                    Locale.KOREA
                ).format(System.currentTimeMillis())
            }.mp3"
        )

        try {
            file.createNewFile()
        } catch (e: Exception) {
            Log.e("FileCreateError", e.toString())
        }
        filePath = file.absolutePath
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startForegroundService() {
        //foreground notification 실행 (must needed)
        val CHANNEL_ID = "default_background_service"
        val channel =
            NotificationChannel(CHANNEL_ID, "사운드 서비스 백그라운드 실행", NotificationManager.IMPORTANCE_HIGH)

        val nm = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (nm.getNotificationChannel(CHANNEL_ID) == null)
            nm.createNotificationChannel(channel)
        val builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)

        val intent = Intent(applicationContext, RecordDestroyService::class.java)
        intent.action = "Close"
        val pending =
            PendingIntent.getService(applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContentText("녹음중입니다.")
            .addAction(0, "종료", pending)
            .priority = NotificationCompat.PRIORITY_LOW

        startForeground(1, builder.build())
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        recordStart()
        Toast.makeText(applicationContext, "녹음이 시작되었습니다.", Toast.LENGTH_SHORT).show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        recordStop()
        stopForeground(true)
        isRunnable = false
        instance = null

        Log.i("MediaRecorder", "녹음 서비스 종료")
        for (file in GetFilesUtil.getFiles(applicationContext)) {
            Log.i("MediaRecorder", file.name)
        }
    }

    private fun recordStart() {
        //음성 녹음이 켜져있으면 중지
        try {
            mediaRecorder?.stop()
        } catch (e: Exception) {
        }

        //음성 세팅
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }


        mediaRecorder?.run {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.HE_AAC)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(96000)
            setOutputFile(filePath)

            prepare()
            start()
        }
    }

    private fun recordStop() {
        mediaRecorder?.apply {
            stop()
            release()
        }

        //파일 경로 확인용
        Log.i("MediaRecorder", "저장 : $filePath")
        Toast.makeText(applicationContext, "저장되었습니다.", Toast.LENGTH_SHORT).show()

        //TODO: remove this(test)
//        GetFilesUtil.playAudioIntent(applicationContext, File(filePath))

        mediaRecorder = null
    }

    fun destroy() {
        stopSelf()
    }
}