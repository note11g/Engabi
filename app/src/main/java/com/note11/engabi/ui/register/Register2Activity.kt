package com.note11.engabi.ui.register

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material.Text
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.note11.engabi.ui.theme.*

import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import java.util.*


class Register2Activity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngabiTheme {
                ProvideWindowInsets(windowInsetsAnimationsEnabled = true) {
                    ConstraintLayout(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        val (indicator, content) = createRefs()

                        ProgressIndicator(
                            nowIndex = 1,
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
        val phone = remember { mutableStateOf("") }
        val focusManager = LocalFocusManager.current

        Column(modifier) {
            Column {
                Text("사용중인 전화번호를\n입력해주세요", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.size(4.dp))
                Text(
                    "입력하신 모든 정보는 공개되지 않아요",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = LightGray300
                )
            }
            Spacer(Modifier.size(64.dp))
            CustomTextField(
                Modifier.fillMaxWidth(),
                phone.value,
                {
                    if (it.toIntOrNull() == null && it.isNotEmpty()) return@CustomTextField
                    if (it.length >= 11) {
                        focusManager.clearFocus()
                        if (it.length > 11) return@CustomTextField
                    }
                    phone.value = it
                },
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Phone,
                hint = "01012345678"
            ) { focusManager.clearFocus() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(color = LightGray300)
            )
            Spacer(Modifier.size(80.dp))
            Text(
                "다음",
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                color = PureWhite,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(Shapes.medium)
                    .background(LightBlue)
                    .clickable { goToNextStep(phone.value) }
                    .padding(vertical = 16.dp)
            )
        }
    }

    private fun goToNextStep(phoneNum: String) {
        if (phoneNum.isEmpty() && phoneNum.length != 11) {
            Toast.makeText(this, "전화번호를 정확히 입력해주세요.", Toast.LENGTH_SHORT).show()
        } else {
            Intent(this, Register3Activity::class.java).let{
                it.putExtra("phone", phoneNum)
                it.putExtra("uid", intent.getStringExtra("uid") ?: "")
                it.putExtra("birth", intent.getStringExtra("birth") ?: "")
                startActivity(it)
            }
        }
    }
}
