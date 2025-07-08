package com.example.openglfirstdemo

import android.app.ActivityManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var gLView: GLSurfaceView
    private var renderSet = false


    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        //挖洞
        gLView = GLSurfaceView(this)
        setContentView(gLView)

        if (isSupportEs2()) {
            gLView.setEGLContextClientVersion(2)
            gLView.setRenderer(AirHockeyRender(this))
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
}