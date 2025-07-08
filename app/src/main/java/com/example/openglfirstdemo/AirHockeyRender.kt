package com.example.openglfirstdemo

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLSurfaceView.Renderer
import com.example.openglfirstdemo.util.ShaderHelper
import com.example.openglfirstdemo.util.TextResReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/**
 * GLSurfaceView在后台线程中执⾏渲染
 */
class AirHockeyRender(val context: Context) : Renderer {

    //--------------------vertex shader Start--------------------
    private val A_POSITITON = "a_Position"
    private var aPositionLocation: Int = 0
    //--------------------vertex shader End--------------------

    //--------------------fragment shader Start--------------------
    private val U_COLOR = "u_Color"
    private var uColorLocation: Int = 0
    //-------------------fragment shader End--------------------


    private val BYTES_FLOAT = 4
    private var program: Int = 0

    //一个顶点有2个分量
    private val POSITION_COMPONENT_COUNT = 2
    //顶点属性数组
    //逆时针顺序排列顶点=卷曲顺序,可以优化性能
    private val tableVerticesWithTriangles: Array<Float> = arrayOf(
        //Triangle 1
        0f, 0f,
        9f, 14f,
        0f, 14f,

        //Triangle 2
        0f, 0f,
        9f, 0f,
        9f, 14f,

        //Line 1
        0f, 7f,
        9f, 7f,

        // ⽊槌
        4.5f, 2f,
        4.5f, 12f
    )

    //把tableVerticesWithTriangles从jvm复制到了本地内存
    //这块内存不会被GC,是为了给openGl读写的本地内存！
    //这一步是为了把数据从jvm->openGl,因为openGl直接运行在本地系统上:p
    //本地内存中的缓冲区！！！
    private val vertexData: FloatBuffer = ByteBuffer
        .allocateDirect(tableVerticesWithTriangles.size * BYTES_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
            put(tableVerticesWithTriangles.toFloatArray())
        }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
        val vertexShaderSource =
            TextResReader.readTextFromResource(context, R.raw.simple_vertex_shader)
        val fragmentShaderSource =
            TextResReader.readTextFromResource(context, R.raw.simple_fragment_shader)

        val vertexShader:Int= ShaderHelper.compileVertexShader(vertexShaderSource)
        val fragmentShader:Int = ShaderHelper.compileFragmentShader(fragmentShaderSource)

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader)
        ShaderHelper.validateProgram(program)
        //把程序装到openGl
        GLES20.glUseProgram(program)

        //一旦程序链接 就可以获取程序中的attribute和uniform的位置！
        //获得uniform的位置
        //⼀个uniform的位置在⼀个程序对象中是唯⼀的,稍后要更新uniform的值会用到
        uColorLocation = GLES20.glGetUniformLocation(program, U_COLOR)
        //获得attribute的位置
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITITON)
        //关联attribute和顶点数据数组
        //设置顶点数据缓冲区(vertex buffer)的读取位置回到起始位置(0)。
        vertexData.position(0)
        //把顶点数据数组绑定到attribute上
        GLES20.glVertexAttribPointer(aPositionLocation, POSITION_COMPONENT_COUNT, GLES20.GL_FLOAT, false, 0, vertexData)


    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        //清空屏幕 触发glClearColor
        GLES20.glClear(GL_COLOR_BUFFER_BIT)
    }
}