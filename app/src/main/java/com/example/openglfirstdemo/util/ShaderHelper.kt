package com.example.openglfirstdemo.util

import android.opengl.GLES20
import android.opengl.GLES20.GL_FRAGMENT_SHADER
import android.opengl.GLES20.GL_VERTEX_SHADER
import android.util.Log

object ShaderHelper {
    private const val TAG = "ShaderHelper"

    fun compileVertexShader(shaderCode: String): Int {
        return compileShader(GL_VERTEX_SHADER, shaderCode)

    }

    fun compileFragmentShader(shaderCode: String): Int {
        return compileShader(GL_FRAGMENT_SHADER, shaderCode)
    }

    /**
     * 创建shader
     * @param type shader的类型，GL_VERTEX_SHADER或者GL_FRAGMENT_SHADER
     * @param shaderCode shader的源码
     *
     * @return shader的id
     */
    fun compileShader(type: Int, shaderCode: String): Int {
        //整个id是openGl对象的引用，无论什么时候想要引用这个对象，都必须使用这个id
        val shaderObjectId = GLES20.glCreateShader(type)
        if (shaderObjectId == 0){
            //0代表创建失败 类似java中返回null
            Log.d(TAG, "compileShader: Could not create new shader")
        }
        //将shader的源码添加到shader对象上
        //源码绑定shader对象
        GLES20.glShaderSource(shaderObjectId, shaderCode)

        //编译shader上的源码
        GLES20.glCompileShader(shaderObjectId)
        //获取编译状态
        val compileStatus = IntArray(1)
        //将编译状态保存到compileStatus数组中
        GLES20.glGetShaderiv(shaderObjectId, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        Log.d(TAG, "compileShader: Result of compiling source:\n${GLES20.glGetShaderInfoLog(shaderObjectId)}")

        if (compileStatus[0] == 0){
            //编译失败 删除编译失败的shader
            GLES20.glDeleteShader(shaderObjectId)
            Log.d(TAG, "compileShader: Compilation of shader failed.")
            return 0
        }


        return shaderObjectId
    }





}