package com.argearproject

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class ARGearBridgeModule(private val reactCtx: ReactApplicationContext)
    : ReactContextBaseJavaModule(reactCtx) {

    companion object {
        private const val TAG = "ARGearBridge"
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1002
    }

    override fun getName(): String = "ARGearBridge"

    @ReactMethod
    fun exampleMethod(message: String) {
        Log.d(TAG, "Received from JS: $message")
        Toast.makeText(reactCtx, "ARGearBridge: $message", Toast.LENGTH_SHORT).show()
    }

    @ReactMethod
    fun startCameraActivity() {
        val activity = reactCtx.currentActivity
        if (activity == null) {
            Log.e(TAG, "startCameraActivity: currentActivity is null (app not in foreground?)")
            Toast.makeText(reactCtx, "No foreground Activity", Toast.LENGTH_SHORT).show()
            return
        }

        // Log to confirm activity is valid
        Log.d(TAG, "startCameraActivity: currentActivity is not null: ${activity.localClassName}")

        // Check camera permission
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission not granted. Requesting permission...")
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission already granted, start camera
            Log.d(TAG, "Camera permission already granted, starting CameraActivity...")
            openCamera(activity)
        }
    }

    private fun openCamera(activity: Activity) {
        activity.runOnUiThread {
            try {
                Log.d(TAG, "Starting CameraActivity...")
                val intent = Intent(activity, CameraActivity::class.java)
                activity.startActivity(intent)
                Log.d(TAG, "CameraActivity started successfully.")
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to start CameraActivity", t)
                Toast.makeText(reactCtx, "Failed to open camera screen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Call this from MainActivity's onRequestPermissionsResult
    fun handlePermissionResult(requestCode: Int, grantResults: IntArray) {
        Log.d(TAG, "handlePermissionResult called with requestCode: $requestCode")
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            val activity = reactCtx.currentActivity
            if (activity == null) {
                Log.e(TAG, "handlePermissionResult: currentActivity is null (app not in foreground?)")
                return
            }

            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted.")
                openCamera(activity)
            } else {
                Log.d(TAG, "Camera permission denied.")
                Toast.makeText(reactCtx, "Camera permission denied", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "handlePermissionResult: Unknown request code $requestCode")
        }
    }
}
