package com.note11.engabi.ui.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.lifecycleScope
import com.gun0912.tedpermission.coroutine.TedPermission
import com.note11.engabi.R
import com.note11.engabi.ui.community.CommunityMainActivity
import com.note11.engabi.ui.login.LoginActivity
import com.note11.engabi.ui.secretbox.SecretboxActivity
import com.note11.engabi.ui.theme.*
import com.note11.engabi.ui.util.CustomBottomDrawer
import com.note11.engabi.ui.video.VideoActivity
import com.note11.engabi.util.ForegroundServiceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    @ExperimentalMaterialApi
    private lateinit var drawerState: BottomDrawerState

    @ExperimentalMaterialApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngabiTheme {
                drawerState = rememberBottomDrawerState(initialValue = BottomDrawerValue.Closed)

                CustomBottomDrawer(
                    drawerState = drawerState,
                    drawerContent = { VoiceRecordBottomDrawer() }) {
                    Scaffold {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = 24.dp,
                                vertical = 32.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            item { HeaderSection() }
                            item { ContentSection() }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun HeaderSection() {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "로고",
                modifier = Modifier.height(32.dp)
            )
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "더보기",
                modifier = Modifier
                    .clickable {

                    }
                    .size(32.dp)
                    .padding(4.dp)
                    .rotate(90.0f)
            )
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun ContentSection() {
        val lockState = remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()

        ConstraintLayout(Modifier.fillMaxWidth()) {
            val (voice, video) = createRefs()

            GoBtnType1(
                modifier = Modifier.constrainAs(voice) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(video.start, margin = 4.dp)

                    width = Dimension.fillToConstraints
                },
                title = "녹음",
                subTitle = "상황을 음성으로 기록해요",
                painterId = R.drawable.home_voice_rec,
                iconHeight = 113.dp
            ) {
                coroutineScope.launch {
                    drawerState.open()
                }
            }

            GoBtnType1(
                modifier = Modifier.constrainAs(video) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(voice.end, margin = 4.dp)
                    end.linkTo(parent.end)

                    width = Dimension.fillToConstraints
                },
                title = "동영상 촬영",
                subTitle = "상황을 영상으로 촬영해요",
                painterId = R.drawable.home_video_rec,
                iconHeight = 81.dp
            ) {
                lifecycleScope.launch {
                    val permissionResult =
                        TedPermission.create()
                            .setPermissions(Manifest.permission.CAMERA)
                            .check()
                    if (permissionResult.isGranted) {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                VideoActivity::class.java
                            )
                        )
                    } else {
                        Toast.makeText(this@MainActivity, "카메라 권한을 허용해주세요", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        if (lockState.value) ListBox("비밀 창고", onClick = {
            startActivity(Intent(this, SecretboxActivity::class.java))
//            lockState.value = false
        }) { onClick ->
            ListBoxBlurItem(onClick)
        }
        else ListBox("비밀 창고", onClick = {
//            lockState.value = true
            startActivity(Intent(this, SecretboxActivity::class.java))
        }) {
            Column {
                for (i in 1..3) {
                    ListBoxItem(
                        "REC_211110_${4 - i}",
                        if (i % 2 == 0) "녹음파일" else "동영상파일",
                        "${17 - i}시간 전"
                    ) {}
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        GoBtnType2(
            title = "신고하기",
            subTitle = "수집한 증거들을 이용하여 신고할 수 있어요",
            painterId = R.drawable.home_report,
            iconHeight = 40.dp
        ) {
            Toast.makeText(applicationContext, "신고 기능은 아직 준비중입니다.", Toast.LENGTH_LONG).show()
        }

        Spacer(Modifier.size(8.dp))

        ListBox("커뮤니티", onClick = {
            startActivity(Intent(this@MainActivity, CommunityMainActivity::class.java))
            Toast.makeText(applicationContext, "커뮤니티 기능은 아직 준비중입니다.", Toast.LENGTH_LONG).show()
        }) {
            Column {
                val strList = listOf(
                    "나 오늘 뭐 먹을까 같이 고민 좀 해주라",
                    "혹시 1등급인 친구들 어떻게 공부하는 지 물어봐도 될까?",
                    "아 게시글 뭐 적지...ㅠㅠ"
                )

                for (i in 1..3) {
                    ListBoxItem(
                        "작성글 제목 ${4 - i}",
                        strList[i - 1],
                        "${17 - i}시간 전"
                    ) {
                        Toast.makeText(applicationContext, "커뮤니티 기능은 아직 준비중입니다.", Toast.LENGTH_LONG)
                            .show()
                    }
                }
            }
        }
    }

    @Composable
    private fun GoBtnType1(
        modifier: Modifier = Modifier,
        title: String,
        subTitle: String,
        painterId: Int,
        iconHeight: Dp,
        onClick: () -> Unit
    ) {
        ConstraintLayout(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(Shapes.medium)
                .background(PureWhite)
                .clickable { onClick() }
        ) {
            val (titles, img) = createRefs()

            Column(Modifier.constrainAs(titles) {
                top.linkTo(parent.top, margin = 18.dp)
                start.linkTo(parent.start, margin = 18.dp)
            }) {
                Text(subTitle, color = LightGray300, fontSize = 12.sp)
                Text(title, color = PureBlack, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Image(
                painter = painterResource(id = painterId),
                contentDescription = "아이콘",
                modifier = Modifier
                    .constrainAs(img) {
                        bottom.linkTo(parent.bottom)
                        end.linkTo(parent.end)
                    }
                    .height(iconHeight))
        }
    }

    @Composable
    private fun GoBtnType2(
        title: String,
        subTitle: String,
        painterId: Int,
        iconHeight: Dp,
        onClick: () -> Unit
    ) {
        Row(
            Modifier
                .clip(Shapes.medium)
                .background(PureWhite)
                .clickable { onClick() }
                .padding(vertical = 20.dp, horizontal = 18.dp)
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Image(painterResource(id = painterId), "아이콘", modifier = Modifier.height(iconHeight))
            Spacer(Modifier.size(16.dp))
            Column {
                Text(subTitle, fontSize = 12.sp, color = LightGray300)
                Spacer(Modifier.size(2.dp))
                Text(title, fontSize = 20.sp, color = LightRed, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun ListBox(
        name: String,
        showMore: Boolean = true,
        onClick: () -> Unit,
        itemSection: @Composable ((onClick: () -> Unit) -> Unit)
    ) {
        Column(
            Modifier
                .clip(Shapes.medium)
                .background(PureWhite)
        ) {
            Text(
                "$name >",
                fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onClick() }
                    .padding(start = 18.dp, top = 20.dp, bottom = 10.dp)
                    .fillMaxWidth()
            )
            Surface(Modifier.fillMaxWidth()) {
                itemSection(onClick)
            }

            if (showMore) Text(
                "더보기",
                color = LightGray300,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick() }
                    .padding(vertical = 14.dp)
            )
        }
    }

    @Composable
    private fun ListBoxItem(
        title: String,
        subTitle: String,
        infoText: String = "",
        onClick: () -> Unit
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 12.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                Modifier
                    .weight(1f)
            ) {
                Text(title, maxLines = 1, fontSize = 18.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.size(2.dp))
                Text(
                    subTitle,
                    maxLines = 1,
                    fontSize = 12.sp,
                    color = LightGray300,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                infoText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = LightGray300,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }

    @Composable
    private fun ListBoxBlurItem(onClick: () -> Unit) {
        Box(
            Modifier
                .fillMaxWidth()
                .clickable { onClick() }, contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.blur),
                contentDescription = "",
                Modifier.fillMaxWidth()
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Filled.Lock, "", Modifier.size(28.dp), LightGray300)
                Spacer(Modifier.size(8.dp))
                Text(
                    "현재는 잠금모드예요",
                    fontSize = 14.sp,
                    color = LightGray400,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.size(2.dp))
                Text(
                    "눌러서 잠금을 해제하면 비밀창고 내용을 볼 수 있어요",
                    fontSize = 12.sp,
                    color = LightGray300,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun VoiceRecordBottomDrawer() {
        val coroutineScope = rememberCoroutineScope()
        Column(Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("녹음하기", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = PureWhite)
                Icon(Icons.Filled.Clear,
                    contentDescription = "delete",
                    tint = DarkGray600,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            coroutineScope.launch { drawerState.close() }
                        }
                        .padding(4.dp))
            }

            Text(
                "녹음 시작하기를 누르면 앱에서 나가지며, 녹음이 시작됩니다.\n" +
                        "녹음을 중지하시려면, 볼륨 버튼 두개를 동시에 약 2초간 눌러주세요.",
                fontWeight = FontWeight.Medium,
                color = LightGray300,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(vertical = 32.dp)
                    .fillMaxWidth()
            )

            Text("녹음 시작하기",
                fontWeight = FontWeight.Medium,
                color = PureWhite,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(Shapes.medium)
                    .background(
                        DarkRedRecording
                    )
                    .fillMaxWidth()
                    .clickable {
                        coroutineScope.launch {
                            startRecording()
                        }
                    }
                    .padding(vertical = 16.dp))
            Text(
                text = "녹음이 진행될 때는 앱을 완전히 종료하지는 마세요.\n녹음이 저장되지 않을 수 있습니다.",
                color = DarkGray600,
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 28.dp)
            )
        }
    }

    @ExperimentalMaterialApi
    private suspend fun startRecording() {
        val permissionResult =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                TedPermission.create()
                    .setPermissions(
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.FOREGROUND_SERVICE
                    )
                    .check()
            } else {
                TedPermission.create()
                    .setPermissions(
                        Manifest.permission.RECORD_AUDIO
                    )
                    .check()
            }
        if (permissionResult.isGranted) {
            drawerState.close()
            ForegroundServiceUtils.runForegroundService(
                applicationContext,
                ForegroundServiceUtils.ForegroundServiceType.SOUND_RECORD_SERVICE
            )
            goHome()
        } else {
            Toast.makeText(this@MainActivity, "권한을 모두 허용해주세요", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun goHome() = Intent(Intent.ACTION_MAIN).run {
        addCategory(Intent.CATEGORY_HOME)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(this)
    }
}
