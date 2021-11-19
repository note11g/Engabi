package com.note11.engabi

import android.app.Application
import com.google.firebase.FirebaseApp
import com.kakao.sdk.common.KakaoSdk

class EngabiApplication : Application() {
    companion object {
        var firebaseApp: FirebaseApp? = null
    }

    override fun onCreate() {
        super.onCreate()
        firebaseApp = FirebaseApp.initializeApp(this)
        KakaoSdk.init(this, "5495cbd8be1677e13b1960ba6a714e75")
    }
}