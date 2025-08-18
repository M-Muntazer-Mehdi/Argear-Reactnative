// android/app/src/main/java/com/argearproject/MainApplication.kt
package com.argearproject

import android.app.Application
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.PackageList            // <-- add this
import com.facebook.soloader.SoLoader
import com.facebook.react.soloader.OpenSourceMergedSoMapping // <-- keep this

class MainApplication : Application(), ReactApplication {

  override val reactNativeHost: ReactNativeHost = object : ReactNativeHost(this) {
    override fun getUseDeveloperSupport() = BuildConfig.DEBUG

    // Use PackageList so all autolinked modules (e.g., SafeAreaContext) are included
    override fun getPackages(): List<ReactPackage> {
      val packages = PackageList(this).packages
      packages.add(ARGearBridgePackage()) // keep your custom bridge
      return packages
    }

    override fun getJSMainModuleName() = "index"
  }

  override fun onCreate() {
    super.onCreate()
    // RN 0.76+ merged .so init (this solved your previous startup crash)
    SoLoader.init(this, OpenSourceMergedSoMapping)
  }
}
