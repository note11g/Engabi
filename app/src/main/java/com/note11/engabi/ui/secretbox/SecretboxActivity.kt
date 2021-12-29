package com.note11.engabi.ui.secretbox

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.GridCells
import androidx.compose.foundation.lazy.LazyVerticalGrid
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
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
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import com.note11.engabi.BuildConfig
import com.note11.engabi.R
import com.note11.engabi.model.UserModel
import com.note11.engabi.ui.theme.*
import kotlinx.coroutines.launch
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.*

class SecretboxActivity : ComponentActivity() {

    private val viewModel by viewModels<SecretboxViewModel>()
    private val deleteMode by lazy { mutableStateOf(false) }

    @OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.loadFiles()

        setContent {
            EngabiTheme {
                Scaffold(
                    backgroundColor = Blue900,
                    floatingActionButton = { UploadFab() },
                    topBar = {
                        TopAppBar(
                            backgroundColor = Color.Transparent,
                            elevation = 0.dp,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        modifier = Modifier
                                            .height(28.dp)
                                            .clickable { this@SecretboxActivity.finish() }
                                            .padding(4.dp),
                                        imageVector = Icons.Filled.ArrowBackIos,
                                        contentDescription = "홈으로 돌아가기",
                                        tint = White
                                    )
                                    Spacer(modifier = Modifier.size(16.dp))
                                    Text(
                                        "비밀 창고",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        color = White
                                    )
                                }



                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (deleteMode.value) Icon(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .padding(end = 8.dp)
                                            .clickable {
                                                viewModel.deleteSelectedFiles()
                                                viewModel.selectFiles.clear()
                                                deleteMode.value = false
                                            }
                                            .padding(4.dp),
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "삭제",
                                        tint = White,
                                    )

                                    Icon(
                                        modifier = Modifier
                                            .height(36.dp)
                                            .clickable {
                                                deleteMode.value = !deleteMode.value
                                                if (deleteMode.value) {
                                                    Toast
                                                        .makeText(
                                                            applicationContext,
                                                            "삭제할 파일을 선택해주세요",
                                                            Toast.LENGTH_SHORT
                                                        )
                                                        .show()


                                                } else {
                                                    viewModel.selectFiles.clear()
                                                }
                                            }
                                            .padding(4.dp),
                                        imageVector = if (!deleteMode.value) Icons.Outlined.Delete else Icons.Filled.Close,
                                        contentDescription = "삭제",
                                        tint = White,
                                    )
                                }
                            }
                        }
                    }
                ) {

                    LazyVerticalGrid(
                        cells = GridCells.Adaptive(160.dp),
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(viewModel.fileList.value.size) { i ->
                            viewModel.fileList.value[i].let { file ->
                                Column(
                                    modifier = (if (!deleteMode.value) Modifier
                                        .clickable {
                                            startActivity(Intent().apply {
                                                action = Intent.ACTION_VIEW
                                                type = MimeTypeMap
                                                    .getSingleton()
                                                    .getMimeTypeFromExtension(file.extension)
                                                val authority =
                                                    "${BuildConfig.APPLICATION_ID}.provider"
                                                data = FileProvider.getUriForFile(
                                                    applicationContext,
                                                    authority,
                                                    file
                                                )
                                                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                                        Intent.FLAG_ACTIVITY_CLEAR_TOP
                                            })
                                        }
                                        .background(Blue800) else Modifier
                                        .selectable(
                                            selected = viewModel.selectFiles.indexOf(file) != -1
                                        ) {
                                            if (viewModel.selectFiles.indexOf(file) != -1) {
                                                viewModel.selectFiles.remove(file)
                                            } else {
                                                viewModel.selectFiles.add(file)
                                            }
                                        }
                                        .background(if (viewModel.selectFiles.indexOf(file) == -1) Blue800 else Blue400))
                                        .fillMaxSize()
                                        .padding(
                                            top = 24.dp,
                                            start = 16.dp,
                                            bottom = 16.dp,
                                            end = 16.dp
                                        )
                                ) {
                                    Text(
                                        SimpleDateFormat("yy.MM.dd HH:mm:ss", Locale.KOREAN).format(
                                            file.lastModified()
                                        ),
                                        color = White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))

                                    Text(
                                        "${file.extension.uppercase(Locale.ENGLISH)} | ${
                                            String.format(
                                                "%.2f",
                                                file.length().toFloat() / (1024f * 1024f)
                                            )
                                        }MB",
                                        color = Gray500,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Normal
                                    )

                                    Spacer(modifier = Modifier.size(12.dp))

                                    Text(
                                        file.nameWithoutExtension,
                                        color = White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }


                        }

                    }
                }
            }
        }
    }

    @Composable
    fun UploadFab() {
        FloatingActionButton(onClick = {
        }, backgroundColor = Blue400) {
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


}