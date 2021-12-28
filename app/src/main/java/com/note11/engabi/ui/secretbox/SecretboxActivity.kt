package com.note11.engabi.ui.secretbox

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.note11.engabi.R
import com.note11.engabi.model.UserModel
import com.note11.engabi.ui.theme.EngabiTheme
import com.note11.engabi.ui.theme.White
import kotlinx.coroutines.launch
import java.util.*

class SecretboxActivity : ComponentActivity() {

    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngabiTheme {
                Scaffold(
                    backgroundColor = Color(0xFFF3F3F3),
                    floatingActionButton = { UploadFab() }
                ) {
                    Column {
                        val pagerState: PagerState = rememberPagerState(initialPage = 0)
                        val coroutineScope = rememberCoroutineScope()
                        val tabList = listOf("녹음", "사진 ∙ 동영상")

                        Column(modifier = Modifier.background(Color(0xFFFCFCFC))) {
                            BackToHome {
                                this@SecretboxActivity.finish()
                            }
                            Text(
                                "비밀창고",
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    top = 16.dp,
                                    bottom = 8.dp
                                ),
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                            )
                            TabRow(selectedTabIndex = pagerState.currentPage,
                                backgroundColor = Color(0xFFFCFCFC),
                                indicator = { tabPositions: List<TabPosition> ->
                                    Box(
                                        modifier = Modifier
                                            .tabIndicatorOffset(tabPositions[pagerState.currentPage])
                                            .height(3.dp)
                                            .background(Color(0xFF7E9EDE))
                                    )
                                }) {
                                tabList.forEachIndexed { index, text ->
                                    Tab(selected = pagerState.currentPage == index,
                                        onClick = {
                                            coroutineScope.launch {
                                                pagerState.animateScrollToPage(index)
                                            }
                                        },
                                        text = {
                                            Text(
                                                text = text,
                                                letterSpacing = 0.sp,
                                                fontWeight = FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = Color(if (pagerState.currentPage == index) 0xFF7E9EDE else 0xFFC4C4C4)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                        HorizontalPager(
                            state = pagerState,
                            count = tabList.size,
                        ) { page: Int ->
                            when (page) {
                                0 -> VoiceRecPage()
                                1 -> VideoRecPage()
                            }
                        }
                    }
                }


            }
        }
    }


    @Composable
    fun BackToHome(onTap: () -> Unit) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, top = 20.dp)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onTap() }
                .padding(0.dp, 4.dp, 4.dp, 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Filled.ArrowBackIos,
                contentDescription = "홈으로 돌아가기",
                tint = Color(0xFF7E9EDE)
            )
            Text(
                "홈",
                color = Color(0xFF7E9EDE),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
            )
        }
    }

    @Composable
    fun UploadFab() {
        FloatingActionButton(onClick = {
        }, backgroundColor = Color(0xFF7E9EDE)) {
            Icon(
                Icons.Filled.Forward,
                contentDescription = "",
                modifier = Modifier
                    .size(32.dp)
                    .rotate(270f),
                tint = White,
            )
        }
    }

    @Composable
    fun VoiceRecPage() {
        val context = LocalContext.current
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

        }
    }

    @Composable
    fun VideoRecPage() {
        val context = LocalContext.current
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

        }
    }

//    @Composable
//    fun PostCard(post: Post, onTap: (Post) -> Unit) {
//        Column(
//            modifier = Modifier
//                .clip(RoundedCornerShape(8.dp))
//                .background(Color.White)
//                .clickable { onTap(post) }
//                .padding(18.dp, 18.dp, 18.dp, 8.dp)
//        ) {
//
//        }
//    }

    object TimeUtil {
        private const val MAX_SEC = 60
        private const val MAX_MIN = 60
        private const val MAX_HOUR = 24
        private const val MAX_DAY = 30
        private const val MAX_MONTH = 12

        fun timeToKorFormat(comparedTime: Long): String {
            val nowTime = System.currentTimeMillis()
            var diffTime: Long = (nowTime - comparedTime) / 1000
            return when {
                diffTime < MAX_SEC -> "방금 전"
                MAX_SEC.let { diffTime /= it; diffTime } < MAX_MIN -> "${diffTime}분 전"
                MAX_MIN.let { diffTime /= it; diffTime } < MAX_HOUR -> "${diffTime}시간 전"
                MAX_HOUR.let { diffTime /= it; diffTime } < MAX_DAY -> "${diffTime}일 전"
                MAX_DAY.let { diffTime /= it; diffTime } < MAX_MONTH -> "${diffTime}달 전"
                else -> "${diffTime}년 전"
            }
        }
    }

}