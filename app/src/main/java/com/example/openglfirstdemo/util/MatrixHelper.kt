package com.example.openglfirstdemo.util

import kotlin.math.tan

object MatrixHelper {
    fun perspectiveM(
        m: FloatArray,
        yFovInDegrees: Float,
        aspect: Float,
        zNear: Float,
        zFar: Float
    ) {
        //弧度
        val angleInRadians: Float = (yFovInDegrees * Math.PI / 180f).toFloat()
        val a: Float = (1.0 / tan(angleInRadians / 2.0)).toFloat()

        //按列写数据
        m[0] = a / aspect
        m[1] = 0f
        m[2] = 0f
        m[3] = 0f

        m[4] = 0f
        m[5] = a
        m[6] = 0f
        m[7] = 0f

        m[8] = 0f
        m[9] = 0f
        m[10] = -((zFar + zNear) / (zFar - zNear))
        m[11] = -1f
        m[12] = 0f
        m[13] = 0f
        m[14] = -((2.0f * zFar * zNear) / (zFar - zNear))
        m[15] = 0f
    }
}