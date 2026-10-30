package com.example.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.repository.ConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.abs

class FloatingBubbleService : Service() {

    private var windowManager: WindowManager? = null
    private var bubbleView: View? = null
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())
    private var tickerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        try {
            windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
            bubbleView = createBubbleView()

            val layoutType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 60
                y = 200
            }

            setupTouchListener(bubbleView!!, params)
            windowManager?.addView(bubbleView, params)
            startTicker()
        } catch (_: Exception) {
            stopSelf()
        }
    }

    private fun createBubbleView(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(20, 14, 24, 14)

            // Modern Pill Background with primary blue border
            val background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 100f
                setColor(Color.parseColor("#1E293B")) // Sleek slate dark background
                setStroke(3, Color.parseColor("#42A5F5")) // Bright cyan/blue border
            }
            this.background = background
            elevation = 16f
        }

        val icon = ImageView(this).apply {
            setImageResource(R.drawable.sfr_logo)
            val size = 56
            layoutParams = LinearLayout.LayoutParams(size, size).apply {
                marginEnd = 14
            }
        }

        val textContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        val titleText = TextView(this).apply {
            text = "SFR Droid • Standby"
            setTextColor(Color.parseColor("#90CAF9"))
            textSize = 10f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
        }

        val timerText = TextView(this).apply {
            tag = "timer_text"
            text = "⏱️ 00:00:00"
            setTextColor(Color.WHITE)
            textSize = 12f
            typeface = android.graphics.Typeface.MONOSPACE
        }

        textContainer.addView(titleText)
        textContainer.addView(timerText)

        root.addView(icon)
        root.addView(textContainer)

        return root
    }

    private fun setupTouchListener(view: View, params: WindowManager.LayoutParams) {
        var initialX = 0
        var initialY = 0
        var initialTouchX = 0f
        var initialTouchY = 0f
        var isClick = false

        view.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isClick = true
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    if (abs(deltaX) > 10 || abs(deltaY) > 10) {
                        isClick = false
                    }
                    params.x = initialX + deltaX
                    params.y = initialY + deltaY
                    try {
                        windowManager?.updateViewLayout(view, params)
                    } catch (_: Exception) {}
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (isClick) {
                        // Open app on click
                        val appIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        }
                        startActivity(appIntent)
                    }
                    true
                }
                else -> false
            }
        }
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = serviceScope.launch {
            val configRepo = ConfigRepository.getInstance(applicationContext)
            while (isActive) {
                val config = configRepo.getConfig()
                if (!config.isStandby || !config.floatingBubbleEnabled) {
                    stopSelf()
                    break
                }

                val startTime = config.standbyStartTime
                val now = System.currentTimeMillis()
                val elapsedMs = if (startTime > 0) (now - startTime).coerceAtLeast(0L) else 0L
                val seconds = (elapsedMs / 1000) % 60
                val minutes = (elapsedMs / (1000 * 60)) % 60
                val hours = elapsedMs / (1000 * 60 * 60)
                val uptimeStr = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)

                bubbleView?.findViewWithTag<TextView>("timer_text")?.text = "⏱️ $uptimeStr"

                delay(1000L)
            }
        }
    }

    override fun onDestroy() {
        tickerJob?.cancel()
        try {
            if (bubbleView != null && windowManager != null) {
                windowManager?.removeView(bubbleView)
            }
        } catch (_: Exception) {}
        bubbleView = null
        super.onDestroy()
    }

    companion object {
        fun start(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                return
            }
            val intent = Intent(context, FloatingBubbleService::class.java)
            context.startService(intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, FloatingBubbleService::class.java)
            context.stopService(intent)
        }
    }
}
