package com.assistivevolume.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.AudioManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.core.app.NotificationCompat

class FloatingService : Service() {

    companion object {
        var isRunning = false
        private const val CHANNEL_ID = "assistive_volume_channel"
        private const val NOTIFICATION_ID = 101
    }

    private lateinit var windowManager: WindowManager
    private lateinit var audioManager: AudioManager
    private var floatingView: View? = null
    private var menuExpanded = false

    // Track touch position for dragging
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false

    private val handler = Handler(Looper.getMainLooper())
    private val collapseRunnable = Runnable { collapseMenu() }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        showFloatingButton()
    }

    private fun showFloatingButton() {
        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_menu, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 50
            y = 300
        }

        windowManager.addView(floatingView, params)
        setupButtonListeners(params)
    }

    private fun resetIdleTimer() {
        handler.removeCallbacks(collapseRunnable)
        if (menuExpanded) {
            handler.postDelayed(collapseRunnable, 3000) // Auto-collapse after 3 seconds
        }
    }

    private fun collapseMenu() {
        if (!menuExpanded) return
        menuExpanded = false
        floatingView?.findViewById<LinearLayout>(R.id.menuPanel)?.visibility = View.GONE
        updateLayoutParamsFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE)
    }

    private fun expandMenu() {
        if (menuExpanded) return
        menuExpanded = true
        floatingView?.findViewById<LinearLayout>(R.id.menuPanel)?.visibility = View.VISIBLE
        updateLayoutParamsFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL)
        resetIdleTimer()
    }

    private fun updateLayoutParamsFlags(flags: Int) {
        val params = floatingView?.layoutParams as? WindowManager.LayoutParams ?: return
        params.flags = flags
        windowManager.updateViewLayout(floatingView, params)
    }

    private fun setupButtonListeners(params: WindowManager.LayoutParams) {
        val btnMain = floatingView!!.findViewById<ImageButton>(R.id.btnMain)
        val menuPanel = floatingView!!.findViewById<LinearLayout>(R.id.menuPanel)
        val btnVolUp = floatingView!!.findViewById<ImageButton>(R.id.btnVolUp)
        val btnVolDown = floatingView!!.findViewById<ImageButton>(R.id.btnVolDown)
        val btnMute = floatingView!!.findViewById<ImageButton>(R.id.btnMute)
        val btnSilent = floatingView!!.findViewById<ImageButton>(R.id.btnSilent)
        val btnClose = floatingView!!.findViewById<ImageButton>(R.id.btnClose)

        // --- Drag + Tap logic for main button ---
        btnMain.setOnTouchListener { _, event ->
            resetIdleTimer() // Reset on any touch of the main button
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - initialTouchX).toInt()
                    val dy = (event.rawY - initialTouchY).toInt()
                    if (Math.abs(dx) > 5 || Math.abs(dy) > 5) isDragging = true
                    if (isDragging) {
                        params.x = initialX + dx
                        params.y = initialY + dy
                        windowManager.updateViewLayout(floatingView, params)
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!isDragging) {
                        if (menuExpanded) collapseMenu() else expandMenu()
                    } else {
                        resetIdleTimer() // Restart timer if dropped while expanded
                    }
                    true
                }
                else -> false
            }
        }

        // --- Volume Controls ---
        btnVolUp.setOnClickListener {
            resetIdleTimer()
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_RAISE,
                AudioManager.FLAG_SHOW_UI
            )
        }

        btnVolDown.setOnClickListener {
            resetIdleTimer()
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_LOWER,
                AudioManager.FLAG_SHOW_UI
            )
        }

        btnMute.setOnClickListener {
            resetIdleTimer()
            audioManager.adjustStreamVolume(
                AudioManager.STREAM_MUSIC,
                AudioManager.ADJUST_TOGGLE_MUTE,
                AudioManager.FLAG_SHOW_UI
            )
        }

        btnSilent.setOnClickListener {
            resetIdleTimer()
            val currentMode = audioManager.ringerMode
            val nextMode = when (currentMode) {
                AudioManager.RINGER_MODE_NORMAL -> AudioManager.RINGER_MODE_VIBRATE
                AudioManager.RINGER_MODE_VIBRATE -> AudioManager.RINGER_MODE_SILENT
                else -> AudioManager.RINGER_MODE_NORMAL
            }
            audioManager.ringerMode = nextMode
        }

        // Close button
        btnClose.setOnClickListener {
            stopSelf()
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "AssistiveVolume Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Tombol volume mengambang aktif" }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    private fun buildNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("AssistiveVolume Aktif")
            .setContentText("Tombol mengambang sedang berjalan")
            .setSmallIcon(android.R.drawable.ic_lock_silent_mode_off)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(collapseRunnable)
        isRunning = false
        floatingView?.let { windowManager.removeView(it) }
        floatingView = null
    }
}
