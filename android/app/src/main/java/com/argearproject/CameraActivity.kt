package com.argearproject

import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.seerslab.argear.session.ARGSession
import com.seerslab.argear.session.config.ARGConfig
import com.seerslab.argear.session.config.ARGInferenceConfig
import java.util.EnumSet

class CameraActivity : AppCompatActivity() {

  private var argSession: ARGSession? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Simple UI so you can see the screen opened
    val root = FrameLayout(this)
    val tv = TextView(this).apply { text = "ARGear Smoke Test (no camera yet)" }
    root.addView(tv)
    setContentView(root)

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
      Log.d("ARGear", "ARGSession created ✅")
    } catch (t: Throwable) {
      Log.e("ARGear", "ARGSession init failed", t)
      finish()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    // If your SDK exposes an explicit release, call it here.
    try { argSession = null } catch (_: Throwable) {}
  }
}
