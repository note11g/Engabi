package com.note11.engabi.util

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity

object AuthUtil {

    @RequiresApi(Build.VERSION_CODES.P)
    fun bioAuth(activity: FragmentActivity, success: ((BiometricPrompt.AuthenticationResult) -> Unit)? = null, error: ((Int, String) -> Unit)? = null, failed: (() -> Unit)? = null) {
        val bioPrompt = BiometricPrompt(activity, activity.mainExecutor, object: BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                error?.invoke(errorCode, errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                success?.invoke(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                failed?.invoke()
            }
        })

        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("생체 인증")
            .setNegativeButtonText("취소")
            .build()

        bioPrompt.authenticate(info)
    }
}