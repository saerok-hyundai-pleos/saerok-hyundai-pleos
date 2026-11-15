package com.saerok.pleos

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.webkit.ConsoleMessage
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewAssetLoader
import androidx.webkit.WebViewClientCompat
import org.json.JSONObject
import java.io.ByteArrayInputStream

class MainActivity : Activity() {
    private lateinit var webView: WebView
    private val ui = Handler(Looper.getMainLooper())
    private val origin = "https://appassets.androidplatform.net/assets/saerok/"
    private val prefs by lazy { getSharedPreferences("appearance", MODE_PRIVATE) }
    private val locations by lazy { getSystemService(LocationManager::class.java) }
    private var listening = false
    private var resumed = false
    private var receivedLocation = false
    private var permissionPending = false
    private val listener = object : LocationListener {
        override fun onLocationChanged(location: Location) { deliver(location) }
        override fun onProviderDisabled(provider: String) { locationError("기기의 위치 기능을 켜면 자동 전환할 수 있어요.") }
        override fun onProviderEnabled(provider: String) { if (resumed) acquire(false) }
        @Deprecated("Legacy provider callback")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }
    private fun trusted() = ::webView.isInitialized && webView.url?.startsWith(origin) == true
    private fun js(code: String) { if (trusted()) webView.evaluateJavascript(code, null) }
    private fun locationError(message: String) { js("window.SaerokTheme?.locationError(${JSONObject.quote(message)})") }
    private fun deliver(location: Location) {
        if (!prefs.getBoolean("auto", false) || !resumed) return
        receivedLocation = true
        js("window.SaerokTheme?.receiveLocation(${location.latitude},${location.longitude})")
    }
    private fun stopLocations() {
        if (listening) locations.removeUpdates(listener)
        listening = false
    }
    @SuppressLint("MissingPermission")
    private fun acquire(prompt: Boolean) {
        if (!prefs.getBoolean("auto", false) || !resumed || !trusted()) return
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (prompt && !permissionPending) {
                permissionPending = true
                requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 301)
            } else locationError("자동 전환을 위해 대략적인 위치 권한을 허용해주세요.")
            return
        }
        try {
            val providers = locations.getProviders(true).filter { it != LocationManager.PASSIVE_PROVIDER }
            val fresh = providers.mapNotNull { locations.getLastKnownLocation(it) }
                .filter { System.currentTimeMillis() - it.time in 0..21_600_000L }
                .maxByOrNull { it.time }
            if (fresh != null) deliver(fresh)
            if (providers.isEmpty()) { locationError("기기의 위치 기능을 켜면 자동 전환할 수 있어요."); return }
            if (!listening) {
                receivedLocation = fresh != null
                providers.forEach { locations.requestLocationUpdates(it, 300_000L, 5_000f, listener, Looper.getMainLooper()) }
                listening = true
                ui.postDelayed({ if (resumed && prefs.getBoolean("auto", false) && !receivedLocation) locationError("아직 위치를 확인하지 못했어요. 현재 화면을 유지하며 위치를 기다립니다.") }, 15_000)
            }
        } catch (e: Exception) { locationError("위치를 확인할 수 없어요. 자동을 끄고 화면을 직접 선택할 수 있어요.") }
    }
    private inner class ThemeBridge {
        @JavascriptInterface fun setSurfaceDark(dark: Boolean) { runOnUiThread { if (trusted()) { prefs.edit().putBoolean("surfaceDark", dark).apply(); webView.setBackgroundColor(if (dark) 0xFF12171A.toInt() else 0xFFF7F3EA.toInt()) } } }
        @JavascriptInterface fun setSolarAuto(enabled: Boolean) { runOnUiThread { if (trusted()) { prefs.edit().putBoolean("auto", enabled).apply(); if (enabled) acquire(false) else stopLocations() } } }
        @JavascriptInterface fun requestSunLocation() { runOnUiThread { if (trusted()) acquire(true) } }
    }
    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val loader = WebViewAssetLoader.Builder().addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this)).build()
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
        webView = WebView(this).apply {
            setBackgroundColor(if (prefs.getBoolean("surfaceDark", false)) 0xFF12171A.toInt() else 0xFFF7F3EA.toInt())
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = false
            settings.allowContentAccess = false
            settings.textZoom = 100
            addJavascriptInterface(ThemeBridge(), "SaerokNative")
            webViewClient = object : WebViewClientCompat() {
                override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse {
                    if (request.url.scheme == "https" && request.url.host == WebViewAssetLoader.DEFAULT_DOMAIN) loader.shouldInterceptRequest(request.url)?.let { return it }
                    return WebResourceResponse("text/plain", "UTF-8", 404, "Not Found", emptyMap(), ByteArrayInputStream(ByteArray(0)))
                }
                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean = !request.url.toString().startsWith(origin)
                override fun onPageFinished(view: WebView, url: String) {
                    if (trusted()) js("window.SaerokNative.setSolarAuto(window.SaerokTheme?.preference.auto===true);window.SaerokTheme?.refresh(false)")
                }
            }
            webChromeClient = object : WebChromeClient() {
                override fun onConsoleMessage(message: ConsoleMessage): Boolean { Log.d("SaerokWeb", "${message.messageLevel()}: ${message.message()} @${message.lineNumber()}"); return true }
            }
            loadUrl(origin + "index.html")
        }
        setContentView(webView)
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 301) { permissionPending = false; if (grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED) acquire(false) else locationError("위치 권한 없이 자동 전환할 수 없어요. 현재 화면을 유지합니다.") }
    }
    override fun onResume() { super.onResume(); resumed = true; webView.onResume(); ui.post { js("window.SaerokTheme?.refresh(false)"); acquire(false) } }
    override fun onPause() { resumed = false; stopLocations(); webView.onPause(); super.onPause() }
    override fun onDestroy() { stopLocations(); ui.removeCallbacksAndMessages(null); webView.removeJavascriptInterface("SaerokNative"); (webView.parent as? android.view.ViewGroup)?.removeView(webView); webView.destroy(); super.onDestroy() }
}
