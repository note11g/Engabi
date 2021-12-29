package com.note11.engabi.util

import android.content.Context
import android.hardware.fingerprint.FingerprintManager
import android.os.Build
import android.os.CancellationSignal
import android.security.KeyPairGeneratorSpec
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import androidx.biometric.BiometricPrompt
import androidx.fragment.app.FragmentActivity
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.KeyGeneratorSpi

object AuthUtil {
    private val DEFAULT_KEY: String = "FingerPrint Key"

    @RequiresApi(Build.VERSION_CODES.P)
    fun bioAuth(activity: FragmentActivity, success: ((BiometricPrompt.AuthenticationResult) -> Unit)? = null, failed: (() -> Unit)? = null, error: ((Int, String) -> Unit)? = null) {
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

    @RequiresApi(Build.VERSION_CODES.M)
    fun fingerAuth(context : Context, success: ((FingerprintManager.AuthenticationResult?) -> Unit)? = null, failed: (() -> Unit)? = null, error: ((Int, String) -> Unit)? = null) {
        val fingerPrint = context.getSystemService(FingerprintManager::class.java)
        if(!fingerPrint.isHardwareDetected && fingerPrint.hasEnrolledFingerprints()) {
            failed?.invoke()
            return;
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val keyParameter = KeyGenParameterSpec.Builder(DEFAULT_KEY, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
            .setUserAuthenticationRequired(true)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
            .build()
        keyGenerator.init(keyParameter)
        keyGenerator.generateKey()

        val cipher = Cipher.getInstance("${KeyProperties.KEY_ALGORITHM_AES}/${KeyProperties.BLOCK_MODE_CBC}/${KeyProperties.ENCRYPTION_PADDING_PKCS7}")
        cipher.init(Cipher.ENCRYPT_MODE, keyGenerator.generateKey())

        val cancellation = CancellationSignal();
        cancellation.setOnCancelListener(failed)
        fingerPrint.authenticate(FingerprintManager.CryptoObject(cipher), cancellation, 0, object: FingerprintManager.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                super.onAuthenticationError(errorCode, errString)
                error?.invoke(errorCode, errString.toString())
            }

            override fun onAuthenticationSucceeded(result: FingerprintManager.AuthenticationResult?) {
                super.onAuthenticationSucceeded(result)
                success?.invoke(result)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                failed?.invoke()
            }
        },  null)
    }
}