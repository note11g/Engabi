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
import com.note11.engabi.util.RecordDestoryService
import java.io.File
import java.lang.Exception
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

        //음성 기록 파일 생성(테스트용)
//        var file = File(applicationContext.getExternalFilesDir(null), "${UUID.randomUUID()}.mp3")

        val file = File(
            applicationContext.filesDir,
            "REC_${
                SimpleDateFormat(
                    "yy-MM-dd_HH-mm-ss_SSSS",
                    Locale.getDefault()
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

        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContentText("녹음중입니다.")
            .setContentIntent(PendingIntent.getService(applicationContext, 0, Intent(applicationContext, RecordDestoryService::class.java), PendingIntent.FLAG_CANCEL_CURRENT))

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
        for(file in GetFilesUtil.getFiles(applicationContext)) {
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
        mediaRecorder = MediaRecorder()
        mediaRecorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder?.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder?.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT)
//        mediaRecorder?.setVideoEncodingBitRate(0)
//        mediaRecorder?.setAudioEncodingBitRate(25000)

        //음성 기록 파일 세팅
        mediaRecorder?.setOutputFile(filePath)

        //음성 녹음 시작
        mediaRecorder?.prepare()
        mediaRecorder?.start()
    }

    private fun recordStop() {
        //음성 녹음 중지
        mediaRecorder?.stop()
        mediaRecorder?.release()

        //파일 경로 확인용
        Log.i("MediaRecorder", "저장 : $filePath")
        Toast.makeText(applicationContext, "저장되었습니다.", Toast.LENGTH_SHORT)
        mediaRecorder = null
    }

    private fun destroy() {
        stopForeground(true)
    }
}