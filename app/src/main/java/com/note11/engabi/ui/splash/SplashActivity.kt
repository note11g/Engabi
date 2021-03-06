package com.note11.engabi.ui.splash

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.AlertDialog
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.kakao.sdk.common.util.Utility
import com.note11.engabi.R
import com.note11.engabi.ui.login.LoginActivity
import com.note11.engabi.ui.main.MainActivity
import com.note11.engabi.ui.theme.*
import com.note11.engabi.util.DataUtil
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : ComponentActivity() {
    private val viewModel by viewModels<SplashViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val keyHash = Utility.getKeyHash(this)
        Log.d("kakaoKeyHash", keyHash)
    }

    override fun onResume() {
        super.onResume()
        setContent {
            EngabiTheme {
                Surface(color = Color.Transparent) {
                    Image(
                        painterResource(id = R.drawable.ic_splash),
                        "",
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    CheckPermissions()
                }
            }
        }
    }

    @Composable
    private fun CheckPermissions() {
        if (!checkBatteryOptimizationPermissions()) {
            BatAlertDialog {
                Intent().run {
                    action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    data = Uri.parse("package:$packageName")
                    startActivity(this)
                }
            }
        } else {
            if (false && !checkAccessibilityPermissions()) { //todo : delete false on release
                AccAlertDialog {
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    Toast.makeText(
                        applicationContext,
                        "????????? ??????????????? ???????????? ???????????? ????????? ????????? ??????????????????.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                whereGoCheck()
            }
        }
    }

    private fun whereGoCheck(delay: Boolean = true) = lifecycleScope.launch {
        val isFirst = DataUtil(applicationContext).getUserInfoOnce() == null
        if (delay) delay(1000L)
        startActivity(
            Intent(
                this@SplashActivity,
                if (isFirst) LoginActivity::class.java else MainActivity::class.java
            )
        )
        this@SplashActivity.finish()
    }

    @Composable
    private fun AccAlertDialog(onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text(text = "?????? ???????????? ??????", letterSpacing = 0.sp, color = White)
                }
            },
            title = {
                Text(
                    text = "????????? ????????????",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
            },
            text = {
                Text(
                    text = "?????????????????? ?????? ?????? ????????? 5?????? ????????? ????????? ????????? ?????? ???????????? ????????? ?????????.\n?????? ????????? ???????????? ????????? ????????? ????????????.",
                    letterSpacing = 0.sp,
                    color = Gray300
                )
            }, backgroundColor = Blue600
        )
    }

    @Composable
    private fun BatAlertDialog(onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    Text(text = "?????? ???????????? ??????", letterSpacing = 0.sp, color = White)
                }
            },
            title = {
                Text(
                    text = "????????? ????????????",
                    color = White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
            },
            text = {
                Text(
                    text = "?????????????????? ?????? ?????? ????????? 5?????? ????????? ????????? ????????? ?????? ???????????? ????????? ?????????.\n?????? ????????? ???????????? ????????? ????????? ?????? ????????? ????????????.",
                    letterSpacing = 0.sp,
                    color = Gray300
                )
            }, backgroundColor = Blue600
        )
    }

    private fun checkAccessibilityPermissions(): Boolean {
        val accessibilityManager =
            getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager

        val list =
            accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.DEFAULT)

        list.forEach { info ->
            if (info.resolveInfo.serviceInfo.packageName.equals(application.packageName)) return true
        }
        return false
    }

    private fun checkBatteryOptimizationPermissions(): Boolean {
        val packageName = packageName
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager

        return pm.isIgnoringBatteryOptimizations(packageName)
    }
}
