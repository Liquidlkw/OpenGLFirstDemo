package com.example.openglfirstdemo

import android.app.ActivityManager
import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLSurfaceView
import android.opengl.GLSurfaceView.Renderer
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.openglfirstdemo.util.TextResReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : AppCompatActivity() {
    private lateinit var gLView: GLSurfaceView
    private var renderSet = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        //挖洞
        gLView = GLSurfaceView(this)
        setContentView(gLView)

        if (isSupportEs2()) {
            gLView.setEGLContextClientVersion(2)
            gLView.setRenderer(MyRender(this))
            renderSet = true
        }

    }

    override fun onPause() {
        super.onPause()
        if (renderSet) {
            gLView.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        if (renderSet) {
            gLView.onResume()
        }
    }

    /**
     * 设备是否支持es2.0
     */
    private fun isSupportEs2(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val confInfo = activityManager.deviceConfigurationInfo
        var supportEs2 = confInfo.reqGlEsVersion >= 0x20000
        Toast.makeText(this, "support Es2: $supportEs2", Toast.LENGTH_SHORT).show()
        return supportEs2
    }

    /**
     * GLSurfaceView在后台线程中执⾏渲染
     */
    class MyRender(val context: Context) : Renderer {
        private val BYTES_FLOAT = 4


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
        val vertexData: FloatBuffer = ByteBuffer
            .allocateDirect(tableVerticesWithTriangles.size * BYTES_FLOAT)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer().apply {
                //!!
                put(tableVerticesWithTriangles.toFloatArray())
            }


        override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
            GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f)
            val vertexShaderSource =
                TextResReader.readTextFromResource(context, R.raw.simple_vertex_shader)
            val fragmentShaderSource =
                TextResReader.readTextFromResource(context, R.raw.simple_fragment_shader)
        }

        override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
            GLES20.glViewport(0, 0, width, height)
        }

        override fun onDrawFrame(gl: GL10?) {
            //清空屏幕 触发glClearColor
            GLES20.glClear(GL_COLOR_BUFFER_BIT)
        }
    }

}