package com.note11.engabi.model.native

import android.util.Size

data class CameraModel(
    val cameraId: String,
    val size: Size,
    val fps: Int,
    val lensFacing: Lens,
    val fov: Float?
) {
    override fun toString(): String =
        "$lensFacing ($cameraId), ${size.width}x${size.height}, ${fps}fps, fov: $fov"

    private fun getMaxSize(): Size {
        for (it in listOf(1080, 720)) {
            if (size.height >= it) return Size(it / 9 * 16, it)
        }
        return Size(720, 480)
    }

    private fun getMaxFps() : Int = if(fps > 30) 30 else fps

    fun getMaxSizeModel(): CameraModel = CameraModel(cameraId, getMaxSize(), getMaxFps(), lensFacing, fov)
}

enum class Lens {
    BACK {
        override fun toString(): String = "후면 카메라"
    },
    FRONT {
        override fun toString(): String = "전면 카메라"
    },
    UNKNOWN {
        override fun toString(): String = "알 수 없음"
    },
}