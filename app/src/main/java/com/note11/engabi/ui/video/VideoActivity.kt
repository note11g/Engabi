package com.note11.engabi.ui.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.media.MediaCodec
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Range
import android.view.*
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.common.util.concurrent.HandlerExecutor
import com.note11.engabi.BuildConfig
import com.note11.engabi.R
import com.note11.engabi.model.native.CameraModel
import com.note11.engabi.model.native.Lens
import com.note11.engabi.ui.theme.EngabiTheme
import com.note11.engabi.ui.theme.spoqaFamily
import com.note11.engabi.util.video.AutoFitSurfaceView
import com.note11.engabi.util.video.CameraUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class VideoActivity : ComponentActivity() {
    private val cameraManager by lazy { getSystemService(Context.CAMERA_SERVICE) as CameraManager }

    private val outputFile by lazy { createFile(applicationContext, "mp4") }

    private val camIdMap: Map<String, CameraModel> by lazy {
        val camMap = mutableMapOf<String, CameraModel>()

        for (cam in CameraUtil.getVideoCameras(cameraManager)) {
            if (camMap[cam.cameraId] == null || camMap[cam.cameraId]!!.size.width * camMap[cam.cameraId]!!.size.height < cam.size.width * cam.size.height) {
                camMap[cam.cameraId] = cam.getMaxSizeModel()
            }
        }

        Log.d(TAG, camMap.toString())

        camMap
    }
    private val camIdState = MutableStateFlow("0")

    private lateinit var recorder: MediaRecorder
    private lateinit var recorderSurface: Surface

    private lateinit var camera: CameraDevice

    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    private lateinit var session: CameraCaptureSession

    private lateinit var recordRequest: CaptureRequest

    private lateinit var lateSurfaceView: AutoFitSurfaceView

    private val recordingState = MutableStateFlow(false)

    private fun previewRequest(surfaceView: SurfaceView): CaptureRequest {
        return session.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            addTarget(surfaceView.holder.surface)
        }.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        hideSystemBars()
        createRecorderSurface()

        setContent {
            EngabiTheme {
                Surface(color = Color.Black) {
                    val isRecord = recordingState.collectAsState()

                    ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                        val (cam, nav, backBtn, guideText, wideBtn) = createRefs()

                        Camera2Compose(Modifier.constrainAs(cam) {
                            top.linkTo(parent.top)
                            bottom.linkTo(nav.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)

                            width = Dimension.fillToConstraints
                            height = Dimension.fillToConstraints
                        })

                        val camIdFlowAsState = camIdState.collectAsState()

                        if (camIdMap[camIdFlowAsState.value]!!.lensFacing == Lens.BACK && camIdMap.size > 2)
                            IconButton(
                                onClick = { changeLensWideOrNormal() },
                                modifier = Modifier
                                    .constrainAs(wideBtn) {
                                        bottom.linkTo(nav.top, margin = 20.dp)
                                        end.linkTo(parent.end, margin = 20.dp)
                                    }
                                    .clip(
                                        CircleShape
                                    )
                                    .background(Color(0x47000000))
                                    .size(40.dp)
                            ) {
                                if (camIdMap[camIdFlowAsState.value]!!.fov == null || camIdMap[camIdFlowAsState.value]!!.fov!! < 1.5)
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_camera_change_wide),
                                        contentDescription = "더 넓게 찍기",
                                        tint = Color.White
                                    )
                                else Icon(
                                    painter = painterResource(id = R.drawable.ic_camera_change_normal),
                                    contentDescription = "일반 모드로 찍기",
                                    tint = Color.White
                                )
                            }

                        Row(
                            Modifier
                                .constrainAs(nav) {
                                    bottom.linkTo(parent.bottom)
                                    start.linkTo(parent.start)
                                    end.linkTo(parent.end)

                                    width = Dimension.fillToConstraints
                                    height = Dimension.value(96.dp)
                                }
                                .background(Color.Black)
                                .padding(horizontal = 24.dp),
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

                            if (isRecord.value.not())
                                Surface(
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .clickable { startRecording() }
                                        .size(60.dp),
                                    color = (Color(0xFFB44B46)),
                                    border = BorderStroke(2.dp, color = Color.White)
                                ) {}
                            else Surface(
                                shape = CircleShape,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable { stopRecording() }
                                    .size(60.dp),
                                color = (Color(0xFF4655B4)),
                                border = BorderStroke(2.dp, color = Color.White)
                            ) {}

                            Box(
                                Modifier
                                    .weight(1f)
                                    .wrapContentWidth(Alignment.End)
                                    .clickable { changeLensFacing() }) {
                                Icon(
                                    Icons.Filled.Cached,
                                    "카메라 전환",
                                    modifier = Modifier
                                        .padding(8.dp)
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
                                .padding(8.dp)
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
                                fontSize = 14.sp,
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

                    if (isRecord.value) Surface(
                        Modifier
                            .background(Color.Black)
                            .pointerInput(Unit) {
                                detectTapGestures(onDoubleTap = { stopRecording() })
                            }
                            .fillMaxSize()
                    ) {}
                }

            }
        }
    }

    @Composable
    fun Camera2Compose(modifier: Modifier = Modifier) {
        AndroidView(factory = { context ->
            val surfaceView = AutoFitSurfaceView(context)
            lateSurfaceView = surfaceView

            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(p0: SurfaceHolder) {
                    surfaceView.run {
                        // todo : 만약, 팅기면 여기서 state를 관리해주어야함(flowAsState)
                        setAspectRatio(
                            camIdMap[camIdState.value]!!.size.width,
                            camIdMap[camIdState.value]!!.size.height
                        )
                        post { initializeCamera() }
                    }
                }

                override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) = Unit
                override fun surfaceDestroyed(p0: SurfaceHolder) = Unit
            })

            surfaceView
        }, modifier = modifier.fillMaxSize())
    }

    private fun createRecorder(camera: CameraModel, surface: Surface) =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(applicationContext)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(outputFile.absolutePath)
            setAudioSamplingRate(44100)
            setAudioEncodingBitRate(96000)
            setVideoEncodingBitRate(6_000_000)
            if (camera.fps > 0) setVideoFrameRate(camera.fps)
            setVideoSize(camera.size.width, camera.size.height)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setInputSurface(surface)
            setOrientationHint(if (camera.lensFacing == Lens.BACK) 90 else 270)
        }

    private fun createRecorderSurface() {
        recorderSurface = MediaCodec.createPersistentInputSurface()
        createRecorder(camIdMap[camIdState.value]!!, recorderSurface).apply {
            prepare()
            release()
        }
    }

    private fun startRecording() {
        recorder = createRecorder(
            camIdMap[camIdState.value]!!,
            recorderSurface
        )
        lifecycleScope.launch(Dispatchers.IO) {
            recordRequest = session.device.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
                .apply {
                    addTarget(lateSurfaceView.holder.surface)
                    addTarget(recorderSurface)
                    set(
                        CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE,
                        Range(camIdMap[camIdState.value]!!.fps, camIdMap[camIdState.value]!!.fps)
                    )
                }.build()

            session.setRepeatingRequest(recordRequest, null, cameraHandler)

            recorder.apply {
                prepare()
                start()
            }

            Log.d(TAG, "Recording started")

            recordingState.value = true
        }

        Toast.makeText(this, "녹화를 시작합니다.\n종료하려면 화면을 빠르게 두번 터치하세요.", Toast.LENGTH_LONG).show()
    }

    private fun stopRecording() = lifecycleScope.launch(Dispatchers.IO) {
        delay(500L)
        recorder.stop()

        recordingState.value = false

        MediaScannerConnection.scanFile(
            applicationContext, arrayOf(outputFile.absolutePath), null, null
        )

        startActivity(Intent().apply {
            action = Intent.ACTION_VIEW
            type = MimeTypeMap.getSingleton()
                .getMimeTypeFromExtension(outputFile.extension)
            val authority = "${BuildConfig.APPLICATION_ID}.provider"
            data = FileProvider.getUriForFile(applicationContext, authority, outputFile)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
        })
    }

    private fun initializeCamera() = lifecycleScope.launch(Dispatchers.Main) {
        camera = openCamera(cameraManager, camIdState.value, cameraHandler)
        val targets = listOf(lateSurfaceView.holder.surface, recorderSurface)
        session = createCaptureSession(camera, targets, cameraHandler)
        session.setRepeatingRequest(previewRequest(lateSurfaceView), null, cameraHandler)
    }

    override fun onStop() {
        super.onStop()
        try {
            camera.close()
        } catch (exc: Throwable) {
            Log.e(TAG, "Error closing camera", exc)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraThread.quitSafely()
        recorderSurface.release()
    }

    private suspend fun reopenCamera() {
        camera.close()
        session.close()
        recorderSurface.release()

        createRecorderSurface()

        camera = openCamera(cameraManager, camIdState.value, cameraHandler)
        val targets = listOf(lateSurfaceView.holder.surface, recorderSurface)
        session = createCaptureSession(camera, targets, cameraHandler)
        session.setRepeatingRequest(previewRequest(lateSurfaceView), null, cameraHandler)
    }

    private fun changeLensFacing() {
        camIdMap[camIdState.value]?.let { nowCam ->
            val facing = when (nowCam.lensFacing) {
                Lens.BACK -> Lens.FRONT
                Lens.FRONT -> Lens.BACK
                else -> Lens.BACK
            }

            for (cam in camIdMap) {
                if (cam.value.lensFacing == facing) {
                    camIdState.value = cam.key
                    break
                }
            }

            lifecycleScope.launch { reopenCamera() }
        }
    }

    private fun changeLensWideOrNormal() {
        camIdMap[camIdState.value]?.let { nowCam ->
            for (cam in camIdMap) {
                if (cam.value.lensFacing == nowCam.lensFacing && nowCam.cameraId != cam.key) {
                    camIdState.value = cam.key
                    break
                }
            }

            lifecycleScope.launch { reopenCamera() }
        }
    }

    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>,
        handler: Handler
    ): CameraCaptureSession = suspendCoroutine { cont ->

        val mCameraSessionListener = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                Log.e(TAG, exc.message, exc)
                cont.resumeWithException(exc)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val outputConfigurationList = mutableListOf<OutputConfiguration>()
            for (target in targets) outputConfigurationList.add(OutputConfiguration(target))

            val sessionConfiguration = SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                outputConfigurationList,
                HandlerExecutor(handler.looper),
                mCameraSessionListener
            )

            device.createCaptureSession(sessionConfiguration)
        } else {
            device.createCaptureSession(targets, mCameraSessionListener, handler)
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun openCamera(
        manager: CameraManager,
        cameraId: String,
        handler: Handler? = null
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                Log.w(TAG, "Camera $cameraId has been disconnected")
                this@VideoActivity.finish()
            }

            override fun onError(device: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                Log.e(TAG, exc.message, exc)
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }, handler)
    }

    private fun hideSystemBars() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsetsController =
                ViewCompat.getWindowInsetsController(window.decorView) ?: return
            windowInsetsController.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
    }

    companion object {
        private val TAG = VideoActivity::class.java.simpleName

        private fun createFile(context: Context, extension: String): File {
            val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
            return File(context.filesDir, "VID_${sdf.format(Date())}.$extension")
        }
    }
}
