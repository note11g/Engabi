package com.note11.engabi.ui.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.media.MediaCodec
import android.media.MediaRecorder
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.Size
import android.view.MotionEvent
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
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

    private val camIdMap: Map<String, CameraModel> by lazy { //todo : change this => lens facing state
        val camMap = mutableMapOf<String, CameraModel>()

        for (cam in CameraUtil.getVideoCameras(cameraManager)) {
            if (camMap[cam.cameraId] == null) {
                camMap[cam.cameraId] = cam
            } else if (camMap[cam.cameraId]!!.size.width * camMap[cam.cameraId]!!.size.height < cam.size.width * cam.size.height) {
                camMap[cam.cameraId] = cam
            }
        }

        camMap
    }

    private val camIdState by lazy { MutableStateFlow("0") }

    private val initCameraModel: CameraModel by lazy {
        lateinit var normalCam: CameraModel

        for (cam in camIdMap) {
            normalCam = cam.value
            break
        }

        camIdState.value = normalCam.cameraId
        normalCam
    }

    private val recorder: MediaRecorder by lazy {
        createRecorder(
            initCameraModel,
            recorderSurface
        )
    } //todo 매번 생성해야할 듯

    private lateinit var camera: CameraDevice

    private val cameraThread = HandlerThread("CameraThread").apply { start() }
    private val cameraHandler = Handler(cameraThread.looper)

    private lateinit var session: CameraCaptureSession

    private val recorderSurface: Surface by lazy {
        val surface = MediaCodec.createPersistentInputSurface()
        createRecorder(initCameraModel, surface).apply {
            prepare()
            release()
        }
        surface
    }

    private lateinit var lateSurfaceView: AutoFitSurfaceView

    private fun previewRequest(surfaceView: SurfaceView): CaptureRequest {
        return session.device.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {
            addTarget(surfaceView.holder.surface)
        }.build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            EngabiTheme {
                Surface(color = Color.Black) {
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

                        IconButton(
                            onClick = { },
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
                            Icon(
                                painter = painterResource(id = R.drawable.ic_camera_change_wide),
                                contentDescription = "더 넓게 찍기",
                                tint = Color.White
                            )
                        }

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
                                                "녹화 기능은 버그로 인해 차후 지원 예정입니다. (cameraX의 호환성 문제로 Camera2로 마이그레이션 예정)",
//                                                "녹화를 시작합니다.\n종료하려면 화면을 빠르게 두번 터치하세요.",
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


    @Composable
    fun Camera2Compose(modifier: Modifier = Modifier) {
        AndroidView(factory = { context ->
            val surfaceView = AutoFitSurfaceView(context)
            lateSurfaceView = surfaceView

            surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
                override fun surfaceCreated(p0: SurfaceHolder) {
                    surfaceView.run {
                        setAspectRatio(initCameraModel.size.width, initCameraModel.size.height)
                        post { initializeCamera(this) }
                    }
                }

                override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) = Unit
                override fun surfaceDestroyed(p0: SurfaceHolder) = Unit
            })

            surfaceView
        }, modifier = modifier.fillMaxSize())
    }

    private fun createRecorder(camera: CameraModel, surface: Surface) = MediaRecorder().apply {
        setAudioSource(MediaRecorder.AudioSource.MIC)
        setVideoSource(MediaRecorder.VideoSource.SURFACE)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setOutputFile(outputFile.absolutePath)
        setVideoEncodingBitRate(RECORDER_VIDEO_BITRATE)
        if (camera.fps > 0) setVideoFrameRate(camera.fps)
        setVideoSize(camera.size.width, camera.size.height)
        setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        setInputSurface(surface)
    }

    private fun initializeCamera(surfaceView: SurfaceView) =
        lifecycleScope.launch(Dispatchers.Main) {
            camera = openCamera(cameraManager, initCameraModel.cameraId, cameraHandler)
            val targets = listOf(surfaceView.holder.surface, recorderSurface)
            session = createCaptureSession(camera, targets, cameraHandler)
            session.setRepeatingRequest(previewRequest(surfaceView), null, cameraHandler)

            // React to user touching the capture button
//        fragmentCameraBinding.captureButton.setOnTouchListener { view, event ->
//            when (event.action) {
//
//                MotionEvent.ACTION_DOWN -> lifecycleScope.launch(Dispatchers.IO) {
//
//                    // Prevents screen rotation during the video recording
//                    requireActivity().requestedOrientation =
//                        ActivityInfo.SCREEN_ORIENTATION_LOCKED
//
//                    // Start recording repeating requests, which will stop the ongoing preview
//                    //  repeating requests without having to explicitly call `session.stopRepeating`
//                    session.setRepeatingRequest(recordRequest, null, cameraHandler)
//
//                    // Finalizes recorder setup and starts recording
//                    recorder.apply {
//                        // Sets output orientation based on current sensor value at start time
//                        relativeOrientation.value?.let { setOrientationHint(it) }
//                        prepare()
//                        start()
//                    }
//                    recordingStartMillis = System.currentTimeMillis()
//                    Log.d(TAG, "Recording started")
//
//                    // Starts recording animation
//                    fragmentCameraBinding.overlay.post(animationTask)
//                }
//
//                MotionEvent.ACTION_UP -> lifecycleScope.launch(Dispatchers.IO) {
//
//                    // Unlocks screen rotation after recording finished
//                    requireActivity().requestedOrientation =
//                        ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
//
//                    // Requires recording of at least MIN_REQUIRED_RECORDING_TIME_MILLIS
//                    val elapsedTimeMillis = System.currentTimeMillis() - recordingStartMillis
//                    if (elapsedTimeMillis < MIN_REQUIRED_RECORDING_TIME_MILLIS) {
//                        delay(MIN_REQUIRED_RECORDING_TIME_MILLIS - elapsedTimeMillis)
//                    }
//
//                    Log.d(TAG, "Recording stopped. Output file: $outputFile")
//                    recorder.stop()
//
//                    // Removes recording animation
//                    fragmentCameraBinding.overlay.removeCallbacks(animationTask)
//
//                    // Broadcasts the media file to the rest of the system
//                    MediaScannerConnection.scanFile(
//                        view.context, arrayOf(outputFile.absolutePath), null, null)
//
//                    // Launch external activity via intent to play video recorded using our provider
//                    startActivity(Intent().apply {
//                        action = Intent.ACTION_VIEW
//                        type = MimeTypeMap.getSingleton()
//                            .getMimeTypeFromExtension(outputFile.extension)
//                        val authority = "${BuildConfig.APPLICATION_ID}.provider"
//                        data = FileProvider.getUriForFile(view.context, authority, outputFile)
//                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
//                                Intent.FLAG_ACTIVITY_CLEAR_TOP
//                    })
//
//                    // Finishes our current camera screen
//                    delay(CameraActivity.ANIMATION_SLOW_MILLIS)
//                    navController.popBackStack()
//                }
//            }

//            true
//        }
        }

    private suspend fun reopenCamera() {
        camera.close()

        camera = openCamera(cameraManager, camIdState.value, cameraHandler)
        val targets = listOf(lateSurfaceView.holder.surface, recorderSurface)
        session = createCaptureSession(camera, targets, cameraHandler)
        session.setRepeatingRequest(previewRequest(lateSurfaceView), null, cameraHandler)
    }

    private fun getInitialLens(): Lens {
        val list = CameraUtil.getVideoCameras(cameraManager)
        list.forEach {
            if (it.lensFacing == Lens.BACK) return Lens.BACK
            else if (it.lensFacing == Lens.FRONT) return Lens.FRONT
        }
        return Lens.BACK
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
        }


        lifecycleScope.launch { reopenCamera() }
    }

    private suspend fun createCaptureSession(
        device: CameraDevice,
        targets: List<Surface>,
        handler: Handler? = null
    ): CameraCaptureSession = suspendCoroutine { cont ->

        device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                Log.e(TAG, exc.message, exc)
                cont.resumeWithException(exc)
            }
        }, handler)
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

    companion object {
        private const val RECORDER_VIDEO_BITRATE: Int = 10_000_000

        private val TAG = VideoActivity::class.java.simpleName

        private fun createFile(context: Context, extension: String): File {
            val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
            return File(context.filesDir, "VID_${sdf.format(Date())}.$extension")
        }
    }
}
