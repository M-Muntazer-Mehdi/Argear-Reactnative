package com.argearproject

import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class ARGearBridgeModule(private val reactCtx: ReactApplicationContext)
  : ReactContextBaseJavaModule(reactCtx) {

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
    activity.runOnUiThread {
      try {
        val intent = Intent(activity, CameraActivity::class.java)
        activity.startActivity(intent)
      } catch (t: Throwable) {
        Log.e(TAG, "Failed to start CameraActivity", t)
        Toast.makeText(reactCtx, "Failed to open camera screen", Toast.LENGTH_SHORT).show()
      }
    }
  }

  companion object { private const val TAG = "ARGearBridge" }
}
