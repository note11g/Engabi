package com.note11.engabi

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.util.Utility

class EngabiApplication : Application() {
    companion object {
        var firebaseApp: FirebaseApp? = null
    }

    override fun onCreate() {
        super.onCreate()
        firebaseApp = FirebaseApp.initializeApp(this)
        KakaoSdk.init(this, "5495cbd8be1677e13b1960ba6a714e75")

        val keyHash = Utility.getKeyHash(this)
        Log.i("AndroidKeyHash", keyHash)
    }
}