package com.note11.engabi.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.user.UserApiClient
import com.note11.engabi.R
import com.note11.engabi.ui.register.Register1Activity
import com.note11.engabi.ui.theme.*

class LoginActivity : ComponentActivity() {

    private val helpTexts = listOf ( "빠른 증거수집 기능" to "위급한 상황시\n상황을 빠르게 녹음 혹은 동영상 촬영", "신고 기능" to "일반 신고 및 긴급신고를 예약하거나\n하지 않고 진행할 수 있음", "커뮤니티" to "익명으로\n모두 다 함께 자유롭게 놀아요" )

    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EngabiTheme {
                Column(
                    Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colors.background),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Icon(
                        painterResource(id = R.drawable.ic_login_logo),
                        "은가비 로고",
                        tint = White,
                        modifier = Modifier.height(40.dp)
                    )

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val pagerState = rememberPagerState()
                        HorizontalPager(count = 3, state = pagerState, modifier = Modifier.wrapContentHeight().fillMaxWidth()) { n ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(helpTexts[n].first, color= White, fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center)
                                Spacer(Modifier.size(18.dp))
                                Text(helpTexts[n].second, color = Gray300, fontWeight = FontWeight.Normal, fontSize = 16.sp, textAlign = TextAlign.Center)
                            }
                        }
                        Spacer(Modifier.size(36.dp))
                        HorizontalPagerIndicator(pagerState = pagerState, activeColor = White, inactiveColor = Gray500)
                    }

                    Image(
                        painterResource(id = R.drawable.kakao_login_large_wide),
                        "카카오톡 로그인",
                        Modifier
                            .height(48.dp)
                            .clip(Shapes.small)
                            .clickable { startLogin() })
                }
            }
        }
    }

    private fun startLogin() {
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                Log.e("test", error.message.toString())
            } else if (token != null) {
                loginSucceed()
            }
        }

        UserApiClient.instance.let {
            if (UserApiClient.instance.isKakaoTalkLoginAvailable(this)) {
                it.loginWithKakaoTalk(this, callback = callback)
            } else {
                it.loginWithKakaoAccount(this, callback = callback)
            }
        }
    }

    private fun loginSucceed() {
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                Log.e("LoginActivity", error.toString())
                Toast.makeText(
                    this,
                    "로그인 오류입니다. 네트워크를 확인해주세요. ${error.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            } else if (user != null) {
                Intent(this, Register1Activity::class.java).let {
                    it.putExtra("uid", "${user.id}")
                    startActivity(it)
                }
            }
        }
    }
}