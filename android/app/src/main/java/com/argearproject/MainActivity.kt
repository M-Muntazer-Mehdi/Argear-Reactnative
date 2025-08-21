package com.argearproject

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.facebook.react.ReactActivity
import android.util.Log

class MainActivity : ReactActivity() {

    companion object {
        const val CAMERA_PERMISSION_REQUEST_CODE = 1002
        private const val TAG = "MainActivity"
    }

    override fun getMainComponentName() = "ARGearProject"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Optionally, you can request permission on app start if needed.
    }

    // Function to request camera permission and start CameraActivity
    fun requestCameraPermissionAndStartCamera() {
        Log.d(TAG, "Checking camera permission...")

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Camera permission not granted, requesting permission.")
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            Log.d(TAG, "Camera permission already granted.")
            // Permission already granted, start camera activity directly
            startCameraActivity()
        }
    }

    // Start CameraActivity after permission is granted
    private fun startCameraActivity() {
        Log.d(TAG, "Starting CameraActivity...")
        val intent = Intent(this, CameraActivity::class.java)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            Log.d(TAG, "onRequestPermissionsResult: ${grantResults[0]}")
            
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Camera permission granted.")
                // Permission granted, start camera activity
                startCameraActivity()
            } else {
                Log.d(TAG, "Camera permission denied.")
                // Permission denied, show a Toast or guide user
                Toast.makeText(this, "Camera permission is required to use the camera.", Toast.LENGTH_SHORT).show()

                // Optionally, you can show a dialog or redirect the user to settings
                // to enable the permission manually
            }
        }
    }
}
