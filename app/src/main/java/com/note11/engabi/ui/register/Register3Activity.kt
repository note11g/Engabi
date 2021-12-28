package com.note11.engabi.ui.register

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.insets.ProvideWindowInsets
import com.note11.engabi.ui.theme.EngabiTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.Text
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.note11.engabi.model.UserModel
import com.note11.engabi.ui.main.MainActivity
import com.note11.engabi.ui.theme.*
import com.note11.engabi.util.DataUtil
import kotlinx.coroutines.launch

class Register3Activity : ComponentActivity() {
    private val viewModel by viewModels<Register3ViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngabiTheme {
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize().background(MaterialTheme.colors.background)
                    ) {
                        val (indicator, content) = createRefs()

                        ProgressIndicator(
                            nowIndex = 2,
                            modifier = Modifier.constrainAs(indicator) {
                                top.linkTo(parent.top)
                                bottom.linkTo(content.top)
                                start.linkTo(parent.start)
                                end.linkTo(parent.end)
                            }
                        )
                        ContentSection(modifier = Modifier.constrainAs(content) {
                            top.linkTo(parent.top)
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start, margin = 24.dp)
                            end.linkTo(parent.end, margin = 24.dp)

                            width = Dimension.fillToConstraints
                        })
                    }
                }
            }
        }
    }

    @Composable
    private fun ContentSection(modifier: Modifier = Modifier) {
        val name = remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        Column(modifier) {
            Column {
                Text("실명을 입력해주세요", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
                Spacer(Modifier.size(4.dp))
                Text(
                    "입력하신 모든 정보는 공개되지 않아요",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Gray300
                )
            }
            Spacer(Modifier.size(64.dp))
            CustomTextField(
                Modifier.fillMaxWidth(), name.value,
                {
                    name.value = it
                }, imeAction = ImeAction.Done, keyboardType = KeyboardType.Text, hint = "김윤주"
            ) { focusManager.clearFocus() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = Gray300)
            )
            Spacer(Modifier.size(80.dp))
            Text(
                "마치기",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.medium)
                    .background(BlueAccent)
                    .clickable { goToNextStep(name.value) }
                    .padding(vertical = 16.dp)
            )
        }
    }

    private fun goToNextStep(name: String) {
        if (name.isEmpty() || name.length < 2) {
            Toast.makeText(this, "이름을 정확히 입력해주세요.", Toast.LENGTH_SHORT).show()
        } else {
            intent.run {
                val uid = getStringExtra("uid")
                val birth = getStringExtra("birth")
                val phone = getStringExtra("phone")

                listOf(uid, birth, phone, name).forEach {
                    if (it.isNullOrEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "오류가 발생했습니다. 앱을 재실행해주세요.",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@run
                    }
                }

                val user = UserModel(uid!!, name, birth!!, phone!!)

                viewModel.registerUser(user) { error ->
                    if (error == null) {
                        lifecycleScope.launch {
                            DataUtil(this@Register3Activity).setUserInfo(user)

                            Toast.makeText(
                                applicationContext,
                                "회원가입에 성공했습니다.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // todo : change to start point for SplashActivity
                            startActivity(Intent(this@Register3Activity, MainActivity::class.java))
                            ActivityCompat.finishAffinity(this@Register3Activity)
                        }
                    } else {
                        Log.e("Register3", error.toString())
                        Toast.makeText(
                            applicationContext,
                            "오류가 발생했습니다. 네트워크 상태를 확인해주세요.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }
}
