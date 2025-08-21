package com.argearproject

import android.Manifest
import android.content.pm.PackageManager
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.seerslab.argear.session.ARGFrame
import com.seerslab.argear.session.ARGSession
import com.seerslab.argear.session.config.ARGCameraConfig
import com.seerslab.argear.session.config.ARGConfig
import com.seerslab.argear.session.config.ARGInferenceConfig
import java.util.EnumSet
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.opengl.GLES20

class CameraActivity : AppCompatActivity() {

    private var argSession: ARGSession? = null
    private var glSurfaceView: GLSurfaceView? = null
    private var container: FrameLayout? = null

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "CameraActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Root container for GL view
        container = FrameLayout(this).also { setContentView(it) }

        // Ask for camera permission or init immediately
        if (!hasCameraPermission()) {
            requestCameraPermission()
        } else {
            initARGear(requireContainer())
        }
    }

    private fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            val granted = grantResults.isNotEmpty() &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED
            if (granted) {
                Log.d(TAG, "Camera permission granted.")
                initARGear(requireContainer())
            } else {
                Log.d(TAG, "Camera permission denied.")
                Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun initARGear(root: FrameLayout) {
        if (argSession != null && glSurfaceView != null) return // already initialized

        try {
            val config = ARGConfig(
                AppConfig.API_URL,
                AppConfig.API_KEY,
                AppConfig.SECRET_KEY,
                AppConfig.AUTH_KEY
            )

            val inference: Set<ARGInferenceConfig.Feature> =
                EnumSet.of(ARGInferenceConfig.Feature.FACE_HIGH_TRACKING)

            argSession = ARGSession(this, config, inference)

            Log.d(TAG, "ARGSession initialized: ${argSession != null}")

            // Camera configuration (front camera). Note: some OEMs map IDs differently; 1 is typical for front.
            val cameraConfig = ARGCameraConfig(
                720,   // width
                1280,  // height
                30f,   // fps (horizontal)
                30f,   // fps (vertical)
                1,     // camera id (front). Use 0 for back.
                true,  // mirror front camera
                1.0f   // initial zoom
            )
            argSession?.setCameraConfig(cameraConfig)

            Log.d(TAG, "Camera config set successfully")

            // GLSurfaceView for AR rendering
            val glView = GLSurfaceView(this).apply {
                setEGLContextClientVersion(2)
                preserveEGLContextOnPause = true
                setRenderer(object : GLSurfaceView.Renderer {
                    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                        Log.d(TAG, "Surface Created")
                        checkGlError("onSurfaceCreated")
                    }

                    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                        Log.d(TAG, "Surface Changed: width=$width, height=$height")
                    }

                    override fun onDrawFrame(gl: GL10?) {
                        val session = argSession ?: return
                        val view = glSurfaceView ?: return
                        gl?.let {
                            Log.d(TAG, "Drawing frame... width: ${view.width}, height: ${view.height}")
                            val ratio = if (view.width * 16 == view.height * 9) {
                                ARGFrame.Ratio.RATIO_1_1
                            } else {
                                ARGFrame.Ratio.RATIO_FULL
                            }
                            try {
                                session.drawFrame(it, ratio, view.width, view.height)
                                checkGlError("onDrawFrame")  // Check for any OpenGL errors
                            } catch (e: Exception) {
                                Log.e(TAG, "Error drawing frame", e)
                            }
                        }
                    }
                })
                renderMode = GLSurfaceView.RENDERMODE_WHEN_DIRTY
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            glSurfaceView = glView
            root.addView(glView)

            Log.d(TAG, "ARGSession initialized and GLSurfaceView added ✅")

            // Start camera
            startCamera()

        } catch (t: Throwable) {
            Log.e(TAG, "ARGSession init failed", t)
            Toast.makeText(this, "ARGear initialization failed", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun startCamera() {
        if (argSession == null) {
            Log.e(TAG, "ARGSession is null, cannot start camera.")
            return
        }

        // Assuming that the camera has already been configured in ARGSession
        try {
            argSession?.resume() // Resume the session to start the camera
            glSurfaceView?.onResume() // Make sure the GLSurfaceView resumes rendering
            Log.d(TAG, "Camera started successfully.")
        } catch (e: Exception) {
            Log.e(TAG, "Error starting the camera", e)
        }
    }

    // Check for OpenGL errors
    private fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e(TAG, "$op: glError $error")
        } else {
            Log.d(TAG, "$op: No OpenGL error")
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume called")
        glSurfaceView?.onResume()
        argSession?.resume()

        // Handle surface reconnection if it was disconnected
        handleSurfaceReconnect()
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause called")
        glSurfaceView?.onPause()
        argSession?.pause()

        // Log surface disconnect
        Log.d(TAG, "Surface disconnected due to activity pause.")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy called")
        argSession?.destroy()
        argSession = null
        glSurfaceView = null
        container = null
    }

    private fun requireContainer(): FrameLayout =
        (container ?: findViewById<FrameLayout>(android.R.id.content))

    // Debugging Window Focus
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        Log.d(TAG, "onWindowFocusChanged: hasWindowFocus=$hasFocus")
        if (hasFocus) {
            glSurfaceView?.requestRender()  // Request render when focus is regained
        }
    }

    // Track Surface Lifecycle for debugging
    private fun trackSurfaceLifecycle() {
        glSurfaceView?.setRenderer(object : GLSurfaceView.Renderer {
            override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
                Log.d(TAG, "Surface Created")
                checkGlError("onSurfaceCreated")
            }

            override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
                Log.d(TAG, "Surface Changed: width=$width, height=$height")
            }

            override fun onDrawFrame(gl: GL10?) {
                Log.d(TAG, "Drawing frame...")
            }
        })
    }

    // Handle Surface Reconnection
    private fun handleSurfaceReconnect() {
        if (glSurfaceView?.surface == null) {
            Log.d(TAG, "Surface is null, attempting to reconnect.")
            glSurfaceView?.surface = glSurfaceView?.holder.surface
            glSurfaceView?.requestRender()
        }
    }
}
