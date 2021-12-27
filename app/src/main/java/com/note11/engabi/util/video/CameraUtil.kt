package com.note11.engabi.util.video

import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import com.note11.engabi.model.native.CameraModel
import com.note11.engabi.model.native.Lens

object CameraUtil {
    fun getVideoCameras(cameraManager: CameraManager): List<CameraModel> {
        val availableCameras: MutableList<CameraModel> = mutableListOf()

        for (id in cameraManager.cameraIdList) {
            val characteristics = cameraManager.getCameraCharacteristics(id)
            val lens = when (characteristics.get(CameraCharacteristics.LENS_FACING)!!) {
                CameraCharacteristics.LENS_FACING_BACK -> Lens.BACK
                CameraCharacteristics.LENS_FACING_FRONT -> Lens.FRONT
                else -> Lens.UNKNOWN
            }

            val capabilities = characteristics.get(
                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES
            )!!
            val cameraConfig = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP
            )!!

            if (capabilities.contains(
                    CameraCharacteristics
                        .REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE
                )
            ) {
                val targetClass = MediaRecorder::class.java

                cameraConfig.getOutputSizes(targetClass).forEach { size ->
                    val secondsPerFrame =
                        cameraConfig.getOutputMinFrameDuration(targetClass, size) /
                                1_000_000_000.0
                    val fps = if (secondsPerFrame > 0) (1.0 / secondsPerFrame).toInt() else 0

                    if (fps >= 30 && size.width / 16 * 9 == size.height)
                        availableCameras.add(CameraModel(id, size, fps, lens))
                }
            }
        }

        return availableCameras
    }
}