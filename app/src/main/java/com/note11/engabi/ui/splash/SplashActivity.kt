package com.note11.engabi.ui.splash

import android.accessibilityservice.AccessibilityServiceInfo
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
        setContent {
            EngabiTheme {
                Surface(color = Color.Transparent) {
                    Image(
                        painterResource(id = R.drawable.ic_splash),
                        "",
                        Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (!checkAccessibilityPermissions()) {
                        AccAlertDialog {
                            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                            Toast.makeText(
                                applicationContext,
                                "설치된 서비스에서 은가비를 선택하여 접근성 설정을 허용해주세요.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        whereGoCheck()
                    }
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        whereGoCheck(false)
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
                    Text(text = "권한 허용하러 가기", letterSpacing = 0.sp)
                }
            },
            title = {
                Text(
                    text = "권한이 필요해요",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp
                )
            },
            text = {
                Text(
                    text = "은가비에서는 볼륨 버튼 두개를 5초간 동시에 누르면 녹음을 바로 시작하는 기능이 있어요.\n해당 기능을 위해서는 접근성 권한이 필요해요.",
                    letterSpacing = 0.sp,
                    color = LightGray400
                )
            }, backgroundColor = PureWhite
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
}
