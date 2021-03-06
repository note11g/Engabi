package com.note11.engabi.ui.community

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
import androidx.compose.material.icons.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import kotlinx.coroutines.launch
import java.util.*

class CommunityMainActivity : ComponentActivity() {

    @OptIn(ExperimentalPagerApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EngabiTheme {
                Scaffold(
                    backgroundColor = Color(0xFFF3F3F3),
                    floatingActionButton = { NewPostFab() }
                ) {
                    Column {
                        val pagerState: PagerState = rememberPagerState(initialPage = 0)
                        val coroutineScope = rememberCoroutineScope()
                        val tabList = listOf("????????????", "?????? ??? ???", "???????????? ???")

                        Column(modifier = Modifier.background(Color(0xFFFCFCFC))) {
                            BackToHome {
                                this@CommunityMainActivity.finish()
                            }
                            Text(
                                "????????????",
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
                                0 -> AllPostsPage()
                                1 -> MyPostsPage()
                                2 -> LikePostsPage()
                            }
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
            contentDescription = "????????? ????????????",
            tint = Color(0xFF7E9EDE)
        )
        Text(
            "???",
            color = Color(0xFF7E9EDE),
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
fun NewPostFab() {
    FloatingActionButton(onClick = {
    }, backgroundColor = Color(0xFF7E9EDE)) {
        Image(
            painter = painterResource(id = R.drawable.create_2x),
            contentDescription = "",
            modifier = Modifier.size(32.dp)
        )
    }
}

@Composable
fun AllPostsPage() {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(count = 10) { index ->
            PostCard(
                post = Post(
                    title = "??? ???????????? ??? ?????????",
                    content = "????????? ????????? ?????? ?????????",
                    photoUrl = "",
                    like = index,
                    isMyLike = (index + 1) % 3 == 0,
                    atPost = System.currentTimeMillis(),
                    user = UserModel(name = "????????? ??????", uid = "", birth = "", phone = ""),
                    comments = listOf()
                )
            ) {
                Toast.makeText(context, "???????????? ????????? ?????? ??????????????????.", Toast.LENGTH_LONG)
                    .show()
            }
        }
    }
}

@Composable
fun MyPostsPage() {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(count = 2) { index ->
            PostCard(
                post = Post(
                    title = "?????? ??? ?????????",
                    content = "?????? ????????? ?????? ?????????",
                    photoUrl = "",
                    like = index,
                    isMyLike = false,
                    atPost = System.currentTimeMillis(),
                    user = UserModel(name = "????????? ??????", uid = "", birth = "", phone = ""),
                    comments = listOf()
                )
            ) {
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun LikePostsPage() {
    val context = LocalContext.current
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp, 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(count = 5) { index ->
            PostCard(
                post = Post(
                    title = "????????? ?????????",
                    content = "?????? ????????? ?????? ?????????",
                    photoUrl = "",
                    like = 5 - index,
                    isMyLike = true,
                    atPost = System.currentTimeMillis(),
                    user = UserModel(name = "????????? ??????", uid = "", birth = "", phone = ""),
                    comments = listOf()
                )
            ) {
                Toast.makeText(context, "$it", Toast.LENGTH_SHORT).show()
            }
        }
    }
}

@Composable
fun PostCard(post: Post, onTap: (Post) -> Unit) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onTap(post) }
            .padding(18.dp, 18.dp, 18.dp, 8.dp)
    ) {
        Text(post.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(12.dp))
        Text(post.content)
        Spacer(Modifier.size(10.dp))
        Divider(color = Color(0xFFF3F3F3), thickness = 1.dp)
        Spacer(Modifier.size(2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row {
                Row(modifier = Modifier
                    .clip(
                        RoundedCornerShape(4.dp)
                    )
                    .clickable { }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (post.isMyLike) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "?????????",
                        tint = if (post.isMyLike) Color(0xFFE87373) else Color(0xFF949494),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "${post.like}", color = Color(0xFF949494))
                }
                Spacer(Modifier.size(6.dp))
                Row(modifier = Modifier
                    .clip(
                        RoundedCornerShape(4.dp)
                    )
                    .clickable { }
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Comment,
                        contentDescription = "????????????",
                        tint = Color(0xFF949494),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        "${post.comments.size}",
                        color = Color(0xFF949494),
                    )
                }
            }
            Text(
                TimeUtil.timeToKorFormat(post.atPost),
                color = Color(0xFF949494),
                fontSize = 12.sp
            )
        }
    }
}

data class Post(
    val title: String,
    val content: String,
    val photoUrl: String,
    val like: Int,
    var isMyLike: Boolean,
    val atPost: Long,
    val comments: List<Comment>,
    val user: UserModel
)

data class Comment(
    val content: String,
    val like: Int,
    var isMyLike: Boolean,
    val atPost: Long,
    val reComments: List<ReComment>,
    val user: UserModel
)

data class ReComment(
    val content: String,
    val like: Int,
    var isMyLike: Boolean,
    val atPost: Long,
    val user: UserModel
)

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
            diffTime < MAX_SEC -> "?????? ???"
            MAX_SEC.let { diffTime /= it; diffTime } < MAX_MIN -> "${diffTime}??? ???"
            MAX_MIN.let { diffTime /= it; diffTime } < MAX_HOUR -> "${diffTime}?????? ???"
            MAX_HOUR.let { diffTime /= it; diffTime } < MAX_DAY -> "${diffTime}??? ???"
            MAX_DAY.let { diffTime /= it; diffTime } < MAX_MONTH -> "${diffTime}??? ???"
            else -> "${diffTime}??? ???"
        }
    }
}