package com.example.openglfirstdemo

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLES20.GL_COLOR_BUFFER_BIT
import android.opengl.GLSurfaceView.Renderer
import android.opengl.Matrix
import com.example.openglfirstdemo.util.MatrixHelper
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
    //原来a代表attribute,u代表uniform啊
    //--------------------vertex shader Start--------------------

    private val U_MATRIX = "u_Matrix"
    private var uMatrixLocation: Int = 0
    //Model Matrix（模型矩阵）：控制单个物体的位置、旋转、缩放
    //View Matrix（视图矩阵）：控制摄像机的视角
    //Projection Matrix（投影矩阵）：控制3D到2D的投影
    //只要用标准 P * V * M * vertex（a_Position） 就可以了
    //正交投影矩阵
    private var projectionMatrix: FloatArray = FloatArray(16)
    //模型矩阵
    private var modelMatrix: FloatArray = FloatArray(16)
    //模型投影矩阵
    private var modelProjectionMatrix: FloatArray = FloatArray(16)


    private val A_POSITITON = "a_Position"
    private var aPositionLocation: Int = 0

    private val A_COLOR ="a_Color"
    private var aColorLocation: Int = 0
    //--------------------vertex shader End--------------------

    //--------------------fragment shader Start--------------------
    private val U_COLOR = "u_Color"
    private var uColorLocation: Int = 0
    //-------------------fragment shader End--------------------


    private val BYTES_FLOAT = 4
    private var program: Int = 0

    //每个顶点有5个分量 x,y,r,g,b
    //位置分量 x,y
    private val POSITION_COMPONENT_COUNT = 2
   //颜色分量 r,g,b
    private val COLOR_COMPONENT_COUNT = 3

    //步长:每个顶点的字节数 = 2个位置分量 + 3个颜色分量 用于给一个数组区分元组大小
    private val STRIDE = (POSITION_COMPONENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_FLOAT


    //顶点属性数组
    //逆时针顺序排列顶点=卷曲顺序,可以优化性能
    private val tableVerticesWithTriangles: FloatArray = floatArrayOf(
        //Order of coordinates: X, Y,Z,W,R,G,B
        //如果只写 (X, Y, R, G, B)，OpenGL会自动：
        //添加 Z = 0.0f
        //添加 W = 1.0f

        //Triangle Fan
        0.0f,  0.0f,1f,1f,1f,
        -0.5f, -0.8f,0.7f,0.7f,0.7f,
        0.5f, -0.8f,0.7f,0.7f,0.7f,
        0.5f,  0.8f,0.7f,0.7f,0.7f,
        -0.5f,  0.8f,0.7f,0.7f,0.7f,
        -0.5f, -0.8f,0.7f,0.7f,0.7f,

        //Line 1
        -0.5f, 0f,1f,0f,0f,
        0.5f, 0f,1f,0f,0f,

        // ⽊槌
        0f,-0.4f,0f,0f,1f,
        0f, 0.4f,1f,0f,0f
    )

    //把tableVerticesWithTriangles从jvm复制到了本地内存
    //这块内存不会被GC,是为了给openGl读写的本地内存！
    //这一步是为了把数据从jvm->openGl,因为openGl直接运行在本地系统上:p
    //本地内存中的缓冲区！！！
    private val vertexData: FloatBuffer = ByteBuffer
        .allocateDirect(tableVerticesWithTriangles.size * BYTES_FLOAT)
        .order(ByteOrder.nativeOrder())
        .asFloatBuffer().apply {
            put(tableVerticesWithTriangles)
        }


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        //设置清屏颜色
        GLES20.glClearColor(0f, 0f, 0f, 0f)
        val vertexShaderSource =
            TextResReader.readTextFromResource(context, R.raw.simple_vertex_shader)
        val fragmentShaderSource =
            TextResReader.readTextFromResource(context, R.raw.simple_fragment_shader)

        val vertexShader: Int = ShaderHelper.compileVertexShader(vertexShaderSource)
        val fragmentShader: Int = ShaderHelper.compileFragmentShader(fragmentShaderSource)

        program = ShaderHelper.linkProgram(vertexShader, fragmentShader)
        ShaderHelper.validateProgram(program)
        //把程序装到openGl
        GLES20.glUseProgram(program)

        //一旦程序链接 就可以获取程序中的attribute和uniform的位置！

        //获得程序中uniform u_Matrix 的位置
        uMatrixLocation = GLES20.glGetUniformLocation(program, U_MATRIX)

        //获得程序中attribute a_Position 的位置
        aPositionLocation = GLES20.glGetAttribLocation(program, A_POSITITON)
        //获得uniform的位置
        //⼀个uniform的位置在⼀个程序对象中是唯⼀的,稍后要更新uniform的值会用到
//        uColorLocation = GLES20.glGetUniformLocation(program, U_COLOR)

        //获得程序中 attribute  a_Color的位置
        aColorLocation = GLES20.glGetAttribLocation(program, A_COLOR)

        //关联attribute和顶点数据数组
        //设置顶点数据缓冲区(vertex buffer)的读取位置回到起始位置(0)。
        vertexData.position(0)
        //把顶点数据数组绑定到attribute上
        GLES20.glVertexAttribPointer(
            aPositionLocation,
            POSITION_COMPONENT_COUNT,
            GLES20.GL_FLOAT,
            false,
            STRIDE,
            vertexData
        )
        //启用attribute:告诉openGl可以从vertexData读取数据了
        GLES20.glEnableVertexAttribArray(aPositionLocation)


        //1.从哪里开始读
        //设置顶点数据缓冲区(vertex buffer)的读取位置回到起始位置(2)，第一个颜色的位置。
        vertexData.position(POSITION_COMPONENT_COUNT)

        //2.怎么读 (关联数据)
        //告诉OpenGL如何从你的缓冲区里读取每个顶点的属性数据，比如顶点坐标、颜色、法线等
        GLES20.glVertexAttribPointer(
            /*shader attribute location */aColorLocation,
            /*components count */COLOR_COMPONENT_COUNT,
            /*type */GLES20.GL_FLOAT,
            /*normalized */false,
            /*stride */STRIDE,
            /*buffer */vertexData
        )

        //3.开始读
        //启用attribute:告诉openGl可以从vertexData读取数据了
        GLES20.glEnableVertexAttribArray(aColorLocation)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
//        //设置2D正交投影，确保图形不会因为屏幕比例而变形
//        //横屏时：左右范围是 -aspectRatio 到 aspectRatio，上下范围是 -1 到 1
//        //竖屏时：左右范围是 -1 到 1，上下范围是 -aspectRatio 到 aspectRatio
//        //projectionMatrix 是一个 FloatArray(16)，会被填充成一个4x4的正交投影矩阵
//        //用于后续的3D到2D的投影变换
//        val aspectRatio = if(width>height) width.toFloat() / height else height.toFloat() / width
//        //左手坐标系
//        if(width>height){
//            //横屏
//            Matrix.orthoM(projectionMatrix, 0, -aspectRatio, aspectRatio, -1f, 1f,-1f, 1f)
//        }else{
//            //竖屏
//            Matrix.orthoM(projectionMatrix, 0, -1f, 1f, /*bottom*/-aspectRatio,/*top*/ aspectRatio,-1f, 1f)
//        }
//
//        // 计算最终的变换矩阵：投影矩阵 × 模型矩阵
//        Matrix.multiplyMM(modelProjectionMatrix, 0, projectionMatrix, 0, modelMatrix, 0)


        //这 会 ⽤ 4 5 度 的 视 野 创 建 ⼀ 个 透 视 投 影 。 这 个 视 椎 体 从 z 值 - 1 的 位 置 开 始 ， 在 z 值
        //为 - 1 0 的 位 置 结 束
        MatrixHelper.perspectiveM(projectionMatrix, 45f, width.toFloat() / height.toFloat(), 1f, 10f)

        Matrix.setIdentityM(modelMatrix, 0)
        //物体初始在 z=0（相机处）或 +Z（身后）→ 看不见/被近裁剪面裁掉。
        //没调用 Matrix.setLookAtM(...) 时，你的视图矩阵就是单位矩阵。等价于“相机在原点 (0,0,0)，朝 −Z”，
        Matrix.translateM(modelMatrix, 0, 0f, 0f, -4f)
        //绕X轴旋转-60度
        Matrix.rotateM(modelMatrix, 0, -60f, 1f, 0f,0f)
        // 计算最终的变换矩阵：投影矩阵 × 模型矩阵 顺序很重要！！！
        Matrix.multiplyMM(modelProjectionMatrix, 0, projectionMatrix, 0, modelMatrix, 0)
    }

    override fun onDrawFrame(gl: GL10?) {
        //1.清屏
        GLES20.glClear(GL_COLOR_BUFFER_BIT)

        // 将变换矩阵传递给GPU着色器
        // 参数详解：
        // uMatrixLocation: 着色器中uniform变量"u_Matrix"的位置，告诉GPU要更新哪个uniform
        // 1: 传递1个矩阵（如果要传递多个矩阵，这里就是矩阵的个数）
        // false: 不转置矩阵（OpenGL期望矩阵是列主序的，所以通常设为false）
        // viewProjectionMatrix: 包含16个浮点数的4x4变换矩阵数组
        // 0: 从数组的第0个位置开始读取数据
        // 作用：将计算好的投影矩阵×模型矩阵的最终变换矩阵发送给GPU，让GPU用它来变换所有顶点
        GLES20.glUniformMatrix4fv(uMatrixLocation, 1, false, modelProjectionMatrix, 0)

        //2.绘制table
        //跟新uColorLocation的值为白色
//        GLES20.glUniform4f(uColorLocation, 1f, 1f, 1f, 1f)
        //绘制table：使用前6个顶点绘制2个三角形
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6)

        //2.绘制分割线
        //跟新uColorLocation的值为红色
//        GLES20.glUniform4f(uColorLocation, 1f, 0f, 0f, 1f)
        //绘制line:使用第6个顶点和第7个顶点绘制1个线段
        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2)

        //3.绘制2个木锥
//        GLES20.glUniform4f(uColorLocation, 0f, 0f, 1f, 1f)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1)
//        GLES20.glUniform4f(uColorLocation, 1f, 0f, 0f, 1f)
        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1)


    }
}