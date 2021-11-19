package com.note11.engabi.ui.video

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.ContextCompat
import com.note11.engabi.R
import com.note11.engabi.ui.theme.EngabiTheme
import com.note11.engabi.ui.theme.spoqaFamily

class VideoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EngabiTheme {
                Surface(color = Color.Black) {
                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (cam, nav, backBtn, guideText) = createRefs()
                        val camState = remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }

                        CameraXCompose(Modifier.constrainAs(cam) {
                            top.linkTo(parent.top)
                            bottom.linkTo(nav.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)

                            width = Dimension.fillToConstraints
                            height = Dimension.fillToConstraints
                        }, camState.value)

                        Row(
                            Modifier
                                .constrainAs(nav) {
                                    top.linkTo(cam.bottom)
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start, margin = 24.dp)
                                    end.linkTo(parent.end, margin = 24.dp)

                                    width = Dimension.fillToConstraints
                                    height = Dimension.value(96.dp)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.Start)
                            ) {
                                Text(
                                    "남은 녹화시간",
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp
                                )
                                Text(
                                    "14시간 32분", // todo : change time dynamically
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        Toast
                                            .makeText(
                                                this@VideoActivity,
                                                "녹화를 시작합니다.\n종료하려면 화면을 빠르게 두번 터치하세요.",
                                                Toast.LENGTH_LONG
                                            )
                                            .show()
                                    }
                                    .size(60.dp),
                                color = (Color(0xFFB44B46)),
                                border = BorderStroke(2.dp, color = Color.White)
                            ) {}

                            Box(
                                Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.End)
                                    .clickable {
                                        camState.value = when (camState.value) {
                                            CameraSelector.LENS_FACING_BACK -> CameraSelector.LENS_FACING_FRONT
                                            CameraSelector.LENS_FACING_FRONT -> CameraSelector.LENS_FACING_BACK
                                            else -> CameraSelector.LENS_FACING_BACK
                                        }
                                        Toast
                                            .makeText(
                                                this@VideoActivity,
                                                "${camState.value}",
                                                Toast.LENGTH_SHORT
                                            )
                                            .show()
                                    }) {
                                Icon(
                                    Icons.Filled.Cached,
                                    "카메라 전환",
                                    modifier = Modifier
                                        .size(32.dp),
                                    tint = Color.White
                                )
                            }

                        }

                        Icon(
                            painterResource(R.drawable.ic_back),
                            "",
                            tint = Color.White,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable { this@VideoActivity.finish() }
                                .size(20.dp)
                                .constrainAs(backBtn) {
                                    top.linkTo(parent.top, margin = 24.dp)
                                    start.linkTo(parent.start, margin = 24.dp)
                                })
                        Text(
                            "녹화 버튼을 누르면 화면이 꺼진 것 처럼 보입니다.\n" +
                                    "녹화를 종료하려면 검은 화면에서 빠르게 두번 터치하세요\n" +
                                    "\n" +
                                    "⚠ 실제로 꺼진 것은 아니므로 전원버튼을 누르지 말아주세요",
                            style = TextStyle(
                                fontFamily = spoqaFamily,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                fontSize = 15.sp,
                                color = Color.White,
                                shadow = Shadow(
                                    color = Color.Black, blurRadius = 6f,
                                    offset = Offset(4f, 4f)
                                )
                            ),
                            modifier = Modifier
                                .constrainAs(guideText) {
                                    top.linkTo(cam.top)
                                    bottom.linkTo(cam.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)
                                }
                        )
                    }
                }

            }
        }
    }
}

@Composable
fun CameraXCompose(modifier: Modifier = Modifier, camState: Int) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    // todo : migrate for camera2 api
    Box(modifier) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val executor = ContextCompat.getMainExecutor(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(camState)
                        .build()

                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                }, executor)

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}