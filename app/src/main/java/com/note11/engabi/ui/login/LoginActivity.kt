package com.note11.engabi.ui.login

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.kakao.sdk.auth.AuthApiClient
import com.kakao.sdk.auth.AuthCodeClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.auth.rx
import com.kakao.sdk.user.UserApiClient
import com.note11.engabi.EngabiApplication
import com.note11.engabi.R
import com.note11.engabi.ui.register.Register1Activity
import com.note11.engabi.ui.theme.EngabiTheme
import com.note11.engabi.ui.theme.Shapes
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch

class LoginActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngabiTheme {
                Column(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painterResource(id = R.drawable.ic_login_logo),
                        "은가비 로고",
                        Modifier.fillMaxWidth(0.8f)
                    )
                    Image(
                        painterResource(id = R.drawable.kakao_login_large_wide),
                        "은가비 로고",
                        Modifier
                            .fillMaxWidth(0.8f)
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