package com.note11.engabi.util

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.gun0912.tedpermission.coroutine.TedPermission
import com.note11.engabi.foreground.RecordService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.Executors

class AccessibilityService : AccessibilityService() {
    var es = Executors.newFixedThreadPool(1)
    private val macroSystem: MacroSystem = MacroSystem()

    companion object {
        const val LONG_PRESS_TIME: Long = 500
    }

    var instance: AccessibilityService? = null

    override fun onCreate() {
        super.onCreate()

        if (instance == null) {
            instance = this

            macroSystem.addKeyCode(KeyEvent.KEYCODE_VOLUME_DOWN)
            macroSystem.addKeyCode(KeyEvent.KEYCODE_VOLUME_UP)
        } else {
            stopSelf()
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        val result = super.onKeyEvent(event)

        val keyData: KeyData? = macroSystem.searchKeyCode(event.keyCode)
        Log.d("KeyEvent", "${(keyData == null)} ${event.keyCode}")
        if (keyData == null) return result

        keyData.isClickNow = event.action == KeyEvent.ACTION_DOWN
        keyData.clickTime = Date().time

        if (!macroSystem.isWorkerRunnable) macroSystem.startWorker()

        return result
    }

    override fun onAccessibilityEvent(e: AccessibilityEvent) {}
    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onInterrupt() {}

    inner class MacroSystem {
        private val keyCode = ArrayList<KeyData>()
        var isWorkerRunnable: Boolean = false

        fun addKeyCode(key: Int): MacroSystem {
            if (searchKeyCode(key) != null) return this

            val keyData = KeyData()
            keyData.keyCode = key
            keyCode.add(keyData)

            return this
        }

        fun searchKeyCode(key: Int): KeyData? {
            for (keyData in keyCode) {
                if (keyData.keyCode == key) return keyData
            }
            return null
        }

        fun isAllChecked(): Boolean {
            for (keyData in keyCode) {
                if (!keyData.isClickNow) return false
            }
            //todo ????????? ??????
            return true
        }

        fun isAllLongChecked(): Boolean {
            for (keyData in keyCode) {
                if (!(keyData.isClickNow && Date().time - keyData.clickTime >= LONG_PRESS_TIME)) return false
            }

            return true
        }

        fun isAllNotChecked(): Boolean {
            for (keyData in keyCode) {
                if (keyData.isClickNow) return false
            }

            return true
        }

        fun startWorker() {
            es.submit(ButtonClickWorker(this))
        }

        fun start() {
            if (RecordService.isRunnable) {
                RecordService.instance?.stopSelf()
            } else {
                CoroutineScope(Dispatchers.Main).launch {
                    val permissionResult =
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                            TedPermission.create()
                                .setPermissions(
                                    Manifest.permission.RECORD_AUDIO,
                                    Manifest.permission.FOREGROUND_SERVICE
                                )
                                .check()
                        } else {
                            TedPermission.create()
                                .setPermissions(
                                    Manifest.permission.RECORD_AUDIO
                                )
                                .check()
                        }
                    if (permissionResult.isGranted) {
                        ForegroundServiceUtils.runForegroundService(
                            applicationContext,
                            ForegroundServiceUtils.ForegroundServiceType.SOUND_RECORD_SERVICE
                        )
                    } else {
                        Toast.makeText(applicationContext, "????????? ?????? ??????????????????", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    inner class KeyData {
        var keyCode = 0
        var clickTime: Long = -1
        var isClickNow = false
    }

    inner class ButtonClickWorker(val macroSystem: MacroSystem) : Runnable {
        override fun run() {
            val timer = Timer()
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    if (macroSystem.isAllLongChecked()) {
                        macroSystem.start()
                        macroSystem.isWorkerRunnable = false
                        timer.cancel()
                    }

                    if (macroSystem.isAllNotChecked()) {
                        macroSystem.isWorkerRunnable = false
                        timer.cancel()
                    }
                }
            }
            timer.schedule(task, LONG_PRESS_TIME)
        }
    }
}