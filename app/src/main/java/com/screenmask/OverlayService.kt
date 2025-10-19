
package com.screenmask

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private val overlayViews = mutableListOf<View>()
    private val ruleManager by lazy { RuleManager(this) }

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createOverlays()
    }

    private fun createOverlays() {
        overlayViews.forEach { windowManager.removeView(it) }
        overlayViews.clear()

        val enabledRules = ruleManager.getRules().filter { it.enabled }

        enabledRules.forEach { rule ->
            val view = View(this)
            view.setBackgroundColor(rule.color)

            val params = WindowManager.LayoutParams(
                rule.right - rule.left,
                rule.bottom - rule.top,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            )

            params.gravity = Gravity.TOP or Gravity.START
            params.x = rule.left
            params.y = rule.top

            windowManager.addView(view, params)
            overlayViews.add(view)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        overlayViews.forEach { windowManager.removeView(it) }
        overlayViews.clear()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}