package com.note11.engabi.ui.main

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.gun0912.tedpermission.coroutine.TedPermission
import com.note11.engabi.R
import com.note11.engabi.ui.community.CommunityMainActivity
import com.note11.engabi.ui.login.LoginActivity
import com.note11.engabi.ui.more.ChangeActivity
import com.note11.engabi.ui.secretbox.SecretboxActivity
import com.note11.engabi.ui.theme.*
import com.note11.engabi.ui.util.CustomBottomDrawer
import com.note11.engabi.ui.util.CustomBottomSheetLayout
import com.note11.engabi.ui.util.CustomBottomSheetScaffold
import com.note11.engabi.ui.video.VideoActivity
import com.note11.engabi.util.AuthUtil
import com.note11.engabi.util.ForegroundServiceUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : FragmentActivity() {
    private val viewModel by viewModels<MainViewModel>()

    @ExperimentalMaterialApi
    private lateinit var sheetState: ModalBottomSheetState

    private val nowSheetState = mutableStateOf<Sheet?>(null)

    enum class Sheet { RECORD, MORE }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngabiTheme {
                sheetState =
                    rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)

                CustomBottomSheetLayout(
                    modifier = Modifier.fillMaxSize(),
                    bottomSheetState = sheetState,
                    sheetContent = {
                        BottomSheet(nowSheetState.value)
                    }
                ) {
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

    @ExperimentalMaterialApi
    @Composable
    private fun HeaderSection() {
        val coroutineScope = rememberCoroutineScope()
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_logo),
                contentDescription = "??????",
                modifier = Modifier.height(32.dp),
                tint = White
            )
            Icon(
                Icons.Filled.MoreVert,
                contentDescription = "?????????",
                modifier = Modifier
                    .clip(Shapes.medium)
                    .clickable {
                        nowSheetState.value = Sheet.MORE
                        coroutineScope.launch { sheetState.show() }
                    }
                    .size(40.dp)
                    .padding(6.dp)
                    .rotate(90.0f)
            )
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun ContentSection() {
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
                title = "??????",
                subTitle = "????????? ???????????? ????????????",
                painterId = R.drawable.ic_home_voice_rec,
                iconHeight = 113.dp
            ) {
                nowSheetState.value = Sheet.RECORD
                coroutineScope.launch { sheetState.show() }
            }

            GoBtnType1(
                modifier = Modifier.constrainAs(video) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(voice.end, margin = 4.dp)
                    end.linkTo(parent.end)

                    width = Dimension.fillToConstraints
                },
                title = "????????? ??????",
                subTitle = "????????? ???????????? ????????????",
                painterId = R.drawable.ic_home_video_rec,
                iconHeight = 81.dp
            ) {
                lifecycleScope.launch {
                    val permissionResult =
                        TedPermission.create()
                            .setPermissions(
                                Manifest.permission.CAMERA,
                                Manifest.permission.RECORD_AUDIO
                            )
                            .check()
                    if (permissionResult.isGranted) {
                        startActivity(
                            Intent(
                                this@MainActivity,
                                VideoActivity::class.java
                            )
                        )
                    } else {
                        Toast.makeText(this@MainActivity, "????????? ????????? ??????????????????", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }

        Spacer(Modifier.size(8.dp))

        GoBtnType1(
            modifier = Modifier.fillMaxWidth(),
            title = "?????? ??????",
            subTitle = "????????? ????????? ??? ??? ?????????",
            painterId = R.drawable.ic_secret_box,
            iconHeight = 117.dp,
            titleFontSize = 22.sp,
            subTitleFontSize = 13.sp
        ) {
            AuthUtil.auth(this, success = {
                startActivity(Intent(this, SecretboxActivity::class.java))
            })
        }

        Spacer(Modifier.size(8.dp))

        GoBtnType2(
            title = "????????????",
            subTitle = "????????? ???????????? ???????????? ????????? ??? ?????????",
            painterId = R.drawable.ic_report_problem,
            iconSize = 40.dp
        ) {
            Toast.makeText(applicationContext, "?????? ????????? ?????? ??????????????????.", Toast.LENGTH_LONG).show()
        }

        Spacer(Modifier.size(8.dp))

        ListBox("????????????", onClick = {
            startActivity(Intent(this@MainActivity, CommunityMainActivity::class.java))
            Toast.makeText(applicationContext, "???????????? ????????? ?????? ??????????????????.", Toast.LENGTH_LONG).show()
        }) {
            Column {
                val strList = listOf(
                    "??? ?????? ??? ????????? ?????? ?????? ??? ?????????",
                    "?????? 1????????? ????????? ????????? ???????????? ??? ???????????? ???????",
                    "??? ????????? ??? ??????...??????"
                )

                for (i in 1..3) {
                    ListBoxItem(
                        "????????? ?????? ${4 - i}",
                        strList[i - 1],
                        "${17 - i}?????? ???"
                    ) {
                        Toast.makeText(applicationContext, "???????????? ????????? ?????? ??????????????????.", Toast.LENGTH_LONG)
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
        titleFontSize: TextUnit = 20.sp,
        subTitleFontSize: TextUnit = 12.sp,
        painterId: Int,
        iconHeight: Dp,
        onClick: () -> Unit
    ) {
        ConstraintLayout(
            modifier = modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(Shapes.medium)
                .background(Blue800)
                .clickable { onClick() }
        ) {
            val (titles, img) = createRefs()

            Column(Modifier.constrainAs(titles) {
                top.linkTo(parent.top, margin = 18.dp)
                start.linkTo(parent.start, margin = 18.dp)
            }) {
                Text(subTitle, color = Gray300, fontSize = subTitleFontSize)
                Spacer(Modifier.size(2.dp))
                Text(title, color = White, fontSize = titleFontSize, fontWeight = FontWeight.Bold)
            }
            Image(
                painter = painterResource(id = painterId),
                contentDescription = "?????????",
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
        iconSize: Dp,
        onClick: () -> Unit
    ) {
        Row(
            Modifier
                .clip(Shapes.medium)
                .background(Blue800)
                .clickable { onClick() }
                .padding(vertical = 24.dp, horizontal = 18.dp)
                .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painterResource(id = painterId),
                "?????????",
                modifier = Modifier.size(iconSize),
                tint = RedAccent
            )
            Spacer(Modifier.size(16.dp))
            Column {
                Text(subTitle, fontSize = 13.sp, color = Gray300)
                Spacer(Modifier.size(2.dp))
                Text(title, fontSize = 24.sp, color = RedAccent, fontWeight = FontWeight.Bold)
            }
        }
    }

    @Composable
    private fun ListBox(
        name: String,
        onClick: () -> Unit,
        itemSection: @Composable ((onClick: () -> Unit) -> Unit)
    ) {
        Column(
            Modifier
                .clip(Shapes.medium)
                .background(Blue800)
        ) {
            Text(
                "$name >",
                fontSize = 20.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onClick() }
                    .padding(start = 18.dp, top = 24.dp, bottom = 10.dp)
                    .fillMaxWidth()
            )
            Surface(Modifier.fillMaxWidth()) {
                itemSection(onClick)
            }
            Spacer(Modifier.size(8.dp))
        }
    }

    @Composable
    private fun ListBoxItem(
        title: String,
        subTitle: String,
        infoText: String = "",
        onClick: () -> Unit
    ) {

        ConstraintLayout(Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 18.dp)) {
            val (t, st, i) = createRefs()

            Text(
                title,
                maxLines = 1,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Gray50,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(t) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(i.start, margin = 4.dp)

                    width = Dimension.fillToConstraints
                }
            )

            Text(
                infoText,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Gray500,
                modifier = Modifier.constrainAs(i) {
                    top.linkTo(t.top)
                    bottom.linkTo(t.bottom)
                    end.linkTo(parent.end)
                }
            )

            Text(
                subTitle,
                maxLines = 1,
                fontSize = 12.sp,
                color = Gray500,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.constrainAs(st) {
                    start.linkTo(parent.start)
                    top.linkTo(t.bottom, margin = 4.dp)
                    end.linkTo(i.start, margin = 4.dp)
                    bottom.linkTo(parent.bottom)

                    width = Dimension.fillToConstraints
                }
            )
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun BottomSheet(nowSheet: Sheet?) {
        when (nowSheet) {
            Sheet.RECORD -> VoiceRecordBottomSheet()
            Sheet.MORE -> MoreBottomSheet()
            else -> Spacer(modifier = Modifier.height(1.dp))
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun VoiceRecordBottomSheet() {
        val coroutineScope = rememberCoroutineScope()
        BackHandler(enabled = sheetState.isVisible) {
            coroutineScope.launch {
                sheetState.hide()
            }
        }

        Column(Modifier.padding(horizontal = 24.dp, vertical = 32.dp)) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("????????????", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
                Icon(Icons.Filled.Clear,
                    contentDescription = "delete",
                    tint = Gray300,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            coroutineScope.launch { sheetState.hide() }
                        }
                        .padding(4.dp))
            }

            Text(
                "?????? ??????????????? ????????? ????????? ????????????, ????????? ???????????????.\n" +
                        "????????? ??????????????????, ?????? ?????? ????????? ????????? ??? 2?????? ???????????????.",
                fontWeight = FontWeight.Medium,
                color = Gray300,
                fontSize = 13.sp,
                modifier = Modifier
                    .padding(vertical = 32.dp)
                    .fillMaxWidth()
            )

            Text("?????? ????????????",
                fontWeight = FontWeight.Medium,
                color = White,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .clip(Shapes.medium)
                    .background(
                        RedAccent
                    )
                    .fillMaxWidth()
                    .clickable {
                        coroutineScope.launch {
                            startRecording()
                        }
                    }
                    .padding(vertical = 16.dp))
            Row() {
                Text(
                    text = "???",
                    color = Gray500,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 28.dp, end = 4.dp)
                )
                Text(
                    text = "????????? ????????? ?????? ?????? ????????? ??????????????? ?????????.\n" +
                            "????????? ???????????? ?????? ??? ????????????.",
                    color = Gray500,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 28.dp)
                )
            }
        }
    }

    @ExperimentalMaterialApi
    @Composable
    private fun MoreBottomSheet() {
        val coroutineScope = rememberCoroutineScope()
        BackHandler(enabled = sheetState.isVisible) {
            coroutineScope.launch { sheetState.hide() }
        }

        Column(Modifier.fillMaxWidth()) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 24.dp, bottom = 16.dp)
            ) {
                Text("?????????", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = White)
                Icon(Icons.Filled.Clear,
                    contentDescription = "delete",
                    tint = Gray300,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { coroutineScope.launch { sheetState.hide() } }
                        .padding(4.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        startActivity(Intent(this@MainActivity, ChangeActivity::class.java))
                    }
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Icon(Icons.Filled.PhonelinkLock, contentDescription = "??? ?????? ??????", tint = Blue200, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.size(24.dp))
                Text("??? ?????? ??????", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Gray50)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable { }
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Icon(Icons.Filled.NotificationImportant, contentDescription = "??? ?????? ??????", tint = Blue200, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.size(24.dp))
                Text("??? ?????? ??????", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Gray50)
            }
            Spacer(modifier = Modifier.size(16.dp))
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
            sheetState.hide()
            ForegroundServiceUtils.runForegroundService(
                applicationContext,
                ForegroundServiceUtils.ForegroundServiceType.SOUND_RECORD_SERVICE
            )
            goHome()
        } else {
            Toast.makeText(this@MainActivity, "????????? ?????? ??????????????????", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun goHome() = Intent(Intent.ACTION_MAIN).run {
        addCategory(Intent.CATEGORY_HOME)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(this)
    }
}
